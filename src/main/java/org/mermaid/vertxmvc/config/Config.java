package org.mermaid.vertxmvc.config;

public class Config {

	private Server server;
	private Scaner scaner;

	public Server getServer() {
		return server;
	}

	public void setServer(Server server) {
		this.server = server;
	}

	public Scaner getScaner() {
		return scaner;
	}

	public void setScaner(Scaner scaner) {
		this.scaner = scaner;
	}

	@Override
	public String toString() {
		return "Config [server=" + server + "]";
	}

}
