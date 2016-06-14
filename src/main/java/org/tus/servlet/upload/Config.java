package org.tus.servlet.upload;

import java.io.File;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
 
public class Config
{
	private static final Logger log = LoggerFactory.getLogger(Config.class.getName());

	String tusApiVersionSupported  = "1.0.0";

	String[] tusApiExtensions = {"creation", "termination"};

	// Maximum size of a single upload. 0 means unlimited.
	long MAX_SIZE = 0L;

	// Maximum total storage space to use.  0 means unlimited (or different policy in use). 
	long MAX_STORAGE = 0L;

	// Server can limit number of bytes it will accept in a single patch request.  0 means unlimited.
	long MAX_REQUEST = 0L;

	// Folder where data will be stored.  Must already exist.
	String UPLOAD_FOLDER = "/tmp";

	long maxSize;
	long maxStorage;
	long maxRequest;
	String uploadFolder;


	public Config() throws ServletException 
	{
		this(new Properties());
	}

	public Config(Properties properties) throws ServletException 
	{
		init(properties);
		log.info("uploadFolder=" + uploadFolder + ", maxFileSize=" + maxSize + ", maxStorage=" + maxStorage +
			", maxRequest=" + maxRequest);
	}

	private void init(Properties properties) throws ServletException
	{
		String tmp;
		Long l; 

		tmp = properties.getProperty("uploadFolder");
		uploadFolder = (tmp == null)? UPLOAD_FOLDER : tmp;
		validateFolder(uploadFolder);

		l = getLongValue(properties.getProperty("maxFileSize"));
		maxSize = (l == null) ? MAX_SIZE : l;

		l = getLongValue(properties.getProperty("maxStorage"));
		maxStorage = (l == null) ? MAX_STORAGE : l;

		l = getLongValue(properties.getProperty("maxRequest"));
		maxRequest = (l == null) ? MAX_REQUEST : l;
	}


    public Config(ServletConfig sc) throws ServletException 
    {
		Properties properties = new Properties();
		if (sc.getInitParameter("uploadFolder") != null)
		{
			properties.setProperty("uploadFolder", sc.getInitParameter("uploadFolder"));
		}
        if (sc.getInitParameter("maxFileSize") != null )
		{
			properties.setProperty("maxFileSize", sc.getInitParameter("maxFileSize"));
		}
        if (sc.getInitParameter("maxStorage") != null )
		{
			properties.setProperty("maxStorage", sc.getInitParameter("maxStorage"));
		}
        if (sc.getInitParameter("maxRequest") != null )
		{
			properties.setProperty("maxRequest", sc.getInitParameter("maxRequest"));
		}
		init(properties);
   	} 
	
	void validateFolder(String folder) throws ServletException
	{
		String tmp;
		File file = new File(folder);
		if (!file.isDirectory() || !file.canWrite() || !file.canRead())
		{
			tmp = "Upload directory: " + folder + " must exist and be readable and writable";
			log.error(tmp);
			throw new ServletException(tmp);
		}
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
