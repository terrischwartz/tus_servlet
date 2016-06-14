package org.tus.servlet.upload;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Composer
{
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(Composer.class.getName());

	Config config;
	Datastore datastore;
	Locker locker;


	public Composer(Config config)
	{
		this.config = config;

		locker = new SingleProcessLocker();
		datastore = new Store(config);
	}
}
