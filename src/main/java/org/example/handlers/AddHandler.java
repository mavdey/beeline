package org.example.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.util.Collections;
import java.util.Queue;

public class AddHandler implements HttpHandler {

    Queue<String[]> queue;
    String[] fieldsName;
    File file;
//    BufferedWriter writer;
//    AtomicReference<BufferedWriter> atomicWriter = new AtomicReference<>();

    public AddHandler(Queue<String[]> queue, String[] fieldsName, File file) throws IOException {
        this.queue = queue;
        this.fieldsName = fieldsName;
        this.file = file;
//        this.writer = new BufferedWriter(new FileWriter(this.file, true));
//        this.atomicWriter.set(new BufferedWriter(new FileWriter(this.file, true)));
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()))) {
            String[] s = reader.readLine().split(";");
            queue.add(s);
            writeToFile(String.join(";", s));
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

    private synchronized void writeToFile(String str) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            writer.write(str + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
