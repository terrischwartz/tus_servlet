package org.tus.filter.auth;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/*
	If authentication is required for uploads and the tus upload servlet is installed in
	an application that requires users to login (instead of being a separate standalone 
	application), this filter can be used to prevent unauthorized users from uploading.  
	If using container based security (or an earlier filter in the chain) that sets the user 
	Principal in the HttpRequest, this filter simply checks that an authenticated user is present.  
	
	However, if you aren't using container based security, and the request.getUserPrincipal()
	returns null even when a user is logged in, you're probably instead storing the logged in
	user's name in the HttpSession.  In that case you'll need to provide an implementation of
	PrincipalManager to get the username from the session and give it to this filter. You'll:
	    * create a class that implements the PrincipalManager interface 
		* set the filter init-param "principalManager.class" in web.xml

	For example:

	Configure In web.xml:
	<filter>
        <filter-name>SessionAuth</filter-name>
        <filter-class>org.tus.filter.auth.SessionAuth</filter-class>
        <init-param>
            <param-name>principalManager.class</param-name>
            <param-value>org.ngbw.restusers.UserBridge</param-value>
        </init-param>
    </filter>
	<filter-mapping>
        <filter-name>SessionAuth</filter-name>
        <servlet-name>upload</servlet-name>
    </filter-mapping>

	TODO: add an optional init param with url to redirect to (instead of sending
	sending an UNAUTHORIZED response.
*/

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionAuth implements Filter 
{
	private static final Logger log = LoggerFactory.getLogger(SessionAuth.class.getName());
	private PrincipalManager principalManager;

    @Override
	public void init(FilterConfig cfg) throws ServletException 
	{
		String classname = cfg.getInitParameter("principalManager.class");
		if (classname == null)
		{
			// The expectation in this case is that the container is providing
			// a request that contains a user Principal object when a user is logged in.
			log.info("filter doesn't have principalManager.class init parameter. Container security must be enabled.");  
			return;
		}
		try
		{
			principalManager = (PrincipalManager)Class.forName(classname).getConstructor().newInstance();
		}
		catch(Exception e)
		{
			log.error("", e);
			throw new ServletException(e);
		}
	}
    
    @Override
    public void destroy() {;}


    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) 
		throws ServletException, IOException 
	{    
		log.debug("In SessionAuth filter");

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        HttpSession session = request.getSession(false);
		String username = null;

		// Using an implementation of PrincipalManager
		if (principalManager != null)
		{
			if (session != null)
			{
				username = principalManager.getUser(session);
			}
			if (username != null)
			{
				request  = new UserRequestWrapper(username, null, request);
			}
		// Using container based security
		} else
		{
			if (request.getUserPrincipal() != null)
			{
				username = request.getUserPrincipal().getName();
			}
		}

		if (username != null)
		{
			chain.doFilter(request, response);
		} else
		{
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		}
		return;
	}
}
