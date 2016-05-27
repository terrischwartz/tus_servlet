package org.tus.servlet.upload;
 

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
public class PatchHandler extends MethodHandler 
{
	private static final Logger log = LoggerFactory.getLogger(PostHandler.class.getName());

	public PatchHandler(Config config, Locker locker, Datastore datastore)
	{
		super(config, locker, datastore);
	}

	@Override
	public boolean go(HttpServletRequest request, HttpServletResponse response)
		throws Exception
	{

		super.go (request, response);

		// Check content type header
		String ct = request.getHeader("Content-Type");
		if (ct == null || !ct.equals("application/offset+octet-stream"))
		{
			log.debug("Missing or invalid content type header.");
			return send(request, response, 400, "missing or invalid Content-Type header.");
		}

		// Check offset header
		Long offset = getLongHeader(request, "upload-offset");
		if (offset == null ||  (long)offset < 0)
		{
			log.debug("upload-offset header missing or invalid.");
			response.setContentType("text/plain");
			return send(request, response, 400, "Upload-Offset header must be a non-negative number.");
		}

		// Get file ID from url
		String filename = getFilename(request);
		if (filename == null)
		{
			log.debug("No file id found in patch url");
			return send(request, response, 404, "Missing file ID in url.");
		}

		boolean locked = false;
		try
		{
			locked = locker.lockUpload(filename);
			if (!locked)
			{
				// todo: other locking errors?  Have locker return an obj with error? 
				log.info("Couldn't lock " + filename);
				return send(request, response, 423, "file is currently locked.");
			}
			return whileLocked(request, response, filename, offset);
		}
		finally
		{
			if (locked)
			{
				locker.unlockUpload(filename);
			}
		}

	}

	private boolean whileLocked(HttpServletRequest request, HttpServletResponse response, String filename, long offset)
		throws Exception
	{

		FileInfo fileInfo = datastore.getFileInfo(filename);
		if (fileInfo == null)
		{
			log.debug("filename not found");
			return send(request, response, 404, "Not found.");
		}

		// Offset in request header must match current file length.
		if (offset != fileInfo.offset)
		{
			log.debug("current file size of " + fileInfo.offset + " doesn't match upload-offset of " + offset);
			return send(request, response, 409, "Mismatched offset.");
		}

		// Offset must be less than expected total length.
		if (offset >= fileInfo.entityLength)
		{
			log.debug("offset " + offset + " is >= total file size of " + fileInfo.entityLength);
			response.setHeader("Upload-Offset", Long.toString(offset));
			return send(request, response, 204, "");
		}

		Long contentLength = getLongHeader(request, "content-length");
		log.debug("Content-length is " + contentLength);

		// If contentLength header present, make sure contentLength + offset <= entityLength
		if (contentLength != null && ((long)contentLength + offset > fileInfo.entityLength))
		{
			log.debug("content-length + offset > entity-length");
			return send(request, response, 413, "resource's size exceeded");
		}

		long maxToRead = contentLength != null ? (long)contentLength : fileInfo.entityLength - offset;

		// Write the data.  If Store.write() throws an exception, Upload.java will handle it and send 500 status.
		long transferred = datastore.write(request, filename, (long)offset, maxToRead);
		long newOffset = transferred + (long)offset; 

		// If upload is complete ...
		if (newOffset == fileInfo.entityLength)
		{
			log.debug("Upload " + filename + " is complete.");
			// TODO: If datastore requires cleanup -> ? 
			// TODO: notify listeners -> ?
		}

		response.setHeader("Upload-Offset", Long.toString(newOffset));
		return send(request, response, 204, "");
	}

}
