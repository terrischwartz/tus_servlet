package org.tus.servlet.upload;
 
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
/*
	The servlet_path is configured in servlet mapping in web.xml. 
	If using this servlet in a struts application, in struts.xml tell struts
	to exclude "/servlet_path/[0-9a-zA-Z]*".
	This servlet should only see uris of the form /servlet_path or /servlet_path/123foo , but nothing with
	special characters or deeper paths.  

	TODO: consider creating a handler for each request.  Code can be more like
	Jersey's then.  No need to pass request and response between methods.

*/
@SuppressWarnings("serial")
public class Upload extends HttpServlet 
{
	private static final Logger log = LoggerFactory.getLogger(Upload.class.getName());
	private Config config;
	private Composer composer;

	@Override
	public void init() throws ServletException
	{
		log.debug("Initialize Upload servlet");
		config = new Config(getServletConfig());
		composer = new Composer(config);
	}

	@Override
	public void service(HttpServletRequest request, HttpServletResponse servletResponse)
		throws IOException, ServletException 
	{
		Response response = new Response(servletResponse);
		try
		{
			log.debug("UPLOAD SERVLET " + request.getMethod() + " " + request.getRequestURL());

			checkVersion(request, response);

			String method = request.getMethod();
			if (method.equals("OPTIONS"))
			{
				new OptionsHandler(composer, request, response).go();
			} else if (method.equals("HEAD"))
			{
				new HeadHandler(composer, request, response).go();
			} else if (method.equals("PATCH"))
			{
				new PatchHandler(composer, request, response).go();
			} else if (method.equals("POST"))
			{
				new PostHandler(composer, request, response).go();
			} else
			{
				log.info("Method " + request.getMethod() + " not allowed.");
				throw new TusException.MethodNotAllowed();
			}
		}
		catch (TusException texc)
		{
			response.setStatus(texc.getStatus()).setText(texc.getText());
		}
		catch(Exception e)
		{
			log.error("", e);
			response.setStatus(500).setText((e.getMessage()  == null) ? 
				"Server Error" : "Server Error: " + e.getMessage());
		}
		send(request, response);
	}

	private void checkVersion(HttpServletRequest request, Response response)
		throws Exception
	{
		String clientVersion = request.getHeader("tus-resumable");
		if (!request.getMethod().equals("OPTIONS") && 
			(clientVersion == null || !clientVersion.equals(config.tusApiVersionSupported)))
		{
			throw new TusException.UnsupportedVersion();
		}
	}


	private void send(HttpServletRequest request, Response response)
		throws IOException
	{
		response.setHeader("Tus-Resumable", config.tusApiVersionSupported);
		response.setHeader("X-Content-Type-Options", "nosniff");
		addAccessHeaders(request, response);
		if (request.getMethod().equals("HEAD"))
		{
			response.setText("");
		}
		String body = response.getText();
		if (body.length() > 0)
		{
			body += "\n";
			response.setHeader("Content-Type", "text/plain; charset=utf-8");
			response.setHeader("Content-Length", Long.toString(body.length()));
		}
		response.setText(body);
		response.write();
	}

	private void addAccessHeaders(HttpServletRequest request, Response response)
	{
		String origin = request.getHeader("Origin");
		if (origin != null && origin.length() > 0)
		{
			response.setHeader("Access-Control-Allow-Origin", origin);
			if (request.getMethod().equals("OPTIONS"))
			{
				response.setHeader("Access-Control-Allow-Methods", 
					"POST, GET, HEAD, PATCH, DELETE, OPTIONS");
				response.setHeader("Access-Control-Allow-Headers", 
						"Origin, " +
						"X-Requested-With, " +
						"Content-Type, " +
						"Upload-Length, " +
						"Upload-Offset, " +
						"Tus-Resumable, " +
						"Upload-Metadata");
				response.setHeader("Access-Control-Max-Age", "86400");

			} else
			{
				response.setHeader("Access-Control-Expose-Headers", 
						"Upload-Offset, " +
						"Location, " +
						"Upload-Length, " +
						"Tus-Version, " +
						"Tus-Resumable, " +
						"Tus-Max-Size, " +
						"Tus-Extension, " +
						"Upload-Metadata");
			}
		}
	}



}

