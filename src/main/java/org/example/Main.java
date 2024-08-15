package org.example;

import org.apache.commons.cli.*;
import org.example.handlers.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class Main {
    private final static Logger logger = LoggerFactory.getLogger(Main.class);
    static Map<String, Queue<String[]>> queueMap = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        final String DEFAULT_DELIMITER = ";";

        Options options = new Options();
        Option portOption = new Option("p", "port", true, "Listening port");
        Option folderOption = new Option("f", "folder", true, "Folder with queues");
        Option fileOption = new Option("F", "file", true, "File with queue");
        Option delimiterOption = new Option("d", "delimiter", true, "Delimiter");

        portOption.setRequired(true);
        folderOption.setRequired(false);
        fileOption.setRequired(false);
        delimiterOption.setRequired(false);

        options.addOption(portOption);
        options.addOption(folderOption);
        options.addOption(fileOption);
        options.addOption(delimiterOption);

        CommandLineParser commandLineParser = new DefaultParser();
        HelpFormatter helpFormatter = new HelpFormatter();

        CommandLine commandLine = null;

        try {
            commandLine = commandLineParser.parse(options, args);
        } catch (ParseException e) {
            logger.error(e.getMessage());
            helpFormatter.printHelp("utility-name", options);
            System.exit(1);
        }
        if (!commandLine.hasOption(fileOption) && !commandLine.hasOption(folderOption)) {
            logger.error("Missing options f or F");
            helpFormatter.printHelp("utility-name", options);
            System.exit(1);
        }
        int port = Integer.parseInt(commandLine.getOptionValue(portOption));
        String delimiter;
        if (commandLine.hasOption(delimiterOption)) {
            delimiter = commandLine.getOptionValue(delimiterOption);
        } else {
            delimiter = DEFAULT_DELIMITER;
        }

        final Server server = new Server(port);
        Map<String, String[]> fileFieldsMap = new HashMap<>();

        try {
            if (commandLine.hasOption(folderOption)) {
                if (Files.isDirectory(Paths.get(new URI("file://" + commandLine.getOptionValue(folderOption))))) {
                    final String finalDelimiter = delimiter;
                    Files.list(Path.of(commandLine.getOptionValue(folderOption))).forEach(path -> {
                        File file = path.toFile();
                        init(file, finalDelimiter, fileFieldsMap);
                        String name = file.getName().split("\\.")[0];
                        server.getServer().createContext("/get/%s".formatted(name), new GetHandler(queueMap.get(name),
                                fileFieldsMap.get(name)));
                        server.getServer().createContext("/add/%s".formatted(name), new AddHandler(queueMap.get(name),
                                fileFieldsMap.get(name), file));
                        server.getServer().createContext("/delete/%s".formatted(name), new DeleteHandler(queueMap.get(name),
                                fileFieldsMap.get(name), file));
                        server.getServer().createContext("/add-without-save/%s".formatted(name), new AddWithoutSaveHandler(queueMap.get(name),
                                fileFieldsMap.get(name), file));
                        server.getServer().createContext("/randomize/%s".formatted(name), new RandomizeHandler(queueMap.get(name)));
                        logger.info("File {} added", file.getName());
                    });
                }
            }
            if (commandLine.hasOption(fileOption)) {
                File file = new File(commandLine.getOptionValue(fileOption));
                init(file, delimiter, fileFieldsMap);
                String name = file.getName().split("\\.")[0];
                server.getServer().createContext("/get/%s".formatted(name), new GetHandler(queueMap.get(name),
                        fileFieldsMap.get(name)));
                server.getServer().createContext("/add/%s".formatted(name), new AddHandler(queueMap.get(name),
                        fileFieldsMap.get(name), file));
                server.getServer().createContext("/delete/%s".formatted(name), new DeleteHandler(queueMap.get(name),
                        fileFieldsMap.get(name), file));
                server.getServer().createContext("/add-without-save/%s".formatted(name), new AddWithoutSaveHandler(queueMap.get(name),
                        fileFieldsMap.get(name), file));
                server.getServer().createContext("/randomize/%s".formatted(name), new RandomizeHandler(queueMap.get(name)));
                logger.info("File {} added", file.getName());
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
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
            while ((s = reader.readLine()) != null && !s.isEmpty()) {
                String[] split = s.split(delimiter);
                queue.add(split);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}