package org.example;

import org.example.handlers.AddHandler;
import org.example.handlers.DeleteHandler;
import org.example.handlers.GetHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class Main {
    static Map<String, Queue<String[]>> queueMap = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        int port = 8008;
        String filename = "C:\\Users\\avdee\\Desktop\\TrueConf\\log_pass_stsload.csv";
        String delimiter = ";";

        switch (args.length) {
            case 2:
                delimiter = args[1];
            case 1:
                port = Integer.parseInt(args[0]);
        }
        Server server = new Server(port);
        Map<String, String[]> fileFieldsMap = new HashMap<>();
        if (args.length > 2) {
            for (int i = 2; i < args.length; i++) {
                File file = new File(args[i]);
                init(file, delimiter, fileFieldsMap);
                String name = file.getName().split("\\.")[0];
                server.getServer().createContext("/get/%s".formatted(name), new GetHandler(queueMap.get(name),
                        fileFieldsMap.get(name)));
                server.getServer().createContext("/add/%s".formatted(name), new AddHandler(queueMap.get(name),
                        fileFieldsMap.get(name), file));
                server.getServer().createContext("/delete/%s".formatted(name), new DeleteHandler(queueMap.get(name),
                        fileFieldsMap.get(name)));
            }
        } else {
            File file = new File(filename);
            init(file, delimiter, fileFieldsMap);
            String name = file.getName().split("\\.")[0];
            server.getServer().createContext("/get/%s".formatted(name), new GetHandler(queueMap.get(name),
                    fileFieldsMap.get(name)));
        }
        server.start();
    }

    private static void init(File file, String delimiter, Map<String, String[]> fileFieldsMap) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String s;
            String name = file.getName().split("\\.")[0];
            Queue<String[]> queue = new LinkedBlockingQueue<>();
            queueMap.put(name, queue);
            s = reader.readLine();
            fileFieldsMap.put(name, s.split(delimiter));
            while ((s = reader.readLine()) != null && !s.equals("")) {
                String[] split = s.split(delimiter);
                queue.add(split);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}