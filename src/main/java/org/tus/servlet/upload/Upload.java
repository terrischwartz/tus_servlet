package org.tus.servlet.upload;
 
import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
/*
	In struts.xml, I told struts to exclude /upload/[0-9a-zA-Z]* so this servlet
	should only see uris of the form /upload or /upload/123foo , but nothing with
	special characters or deeper paths.  Also had to tell web.xml to send urls
	starting with /upload here.

*/
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
	public void service(HttpServletRequest request, HttpServletResponse response)
		throws IOException, ServletException 
	{
		try
		{
			log.debug("UPLOAD SERVLET " + request.getMethod() + " " + request.getRequestURL());

			/*
			String method = request.getHeader("x-http-method-override");
			if (method != null)
			{
				method = method.toUpperCase();
			} else
			{
				method = request.getMethod();
			}
			*/
			MethodHandler handler = composer.methodHandlers.get(request.getMethod());
			if (handler == null)
			{
				log.info("Method " + request.getMethod() + " not valid.");
				send(response, 404, "");
				return;
			}
			handler.go(request, response);
		}
		catch(IOException ioe)
		{
			log.error("", ioe);
			throw ioe;
		}
		catch(Exception e)
		{
			log.error("", e);
			send(response, 500, (e.getMessage()  == null) ? "Server Error" : "Server Error: " + e.getMessage());
			// or is it better to throw a servlet exception?  What if there are filters called after this?
		}
	}

	private void send(HttpServletResponse response, int status, String text)
		throws IOException
	{
		PrintWriter out = response.getWriter();
		out.print(text + "\n");
	}

}

