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
	long MAX_SIZE = 0L;

	// Maximum total storage space to use.  0 means unlimited (or different policy in use). 
	long MAX_STORAGE = 0L;

	// Folder where data will be stored.  Must already exist.
	String UPLOAD_FOLDER = "/tmp";

	long maxSize;
	long maxStorage;
	String uploadFolder;


	public Config(ServletConfig sc) throws ServletException
	{
		String tmp;
		Long l; 

		uploadFolder = sc.getInitParameter("uploadFolder");
		if (uploadFolder == null)
		{
			uploadFolder = UPLOAD_FOLDER;
		}
		File file = new File(uploadFolder);
		if (!file.isDirectory() || !file.canWrite() || !file.canRead())
		{
			tmp = "Upload directory: " + uploadFolder + " must exist and be readable and writable";
			log.error(tmp);
			throw new ServletException(tmp);
		}

		l = getLongValue(sc.getInitParameter("maxFileSize"));
		maxSize = (l == null) ? MAX_SIZE : l;

		l = getLongValue(sc.getInitParameter("maxStorage"));
		maxStorage = (l == null) ? MAX_STORAGE : l;

		log.info("uploadFolder=" + uploadFolder + ", maxFileSize=" + maxSize + ", maxStorage=" + maxStorage);
	}

	public static Long getLongValue(String text) throws ServletException
	{
		String msg;
		if (text == null)
		{
			return null;
		}
		Long value = null;
		try
		{
			value = new  Long(text);
			return value;
		}
		catch(NumberFormatException ne)
		{
			msg = "Parameter must be a long.  Error parsing: " + text;
			throw new ServletException(msg);
		}
	}
}
