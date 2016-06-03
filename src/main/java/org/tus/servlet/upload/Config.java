package org.tus.servlet.upload;
 

import java.io.File;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
public class Config
{
	private static final Logger log = LoggerFactory.getLogger(Config.class.getName());

	String tusApiVersionSupported  = "1.0.0";

	String[] tusApiExtensions = {"creation", "termination"};

	// Maximum size of a single upload. 0 means unlimited.
	long maxSize = 0L;

	// Folder where data will be stored.  Must already exist.
	String uploadFolder = "/tmp";


	/*
		TODO: override default values with those from servlet config
		Call getInitParameter() to get parameters from web.xml
	*/
	public Config(ServletConfig sc) throws ServletException
	{
		String msg;
		File file = new File(uploadFolder);
		if (!file.isDirectory() || !file.canWrite() || !file.canRead())
		{
			msg = "Upload directory: " + uploadFolder + " must exist and be readable and writable";
			log.error(msg);
			throw new ServletException(msg);
		}
	}
}
