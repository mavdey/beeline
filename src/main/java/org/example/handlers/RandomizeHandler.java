package org.example.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

public class RandomizeHandler implements HttpHandler {

    Queue<String[]> queue;

    public RandomizeHandler(Queue<String[]> queue) {
        this.queue = queue;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Set<String[]> set = new HashSet<>();
        set.addAll(queue);
        queue.clear();
        queue.addAll(set);
        exchange.sendResponseHeaders(200, 0);
        exchange.close();
    }
}
