package org.tus.servlet.upload;
 

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
/*
	Send current offset in upload or 404
*/
public class HeadHandler extends MethodHandler 
{
	private static final Logger log = LoggerFactory.getLogger(HeadHandler.class.getName());

	public HeadHandler(Config config, Locker locker, Datastore datastore)
	{
		super(config, locker, datastore);
	}


	@Override
	public boolean go(HttpServletRequest request, HttpServletResponse response)
		throws Exception
	{
		super.go(request, response);
		String filename = getFilename(request);
		if (filename == null)
		{
			log.debug("url has no valid filename part");
			return send(request, response, 404, "");
		}
		boolean locked = false;
		try
		{
			locked = locker.lockUpload(filename);
			if (!locked)
			{
				// todo: other locking errors?  Have locker return an obj with error? 
				// can't send error message in body when request is HEAD.  423 = LOCKED
				log.info("Couldn't lock " + filename);
				return send(request, response, 423, "");
			}
			return whileLocked(request, response, filename);
		}
		finally
		{
			if (locked)
			{
				locker.unlockUpload(filename);
			}
		}

	}

	private boolean whileLocked(HttpServletRequest request, HttpServletResponse response, String filename)
		throws Exception
	{
		FileInfo fileInfo = datastore.getFileInfo(filename);
		if (fileInfo == null)
		{
			log.debug("filename not found");
			return send(request, response, 404, "");
		}

		if (fileInfo.metadata != null && fileInfo.metadata.length() > 0)
		{
			response.setHeader("Upload-Metadata", fileInfo.metadata);
		}
		response.setHeader("Cache-Control", "no-store");
		response.setHeader("Upload-Length", Long.toString(fileInfo.entityLength));
		response.setHeader("Upload-Offset", Long.toString(fileInfo.offset));
		// 204 = NO CONTENT
		return send(request, response, 204, "");
	}
}
