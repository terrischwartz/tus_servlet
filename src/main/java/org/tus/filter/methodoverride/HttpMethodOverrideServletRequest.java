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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * @author Filippo De Luca
 * 
 * @version 1.0
 */
public class HttpMethodOverrideServletRequest extends HttpServletRequestWrapper {

	public static final String DEFAULT_HEADER = "X-HTTP-Method-Override";

	private final String methodOverrideHeader;

	private transient String method;

	/**
	 * @param request
	 */
	public HttpMethodOverrideServletRequest(
			HttpServletRequest request, String methodOverrideHeader) {
		super(request);
		this.methodOverrideHeader = methodOverrideHeader;
	}

	@Override
	public String getMethod() {

		if (method == null) {
			method = resolveMethod();
		}

		return method;
	}

	protected String resolveMethod() {

		String headerValue = getHeader(methodOverrideHeader);

		if (headerValue != null) {
			return headerValue;
		} else {
			return super.getMethod();
		}
	}

}
