package org.tus.servlet.upload;
 

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import javax.servlet.http.HttpServletRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
class Store implements Datastore
{
	private static final Logger log = LoggerFactory.getLogger(Store.class.getName());

	private final String binPath;
	private final String infoPath;
	private final long maxRequest;
	
	private Config config;

	/*
		The creation extension has an optional feature that we don't yet support.
		When we do we'll add creation-defer-length to the list of extensions. 
	*/
	private static String extensions = "creation,expiration";


	Store(Config config)
	{
		this.config = config;
		binPath = config.uploadFolder;
		infoPath = config.uploadFolder;
		maxRequest = config.maxRequest;
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
	public long write(HttpServletRequest request, String filename, long offset, long max)
		throws Exception
	{
		String pathname  = getBinPath(filename);
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
		RandomAccessFile raf = new RandomAccessFile(getBinPath(filename), "rw"); // throws if file doesn't exist
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
		Returns null if info or bin file doesn't exist.
	*/
	@Override
	public FileInfo getFileInfo(String filename)
		throws Exception
	{
		File ifile = new File(getInfoPath(filename));
		File bfile = new File(getBinPath(filename));

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
		File file = new File(getInfoPath(fileInfo.id));
		ObjectMapper mapper = new ObjectMapper();
		mapper.writeValue(file, fileInfo); 
		
	}

	private String getBinPath(String filename)
	{
		return binPath + File.separator + filename + ".bin";
	}
	
	private String getInfoPath(String filename)
	{
		return infoPath + File.separator + filename + ".info";
	}







}
