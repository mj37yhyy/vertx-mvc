package org.mermaid.vertxmvc.config;

public class Server {

	private int port;

	private String contextPath;

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getContextPath() {
		return contextPath;
	}

	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}

	@Override
	public String toString() {
		return "Server{" +
				"port=" + port +
				", contextPath='" + contextPath + '\'' +
				'}';
	}
}
