package org.tus.servlet.upload;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Composer
{
	private static final Logger log = LoggerFactory.getLogger(Composer.class.getName());

	Datastore datastore;
	Locker locker;
	Map<String, MethodHandler> methodHandlers;


	public Composer(Config config)
	{
		Locker locker = new SingleProcessLocker();
		datastore = new Store(config);
		methodHandlers = new HashMap<String, MethodHandler>();
		methodHandlers.put("OPTIONS", new OptionsHandler(config, locker, datastore));
		methodHandlers.put("HEAD", new HeadHandler(config, locker, datastore));
		methodHandlers.put("POST", new PostHandler(config, locker, datastore));
		methodHandlers.put("PATCH", new PatchHandler(config, locker, datastore));
	}
}
