package org.tus.servlet.upload;
 

import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
/*
	Base class for handlers.
*/
public class MethodHandler 
{
	private static final Logger log = LoggerFactory.getLogger(MethodHandler.class.getName());
	Config config;
	Locker locker;
	Datastore datastore;

	public MethodHandler(Config config, Locker locker, Datastore datastore)
	{
		this.config = config;
		this.locker = locker;
		this.datastore = datastore;
	}

	public boolean go(HttpServletRequest request, HttpServletResponse response)
		throws Exception
	{
		String clientVersion = request.getHeader("tus-resumable");
		if (!request.getMethod().equals("OPTIONS") && 
			(clientVersion == null || !clientVersion.equals(config.tusApiVersionSupported)))
		{
			return send(request, response, 412, "Precondition Failed");
		}
		return true;
	}

	public boolean send(HttpServletRequest request, HttpServletResponse response, int status, String body)
		throws IOException
	{
		response.setStatus(status);

		response.addHeader("Tus-Resumable", config.tusApiVersionSupported);
		response.setHeader("X-Content-Type-Options", "nosniff");
		addAccessHeaders(request, response);

		if (request.getMethod().equals("HEAD"))
		{
			body = "";
		}
		if (body.length() > 0)
		{
			body += "\n";
			response.setHeader("Content-Type", "text/plain; charset=utf-8");
		}
		response.setHeader("Content-Length", Long.toString(body.length()));

		write(response, body);

		return true;
	}

	private void addAccessHeaders(HttpServletRequest request, HttpServletResponse response)
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
					"Origin, X-Requested-With, Content-Type, Upload-Length, Upload-Offset, Tus-Resumable, Upload-Metadata");
				response.setHeader("Access-Control-Max-Age", "86400");

			} else
			{
				response.setHeader("Access-Control-Expose-Headers", 
					"Upload-Offset, Location, Upload-Length, Tus-Version, Tus-Resumable, Tus-Max-Size, Tus-Extension, Upload-Metadata");
			}
		}
	}

	/*
		Returns null if header doesn't exist or isn't a long value.
	*/
	public Long getLongHeader(HttpServletRequest request, String header)
	{
		String headerValue = request.getHeader(header);
		if (headerValue == null)
		{
			return null;
		}
		Long value = null;
		try
		{
			value = new  Long(headerValue);
			log.debug("parsed header " + header + "=" + headerValue);
			return value;
		}
		catch(NumberFormatException ne)
		{
			log.debug(ne.toString());
			return null;
		}
	}

	public void write(HttpServletResponse response, String body)
		throws IOException
	{
		PrintWriter out = response.getWriter();
		out.print(body);
	}

	/*
		Extract filename from URL.  If this returns null it means the URL was invalid.
		Caller should return 404 not found.
	*/
	public String getFilename(HttpServletRequest request)
	{
		String pathInfo= request.getPathInfo();
		log.debug("pathInfo is:" + pathInfo );
		if (pathInfo == null)
		{
			return null;
		}
		// Matches file ID consisting of letters, digits and underscores with optional trailing slash
		Pattern pattern = Pattern.compile("/(\\w+)/?");
		Matcher matcher = pattern.matcher(pathInfo);
		if (!matcher.matches())
		{
			log.debug("URL doesn't have form of an upload endpoint."); 
			return null;
		}
		String filename = matcher.group(1);
		log.debug("file ID is:" + filename);
		return filename;
	}
}
