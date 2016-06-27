package org.tus.servlet.upload;
 
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

 
/*
	Simple locking of ids/filenames to prevent threads from
	accessing critical code concurrently.  Locks are not re-entrant. 
	Not persistent.  
	
	You need a different Locker if multiple processes are involved.
*/
public class  SingleProcessLocker implements Locker
{
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(SingleProcessLocker.class.getName());
	protected HashSet<String> pool = new HashSet<String>();

	/* 
		Returns true if able to lock name without waiting.  False if
		name is already locked, even if locked by the same thread.
	*/
	@Override
	public boolean lockUpload(String name) throws Exception
	{
		synchronized(pool)
		{
			if (pool.contains(name))
			{
				return false;
			}
			pool.add(name);
			return true;
		}
	}

	/* 
		Unlocks name.
		Note: name can be unlocked by any thread; calling code must 
		insure that a thread only unlocks names it has locked.
	*/
	@Override
	public void unlockUpload(String name) throws Exception
	{
		synchronized(pool)
		{
			if (pool.contains(name))
			{
				pool.remove(name);
			}
		}
	}
}
