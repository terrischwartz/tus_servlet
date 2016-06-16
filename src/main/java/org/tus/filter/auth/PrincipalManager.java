package org.tus.filter.auth;

import javax.servlet.http.HttpSession;

public interface PrincipalManager
{
	// Return username of logged in user or null if no one is logged in. 
	public String getUser(HttpSession session);
}
