package org.mermaid.vertxmvc;

public class VertxMvcApp {

	public static void run() {
		Container container = new Container();
		container.initialization();
		container.startServer();
	}
}
