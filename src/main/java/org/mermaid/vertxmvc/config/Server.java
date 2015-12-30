package org.mermaid.vertxmvc.config;

public class Server {

	private int port;

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	@Override
	public String toString() {
		return "Server [port=" + port + "]";
	}
}
