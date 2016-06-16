package org.tus.filter.auth;

import java.security.Principal;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/*
	Wraps a request with a version that overrides getUserPrincipal()
*/
public class UserRequestWrapper extends HttpServletRequestWrapper 
{
	String user;
	List<String> roles = null;
	HttpServletRequest realRequest;

	public UserRequestWrapper(String user, List<String> roles, HttpServletRequest request) 
	{
		super(request);
		this.user = user;
		this.roles = roles;
		this.realRequest = request;
	}

	@Override
	public boolean isUserInRole(String role) 
	{
		if (roles == null) 
		{
			return this.realRequest.isUserInRole(role);
		}
		return roles.contains(role);
	}

	@Override
	public Principal getUserPrincipal() 
	{
		if (this.user == null) 
		{
			return realRequest.getUserPrincipal();
		}

		// make an anonymous implementation to just return our user
		return new Principal() 
		{
			@Override
			public String getName() 
			{     
				return user;
			}
		};
	}
}
