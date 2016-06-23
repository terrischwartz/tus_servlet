package org.tus.servlet.upload;
 

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
/*
	Todo: 
	- don't accept uploads when running out of disk space? (tusd uses a mix-in class for this)
	- deferredLength???  
*/
public class PostHandler extends BaseHandler 
{
	private static final Logger log = LoggerFactory.getLogger(PostHandler.class.getName());

	public PostHandler(Composer composer, HttpServletRequest request, Response response)
	{
		super(composer, request, response);
	}

	@Override
	public void go() throws Exception
	{
		Long length = getLongHeader("upload-length");
		if (length == null || (long)length < 0)
		{
			throw new TusException.InvalidUploadLength();
		}
		if (config.maxSize > 0 && ((long)length > config.maxSize))
		{
			throw new TusException.MaxSizeExceeded();
		}

		// TODO: check if we have enough storage space?

		String metadata = request.getHeader("Upload-Metadata");

		// Generate unique id to serve as the file ID and store optional metadata.
		FileInfo fileInfo = new FileInfo((long)length, metadata, Upload.getAuthenticatedUser(request));

		datastore.create(fileInfo);

		String url = request.getRequestURL().toString(); 
		if (url.endsWith("/"))
		{
			url += fileInfo.id;
		} else
		{
			url += "/" + fileInfo.id;
		}
		log.debug("return url in location header.  url is " + url);

		response.setHeader("Location", url);
		response.setStatus(Response.CREATED);
	}

}
