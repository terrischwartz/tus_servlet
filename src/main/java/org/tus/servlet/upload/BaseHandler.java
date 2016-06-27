package org.tus.servlet.upload;
 

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
public abstract class BaseHandler 
{
	private static final Logger log = LoggerFactory.getLogger(BaseHandler.class.getName());
	Config config;
	Locker locker;
	Datastore datastore;
	HttpServletRequest request;
	Response response;

	public BaseHandler(Composer composer, HttpServletRequest request, Response response)
	{
		this.config = composer.config;
		this.locker = composer.locker;
		this.datastore = composer.datastore;
		this.request = request;
		this.response = response;
	}

	public abstract void go() throws Exception;

	/*
		Returns null if header doesn't exist or isn't a long value.
	*/
	public Long getLongHeader(String header)
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

	/*
		Extract id from URL.  If this returns null it means the URL was invalid.
		Caller should return 404 not found or BAD_REQUEST?.
	*/
	public String getID()
	{
		// Returns everything after the servlet path and before the query string (if any)
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
		String id = matcher.group(1);
		log.debug("file ID is:" + id);
		return id;
	}
}
