package org.tus.servlet.upload;
 

import java.io.File;
import java.io.FilenameFilter;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
 
/*
	Different types of stores may be created by extending this store, hence
	the "protected" declarations.
*/
public class Store implements Datastore
{
	protected static final Logger log = LoggerFactory.getLogger(Store.class.getName());

	protected String binPath;
	protected String infoPath;
	protected long maxRequest;
	protected Locker locker;
	
	protected static String extensions = "creation,termination";


	

	@Override
	public void init(Config config, Locker locker) throws Exception
	{
		this.binPath = config.uploadFolder;
		this.infoPath = config.uploadFolder;
		this.maxRequest = config.maxRequest;
		this.locker = locker;
	}

	@Override
	public void destroy() throws Exception
	{
	}


	@Override
	public String getExtensions()
	{
		return extensions;
	}


	// Called for a new upload
	@Override
	public void create(FileInfo fi)
		throws Exception
	{
		// Save the fileInfo on disk.
		saveFileInfo(fi);	
		
		// Create an empty file for the binary data, on disk.
		String pathname  = getBinPath(fi.id);
		File file = new File(pathname );

		// It shouldn't be possible for the file to already exist, so failure isn't expected here.
		boolean created = file.createNewFile();

		if (!created)
		{
			throw new Exception("File " + pathname + " already exists.");
		}

		log.debug("created " + file.getCanonicalPath());
	}

	/*
		Returns number of bytes written. 
		TODO: 
			- concurrency issue: make sure only one thread writing to this file 
			- security: what if someone else gets our file id and writes to it here?
			- does tus allow out of order chunks?  Multiple concurrent writes to same file?
				That's not handled by this code.
				From protocol spec, sounds like since we aren't implementing concatentation
				extension, offset in request must match file offset, or return 409 Conflict
				status.  Let PatchHander check that and return 409 status.
			- how does the web app using this servlet know when the upload is complete?  We have
			this FileInfo object with the expected length.  Shouldn't we be persisting that
			and using it?


	*/
	@Override
	public long write(HttpServletRequest request, String id, long offset, long max)
		throws Exception
	{
		String pathname  = getBinPath(id);
		File file = new File(pathname );
		if (!file.exists())
		{
			log.warn("File " + pathname + " doesn't exist.");
			throw new Exception("File " + pathname + " doesn't exist.");
		}
		if (!file.canRead() || !file.canWrite() || !file.isFile())
		{
			log.error("File " + pathname + " has permissions problem or is not a regular file.");
			throw new Exception("File " + pathname + " has permissions problem or is not a regular file.");
		}
		// TODO: check that file offset matches request offset.

		long transferred = 0L;
		RandomAccessFile raf = new RandomAccessFile(getBinPath(id), "rw"); // throws if file doesn't exist
		FileChannel dest = raf.getChannel();

		if (maxRequest > 0L && maxRequest < max)
		{
			max = maxRequest;
		}

		/*
			TODO: Is the source blocking or async?  This will only work if it blocks until data is available.
			BUT blocking isn't so great if client loses network connectivity and we block indefnitely because
			he hasn't closed the tcp connection.  Is there a way to set a timeout so we can close the connection?

			fyi: request.getInputStream is returning a ServletInputStream
		*/	
		ReadableByteChannel rbc = Channels.newChannel(request.getInputStream());

		try
		{
			log.debug("Calling FileChannel.transferFrom ...");
			transferred = dest.transferFrom(rbc, offset, max);
			log.debug("Transferred " + transferred + " bytes." );
			return transferred;
		}
		catch(Exception e)
		{
			log.error("", e);
			throw e;
		}
		finally
		{
			if (dest != null)
			{
				dest.close();
			}
			if (raf != null)
			{
				raf.close();
			}
			log.debug("finished with write");
		}
	}

	/*
		Remove partial or complete upload.
		TODO: periodically look for bfiles w/o corresponding ifile and delete them
		for any cases where we've crashed between delete if ifile and bfile.
	*/
	@Override
	public void terminate(String id) throws Exception
	{
		new File(getInfoPath(id)).delete();
		new File(getBinPath(id)).delete();
	}

	/*
		Returns null if info or bin file doesn't exist.
	*/
	@Override
	public FileInfo getFileInfo(String id)
		throws Exception
	{
		File ifile = new File(getInfoPath(id));
		File bfile = new File(getBinPath(id));

		if (!ifile.exists() || !bfile.exists())
		{
			return null;
		}

		ObjectMapper mapper = new ObjectMapper();
		FileInfo fileInfo = mapper.readValue(ifile, FileInfo.class);

		fileInfo.offset =  bfile.length();
		return fileInfo;
	}

	@Override
	public void saveFileInfo(FileInfo fileInfo)
		throws Exception
	{
		// to avoid confusion, never store the offset on disk.  We always
		// get it by stat'ing the .bin file.
		fileInfo.offset = -1;

		File file = new File(getInfoPath(fileInfo.id));
		ObjectMapper mapper = new ObjectMapper();
		mapper.writeValue(file, fileInfo); 
		
	}

	@Override
	public void finish(String id) throws Exception { ; } 

	protected String getBinPath(String id)
	{
		return binPath + File.separator + id + ".bin";
	}
	
	protected String getInfoPath(String id)
	{
		return infoPath + File.separator + id + ".info";
	}

	// Given full path of .bin or .info file, return the corresponding ID
	protected String getIDFromFilename(String filename)
	{
		String name = (new File(filename)).getName();
		int pos = name.lastIndexOf(".");
		if (pos > 0)
		{
			name = name.substring(0, pos);
		}
		return name;
	}

	public List<FileInfo> getAllUploads() throws Exception
	{
		// Get the names of all the .info files in the upload info dir
		File dir = new File(infoPath);
		File [] files = dir.listFiles(new FilenameFilter() 
		{
			@Override
				public boolean accept(File dir, String name) 
				{
					return name.endsWith(".info");
				}
		});

		// Read the .info files and return them in a list of fileInfos.
		ArrayList<FileInfo> fiList = new ArrayList<FileInfo>();
		for (File file : files)
		{
			FileInfo fi = getFileInfo(getIDFromFilename(file.getAbsolutePath()));
			if (fi != null)
			{
				fiList.add(fi);
			} 
		}
		return fiList;
	}

	public List<FileInfo> getCompletedUploads() throws Exception
	{
		List<FileInfo> fiList = getAllUploads();
		Iterator<FileInfo> it = fiList.iterator();
		while(it.hasNext())
		{
			FileInfo fileInfo = it.next();
			if (fileInfo.offset < fileInfo.entityLength) 
			{
				it.remove();
			}
		}
		return fiList;
	}

}
