package org.tus.servlet.upload;
 

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
public class OptionsHandler extends MethodHandler 
{
	private static final Logger log = LoggerFactory.getLogger(PostHandler.class.getName());

	public OptionsHandler(Config config, Locker locker, Datastore datastore)
	{
		super(config, locker, datastore);
	}

	@Override
	public boolean go(HttpServletRequest request, HttpServletResponse response)
		throws Exception
	{
		super.go(request, response);

		if (datastore.getExtensions() != null)
		{
			response.setHeader("Tus-Extension", datastore.getExtensions());
		}
		if (config.maxSize > 0)
		{
			response.setHeader("Tus-Max-Size", Long.toString(config.maxSize));
		}
		return send(request, response, 200, "");
	}
}
