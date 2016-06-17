package org.tus.servlet.upload;
 

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
public class DeleteHandler extends BaseHandler 
{
	private static final Logger log = LoggerFactory.getLogger(DeleteHandler.class.getName());

	public DeleteHandler(Composer composer, HttpServletRequest request, Response response)
	{
		super(composer, request, response);
	}


	@Override
	public void go() throws Exception
	{
		// Get file ID from url
		String filename = getFilename();
		if (filename == null)
		{
			log.debug("No file id found in patch url");
			throw new TusException.NotFound();
		}
		boolean locked = false;
		try
		{
			locked = locker.lockUpload(filename);
			if (!locked)
			{
				log.info("Couldn't lock " + filename);
				throw new TusException.FileLocked();
			}
			whileLocked(filename);
		}
		finally
		{
			if (locked)
			{
				locker.unlockUpload(filename);
			}
		}
	}

	private void whileLocked(String filename)
		throws Exception
	{
		FileInfo fileInfo = datastore.getFileInfo(filename);
		if (fileInfo == null)
		{
			log.debug("fileInfo not found for '" + filename + "'");
			throw new TusException.NotFound();
		}
		datastore.terminate(filename);
		response.setStatus(Response.NO_CONTENT);
	}


}
