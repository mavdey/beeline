package org.example;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class Server {
    private final HttpServer server;
//    private final Logger logger = LoggerFactory.getLogger(Server.class);

    public Server(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), Integer.MAX_VALUE);
        server.setExecutor(Executors.newFixedThreadPool(5));
    }

    public void start() {
        server.start();
//        logger.info("Server started at {}", server.getAddress());
    }

    public void stop(int delay) {
        server.stop(delay);
//        logger.info("Server stopped");
    }

    public HttpServer getServer() {
        return server;
    }
}
