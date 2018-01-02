package org.mermaid.vertxmvc;

import org.mermaid.vertxmvc.converters.DateRequestConverter;
import org.mermaid.vertxmvc.converters.WebDataBinder;

import java.util.Date;

public class VertxMvcApp {

	public static void run() {
		WebDataBinder.registerCustomConverter(Date.class,
				new DateRequestConverter());

		Container container = new Container();
		container.initialization();
		container.startServer();
	}
}
