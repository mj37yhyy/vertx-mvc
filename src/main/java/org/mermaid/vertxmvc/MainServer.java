package org.mermaid.vertxmvc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MainServer {

	public static void main(String[] args) {
		Container container = new Container();
		container.initialization();
		container.startServer();
	}

	Logger logger = LogManager.getLogger(getClass());
}
