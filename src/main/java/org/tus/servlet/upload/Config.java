package org.tus.servlet.upload;

import java.io.File;
import java.util.Enumeration;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
public class Config
{
	private static final Logger log = LoggerFactory.getLogger(Config.class.getName());

	String tusApiVersionSupported  = "1.0.0";

	String[] tusApiExtensions = {"creation", "termination"};

	//Maximum size of a single upload. 0 means unlimited.
	long MAX_SIZE = 0L;

	//Maximum total storage space to use.  0 means unlimited (or different policy in use). 
	long MAX_STORAGE = 0L;

	//Server can limit number of bytes it will accept in a single patch request.  0 means unlimited.
	long MAX_REQUEST = 0L;

	// Default value. Folder where data will be stored.  Servlet will create the folder if it doesn't exist and
	// parent dir does exist and permissions allow.  
	String UPLOAD_FOLDER = "/tmp";

	public long maxSize;
	public long maxStorage;
	public long maxRequest;
	public String uploadFolder;
	public String datastoreProvider;

	// Derived classes may need additional configuration.
	public Properties allProperties = new Properties(); 


	public Config() throws ServletException 
	{
		this(new Properties());
	}

	public Config(Properties properties) throws ServletException 
	{
		init(properties);
	}

	private void init(Properties properties) throws ServletException
	{
		String tmp;
		Long l; 

		this.allProperties.putAll(properties);

		tmp = properties.getProperty("uploadFolder");
		uploadFolder = (tmp == null)? UPLOAD_FOLDER : tmp;
		validateFolder(uploadFolder);

		l = getLongValue("maxFileSize");
		maxSize = (l == null) ? MAX_SIZE : l;

		l = getLongValue("maxStorage");
		maxStorage = (l == null) ? MAX_STORAGE : l;

		l = getLongValue("maxRequest");
		maxRequest = (l == null) ? MAX_REQUEST : l;

		datastoreProvider = properties.getProperty("datastoreProvider");
		log.info("uploadFolder=" + uploadFolder + ", maxFileSize=" + maxSize + ", maxStorage=" + maxStorage +
			", maxRequest=" + maxRequest + ", datastoreProvider=" + datastoreProvider);
	}


    public Config(ServletConfig sc) throws ServletException 
    {
		Properties properties = new Properties();
		@SuppressWarnings("unchecked")
		Enumeration<String> names =  sc.getInitParameterNames();
		while (names.hasMoreElements())
		{
			String name = names.nextElement();
			String value = sc.getInitParameter(name);
			if (value != null)
			{
				properties.setProperty(name, value); 
			}
		}

		/*
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
        if (sc.getInitParameter("datastoreProvider") != null )
		{
			properties.setProperty("datastoreProvider", sc.getInitParameter("datastoreProvider"));
		}
		*/
		init(properties);
   	} 
	
	protected void validateFolder(String folder) throws ServletException
	{
		String tmp;
		File file = new File(folder);
		if (!file.isDirectory() && !file.mkdir())
		{
			tmp = "Unable to find or create directory " + folder;
			log.error(tmp);
			throw new ServletException(tmp);
		}
		if (!file.canWrite() || !file.canRead())
		{
			tmp = "Upload directory: " + folder + " must be readable and writable";
			log.error(tmp);
			throw new ServletException(tmp);
		}
	}

	public String getStringValue(String name) throws ServletException
	{
		return allProperties.getProperty(name);
	}

	public Long getLongValue(String name) throws ServletException
	{
		String msg;
		String value = allProperties.getProperty(name);
		if (value == null)
		{
			return null;
		}
		Long longValue = null;
		try
		{
			longValue = new  Long(value);
			return longValue;
		}
		catch(NumberFormatException ne)
		{
			msg = "Parameter must be a long.  Error parsing: " + value;
			throw new ServletException(msg);
		}
	}
}
