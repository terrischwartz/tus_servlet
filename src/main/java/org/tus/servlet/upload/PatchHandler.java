package org.tus.servlet.upload;
 

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
public class PatchHandler extends BaseHandler 
{
	private static final Logger log = LoggerFactory.getLogger(PatchHandler.class.getName());

	public PatchHandler(Composer composer, HttpServletRequest request, Response response)
	{
		super(composer, request, response);
	}


	@Override
	public void go() throws Exception
	{
		// Check content type header
		String ct = request.getHeader("Content-Type");
		if (ct == null || !ct.equals("application/offset+octet-stream"))
		{
			log.debug("Missing or invalid content type header.");
			throw new TusException.InvalidContentType();
		}

		// Check offset header
		Long offset = getLongHeader("upload-offset");
		if (offset == null ||  (long)offset < 0)
		{
			log.debug("upload-offset header missing or invalid.");
			throw new TusException.InvalidOffset();
		}

		// Get file ID from url
		String id = getID();
		if (id == null)
		{
			log.debug("No file id found in patch url");
			throw new TusException.NotFound();
		}

		boolean locked = false;
		try
		{
			locked = locker.lockUpload(id);
			if (!locked)
			{
				log.info("Couldn't lock " + id);
				throw new TusException.FileLocked();
			}
			whileLocked(id, offset);
		}
		finally
		{
			if (locked)
			{
				locker.unlockUpload(id);
			}
		}
	}

	private void whileLocked(String id, long offset)
		throws Exception
	{
		FileInfo fileInfo = datastore.getFileInfo(id);
		if (fileInfo == null)
		{
			log.debug("fileInfo not found for '" + id + "'");
			throw new TusException.NotFound();
		}

		// Offset in request header must match current file length.
		if (offset != fileInfo.offset)
		{
			log.debug("current file size of " + fileInfo.offset + " doesn't match upload-offset of " + offset);
			throw new TusException.MismatchOffset();
		}

		long newOffset = fileInfo.offset;

		// Only write the data to store if we haven't already got the full file.
		if (fileInfo.offset != fileInfo.entityLength)
		{
			Long contentLength = getLongHeader("content-length");
			log.debug("Content-length is " + contentLength);

			// If contentLength header present, make sure contentLength + offset <= entityLength
			if (contentLength != null && ((long)contentLength + offset > fileInfo.entityLength))
			{
				log.debug("content-length + offset > entity-length: " + contentLength + " + " + 
					offset + " > " + fileInfo.entityLength);
				throw new TusException.SizeExceeded();
			}

			// Don't exceed entityLength.
			long maxToRead = contentLength != null ? (long)contentLength : fileInfo.entityLength - offset;

			// Write the data.  
			long transferred = datastore.write(request, id, (long)offset, maxToRead);
			newOffset = transferred + (long)offset; 

			// If upload is complete ...
			if (newOffset == fileInfo.entityLength)
			{
				log.debug("Upload " + id + " is complete.");
				datastore.finish(id);
			}
		} 
		response.setHeader("Upload-Offset", Long.toString(newOffset));
		response.setStatus(Response.NO_CONTENT);
	}
}
