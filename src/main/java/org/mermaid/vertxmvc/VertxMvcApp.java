package org.mermaid.vertxmvc;

import org.mermaid.vertxmvc.converters.DateRequestConverter;
import org.mermaid.vertxmvc.converters.WebDataBinder;

import java.util.Date;

public class VertxMvcApp {

	public static void run() {
		run(null);
	}

	public static void run(VertxMvcAppConfig config) {
		WebDataBinder webDataBinder = new WebDataBinder();
		defaultBinder(webDataBinder);
		if (config != null)
			config.initBinder(webDataBinder);

		new Container() {
			{
				initialization();
				startServer();
			}
		};
	}

	private static void defaultBinder(WebDataBinder webDataBinder) {
		webDataBinder.registerCustomConverter(Date.class,
				new DateRequestConverter());
	}

}
