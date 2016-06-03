package org.tus.servlet.upload;
 
import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
/*
	The servlet_path is configured in servlet mapping in web.xml. 
	If using this servlet in a struts application, in struts.xml tell struts
	to exclude "/servlet_path/[0-9a-zA-Z]*".
	This servlet should only see uris of the form /servlet_path or /servlet_path/123foo , but nothing with
	special characters or deeper paths.  

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
		}
	}

	private void send(HttpServletResponse response, int status, String text)
		throws IOException
	{
		response.setStatus(status);
		PrintWriter out = response.getWriter();
		if (text.length() > 1)
		{
			text += "\n";
		}
		out.print(text);
	}

}

