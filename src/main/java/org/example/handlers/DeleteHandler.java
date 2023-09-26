package org.example.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Queue;

public class DeleteHandler implements HttpHandler {

    Queue<String[]> queue;
    String[] fieldsName;

    public DeleteHandler(Queue<String[]> queue, String[] fieldsName) {
        this.queue = queue;
        this.fieldsName = fieldsName;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String[] entity = queue.poll();
        String response;
        if (entity != null) {
            StringBuilder builder = new StringBuilder("{");

            for (int i = 0; i < entity.length; i++) {
                builder.append("""
                        
                        "%s":"%s" """.formatted(fieldsName[i], entity[i]));
                if (i < entity.length - 1) {
                    builder.append(",");
                }
            }

            builder.append("""
                    
                    }""");
            response = builder.toString();
            exchange.getResponseHeaders().put("Content-type", Collections.singletonList("application/json"));
            exchange.sendResponseHeaders(200, response.length());
        } else {
            response = "Error. Entity is null";
            exchange.sendResponseHeaders(400, response.length());
        }
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.flush();
        os.close();
    }
}
