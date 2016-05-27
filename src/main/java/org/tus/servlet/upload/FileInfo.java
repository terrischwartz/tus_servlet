package org.tus.servlet.upload;
 

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 

/*
	Todo: 
		- may add owner id field
*/
/*
		- POST method writes this object to disk

		- HEAD and other methods read it from disk. 
			this.offset is not stored in serialized version of this struct, 
			instead it is set by stat'ing the bin file with this.id.
*/
public class FileInfo 
{
	private static final Logger log = LoggerFactory.getLogger(FileInfo.class.getName());
	public long entityLength = -1;
	public String id;
	public long offset = -1;
	public String metadata;

	// This ctor is used by post method to create the FileInfo
	public FileInfo(long entityLength, String metadata)
	{
		this.entityLength = entityLength;
		this.id = UUID.randomUUID().toString();
		this.id = this.id.replace("-", "_");
		this.metadata = metadata;
	}

	// This is used by jackson to deserialize from file
	public FileInfo()
	{
	}

}
