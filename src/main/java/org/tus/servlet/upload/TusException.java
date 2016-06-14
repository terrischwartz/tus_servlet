package org.tus.servlet.upload;

import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class TusException extends Exception
{
	private int status;
	private String text;
	public int getStatus() { return this.status; }
	public String getText() { return this.text; }

	public TusException(int status, String text)
	{
		this.status = status;
		this.text = text;
	}

	public static class UnsupportedVersion extends TusException
	{
		public UnsupportedVersion() 
		{
			super(HttpServletResponse.SC_PRECONDITION_FAILED, "unsupported version");
		}
	}
	public static class MaxSizeExceeded extends TusException
	{
		public MaxSizeExceeded()
		{
			super(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, "maximum size exceeded");
		}
	}
	public static class InvalidContentType extends TusException
	{
		public InvalidContentType()
		{
			super(HttpServletResponse.SC_BAD_REQUEST, "missing or invalid Content-Type header");
		}
	}
	public static class InvalidContentLength extends TusException
	{
		public InvalidContentLength()
		{
			super(HttpServletResponse.SC_BAD_REQUEST, "missing or invalid Content-Length header");
		}
	}
	public static class InvalidUploadLength extends TusException
	{
		public InvalidUploadLength()
		{
			super(HttpServletResponse.SC_BAD_REQUEST, "missing or invalid Upload-Length header");
		}
	}
	public static class InvalidOffset extends TusException
	{
		public InvalidOffset()
		{
			super(HttpServletResponse.SC_BAD_REQUEST, "missing or invalid Upload-Offset header");
		}
	}
	public static class NotFound extends TusException
	{
		public NotFound()
		{
			super(HttpServletResponse.SC_NOT_FOUND, "upload not found");
		}
	}
	public static class FileLocked extends TusException
	{
		public FileLocked()
		{
			// 423 is LOCKED (WebDAV rfc 4918)
			super(423, "file currently locked");
		}
	}
	public static class MismatchOffset extends TusException
	{
		public MismatchOffset()
		{
			super(HttpServletResponse.SC_CONFLICT, "mismatched offset");
		}
	}
	public static class SizeExceeded extends TusException
	{
		public SizeExceeded()
		{
			super(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, "resource's size exceeded");
		}
	}
	public static class NotImplemented extends TusException
	{
		public NotImplemented()
		{
			super(HttpServletResponse.SC_NOT_IMPLEMENTED, "feature not implemented");
		}
	}
	public static class UploadNotFinished extends TusException
	{
		public UploadNotFinished()
		{
			super(HttpServletResponse.SC_BAD_REQUEST, "one of the partial uploads is not finished");
		}
	}
	public static class InvalidConcat extends TusException
	{
		public InvalidConcat()
		{
			super(HttpServletResponse.SC_BAD_REQUEST, "invalid Upload-Concat header");
		}
	}
	public static class ModifyFinal extends TusException
	{
		public ModifyFinal()
		{
			super(HttpServletResponse.SC_FORBIDDEN, "modifying a final upload is not allowed");
		}
	}
	public static class MethodNotAllowed extends TusException
	{
		public MethodNotAllowed()
		{
			super(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "method not allowed");
		}
	}
}
