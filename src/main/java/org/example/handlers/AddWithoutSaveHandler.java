package org.example.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.util.Collections;
import java.util.Queue;

public class AddWithoutSaveHandler implements HttpHandler {

    Queue<String[]> queue;
    String[] fieldsName;
    File file;


    public AddWithoutSaveHandler(Queue<String[]> queue, String[] fieldsName, File file) {
        this.queue = queue;
        this.fieldsName = fieldsName;
        this.file = file;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()))) {
            String[] s = reader.readLine().split(";");
            queue.add(s);
        } catch (IOException | RuntimeException e) {
            String response = "Error";
            exchange.sendResponseHeaders(400, response.length());

            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.flush();
            os.close();
            return;
        }


        String response = "OK";
        exchange.getResponseHeaders().put("Content-type", Collections.singletonList("application/json"));
        exchange.sendResponseHeaders(200, response.length());

        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.flush();
        os.close();
    }
}
