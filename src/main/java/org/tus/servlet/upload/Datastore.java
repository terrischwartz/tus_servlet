package org.tus.servlet.upload;

import javax.servlet.http.HttpServletRequest;

interface Datastore
{

	/*
		Return list of extensions supported by this datastore.
	*/
	public String getExtensions();

	/*
		Create binary and auxillary files to start an upload.
	*/
	public void create(FileInfo fi) throws  Exception;

	/*
		Writes up to max bytes, starting at offset, to filename, from request.
		Returns the number of bytes written.
	*/
	public long write(HttpServletRequest request, String filename, long offset, long max)
		throws Exception;

	/*
		Retrieve FileInfo describing the upload identified by filename.
		Returns null if info or bin file for filename doesn't exist.
	*/
	public FileInfo getFileInfo(String filename) throws Exception;

	/*
		Persist FileInfo
	*/
	public void saveFileInfo(FileInfo fileInfo) throws Exception;

}
