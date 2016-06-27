package org.tus.servlet.upload;
 

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
/*
	Send current offset in upload or 404
*/
public class HeadHandler extends BaseHandler 
{
	private static final Logger log = LoggerFactory.getLogger(HeadHandler.class.getName());

	public HeadHandler(Composer composer, HttpServletRequest request, Response response)
	{
		super(composer, request, response);
	}

	@Override
	public void go() throws Exception
	{
		String id = getID();
		if (id == null)
		{
			log.debug("url has no valid id part");
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
			whileLocked(id);
		}
		finally
		{
			if (locked)
			{
				locker.unlockUpload(id);
			}
		}

	}

	private void whileLocked(String id)
		throws Exception
	{
		FileInfo fileInfo = datastore.getFileInfo(id);
		if (fileInfo == null)
		{
			log.debug("id '" + id + "' not found");
			throw new TusException.NotFound();
		}

		if (fileInfo.metadata != null && fileInfo.metadata.length() > 0)
		{
			response.setHeader("Upload-Metadata", fileInfo.metadata);
		}
		response.setHeader("Cache-Control", "no-store");
		response.setHeader("Upload-Length", Long.toString(fileInfo.entityLength));
		response.setHeader("Upload-Offset", Long.toString(fileInfo.offset));
		response.setStatus(Response.NO_CONTENT);
	}
}
