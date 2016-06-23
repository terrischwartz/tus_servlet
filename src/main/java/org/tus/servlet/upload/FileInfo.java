package org.tus.servlet.upload;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 

/*
	Todo: 
		- how does jackson marshal fields with null values?
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
	public String suggestedFilename;
	public String username;
	public Map<String, String> decodedMetadata;

	// This ctor is used by post method to create the FileInfo
	public FileInfo(long entityLength, String metadata, String username)
	{
		this.entityLength = entityLength;
		this.id = UUID.randomUUID().toString();
		this.id = this.id.replace("-", "_");
		this.metadata = metadata;
		this.decodedMetadata = parseMetadata(metadata);
		this.username = username;

		// See if client sent suggested filename in metadata and log it.
		this.suggestedFilename = decodedMetadata.get("filename");
		log.debug("New file ID = " + id + ", filename=" + suggestedFilename); 
	}

	// This is used by jackson to deserialize from file
	public FileInfo()
	{
	}

	/*
		Metadata is transmitted as comma separated key/value pairs, where
		key and value are separated by a space and value is base64 encoded.
		TODO: not sure if it's url safe, mime safe or std base64.  Currently
		using url safe.

	*/
	Map<String, String> parseMetadata(String metadata)
	{
		HashMap<String,String> map = new HashMap<String, String>();
		if (metadata == null)
		{
			return map;
		}
		String[] pairs = metadata.split(",");
		for (int i = 0; i < pairs.length; i++)
		{
			String[] element = pairs[i].trim().split(" ");
			if (element.length != 2)
			{
				log.warn("Ignoring metadata element:'" + pairs[i] + "'");
				continue;
			}
			String key = element[0];
			byte[] value;
			try
			{
				value = Base64.getUrlDecoder().decode(element[1]);
			} 
			catch(IllegalArgumentException iae)
			{
				log.warn("Invalid encoding of metadata element:'" + pairs[i] + "'");
				continue;
			}
			map.put(key, new String(value));
		}
		return map;
	}
}

