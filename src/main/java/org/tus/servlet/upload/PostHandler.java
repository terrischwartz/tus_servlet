package org.tus.servlet.upload;
 

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
/*
	Todo: 
	- implement a max upload file size (see how tusd does this)
	- don't accept uploads when running out of disk space? (tusd uses a mix-in class for this)
	- not using deferredLength???  Check latest github tus-node code.
*/
public class PostHandler extends MethodHandler 
{
	private static final Logger log = LoggerFactory.getLogger(PostHandler.class.getName());

	public PostHandler(Config config, Locker locker, Datastore datastore)
	{
		super(config, locker, datastore);
	}

	@Override
	public boolean go(HttpServletRequest request, HttpServletResponse response)
		throws Exception
	{
		super.go(request, response);
		Long length = getLongHeader(request, "upload-length");
		if (length == null || (long)length < 0)
		{
			// 400 = BAD REQUEST
			return send(request, response, 400, "Upload-Length header required.  Must be non-negative integer.");
		}
		if (config.maxSize > 0 && ((long)length > config.maxSize))
		{
			// 413 = ENTITY TOO LARGE
			return send(request, response, 413, "Maximum file size exceeded.");
		}

		// TODO: check if we have enough storage space?

		String metadata = request.getHeader("Upload-Metadata");

		// Generate unique id to serve as the file ID and store optional metadata.
		FileInfo fileInfo = new FileInfo((long)length, metadata);


		// Can throw exception which will result in 500 status to client.
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
		return send(request, response, 201,  ""); 
	}

}
