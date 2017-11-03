package org.mermaid.vertxmvc;

public class MainServer {

    public static void main(String[] args) {
        Container container = new Container();
        container.initialization();
        container.startServer();
    }

}
