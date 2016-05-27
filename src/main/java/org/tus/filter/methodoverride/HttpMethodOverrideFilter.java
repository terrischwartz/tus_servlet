/**
 * Licensed to the Apache Software Foundation (ASF) under one or more 
 * contributor license agreements.  See the NOTICE file distributed with 
 * this work for additional information regarding copyright ownership.  
 * The ASF licenses this file to you under the Apache License, Version 2.0 
 * (the "License"); you may not use this file except in compliance with 
 * the License.  You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License
 */
package org.tus.filter.methodoverride;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * 
 * @author Filippo De Luca
 * 
 * @version 1.0
 */
public class HttpMethodOverrideFilter implements Filter {

	public static final String HEADER_PARAM = "methodOverrideHeader";

	private String header;

	public void init(FilterConfig filterConfig) throws ServletException {

		header = filterConfig.getInitParameter(HEADER_PARAM);
		if (header == null || header.length() == 0) {
			header = HttpMethodOverrideServletRequest.DEFAULT_HEADER;
		}

	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {

		ServletRequest filteredRequest = request;

		if (request instanceof HttpServletRequest) {
			filteredRequest = processRequest(request);
		}

		chain.doFilter(filteredRequest, response);
	}

	protected ServletRequest processRequest(ServletRequest request) {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		return new HttpMethodOverrideServletRequest(httpRequest, header);
	}

	public void destroy() {
		// Empty
	}

}
