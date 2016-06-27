package org.tus.servlet.upload;

import javax.servlet.http.HttpServletRequest;

/*
	A single instance of the  datastore will be created.
*/
public interface Datastore
{
	/*
		Initialize the datastore service
	*/
	public void init(Config config, Locker locker) throws Exception;

	/*
		Return list of extensions supported by this datastore.
	*/
	public String getExtensions();

	/*
		Create binary and auxillary files to start an upload.
	*/
	public void create(FileInfo fi) throws  Exception;

	/*
		Writes up to max bytes, starting at offset, to id's storage, from request.
		Returns the number of bytes written.
	*/
	public long write(HttpServletRequest request, String id, long offset, long max)
		throws Exception;

	/*
		Retrieve FileInfo describing the upload identified by filename.
		Returns null if info or bin file for filename doesn't exist.
	*/
	public FileInfo getFileInfo(String id) throws Exception;

	/*
		Persist FileInfo
	*/
	public void saveFileInfo(FileInfo fileInfo) throws Exception;

	/*
		Delete partial or complete upload in response to a DELETE request.	
	*/
	public void terminate(String id) throws Exception;


	/*
		If there's any work to do when the upload is finished, this is where it's done. 
	*/
	public void finish(String id) throws Exception;

	/*
		Shutdown the datastore service
	*/
	public void destroy() throws Exception;

}
