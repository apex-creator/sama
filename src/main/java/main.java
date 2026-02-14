import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class main {
    public static final Logger log = LoggerFactory.getLogger(main.class);
    static File configFile = new File("config_slave.json");
    static ObjectMapper mapper = new ObjectMapper();

    static ObjectNode config;
    static ArrayNode node = null;
    static Dasa dasa;
    static Swami swami;

    static {
        try {
            dasa = new Dasa();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    String path = null;

    static void main(String[] args) throws Exception {


        Scanner sc = new Scanner(System.in);

        String ipv4 = null;
        String design = null;


        Thread Dasa = new Thread(() -> {
            try {
                // PASS THE STRING, NOT THE NODE
                dasa.slave(node);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        Thread Swami = new Thread(() -> {
            try {
                swami.master(node);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        //checks if config files exist if it doesn't then create it
        if (configFile.exists() && configFile.length() > 0) {
            config = (ObjectNode) mapper.readTree(configFile);
        } else {
            config = mapper.createObjectNode();
        }


        JsonNode existing = config.get("path");
        if (existing != null && existing.isArray()) {
            node = (ArrayNode) existing;
        } else {
            node = mapper.createArrayNode();
            config.set("path", node);
        }


        if (config.hasNonNull("design")) {
            design = config.get("design").asText();
            Dasa.start(); // I changed run() to start() so it doesn't freeze your app!
        } else {
            log.info("please enter your designations in the network....");
            log.info("Dasa(slave)   ==>  1");
            log.info("Swami(master) ==>  2");
            System.out.print("================>  ");
            int des = sc.nextInt();
            sc.nextLine(); // consume newline
            if ((des == 1) || (des == 2)) {
                log.info("thanks.");
                if (des == 1) {
                    config.put("design", "slave");

                    if (node.isEmpty()) {
                        log.info("no directories found ...");
                        System.out.print("Enter the directories you want synchronise...");
                        String path = sc.nextLine();
                        node.add(path);
                        mapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValue(configFile, config);

                        log.info("<<<MEMORY_UPDATED>>>");
                    }
                    Dasa.start();


                }
                if (des == 2) {

                    config.put("design", "master");

                    if (node.isEmpty()) {
                        log.info("no directory found.");
                        System.out.print("please enter directory you have to synchronise...");
                        String path = sc.nextLine();
                        node.add(path);
                        mapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValue(configFile, config);

                        log.info("<<<MEMORY_UPDATED>>>");

                    }
                    Swami.start();
                }
            }


        }
        commandLoop(sc);
    }

    static void commandLoop(Scanner sc) throws Exception {

        intro();
        while (true) {
            System.out.print("SAMA> ");
            String command = sc.nextLine().trim();
            if (!command.equalsIgnoreCase("add") || !command.equalsIgnoreCase("list") || command.equalsIgnoreCase("exit")) {
                System.out.print("INVALID INPUT!!!, PLEASE use ");
                intro();
            }
            if (command.equalsIgnoreCase("add")) {
                log.info("Enter path: ");
                String newPath = sc.nextLine();

                node.add(newPath);
                String design = config.get("design").asText();
                if (design.equalsIgnoreCase("slave")) {
                    dasa.registerNewPath(newPath);
                }
                if (design.equalsIgnoreCase("master")) {
                    swami.registerNewPath(newPath);
                }

                try {
                    mapper
                            .writerWithDefaultPrettyPrinter()
                            .writeValue(configFile, config);

                    log.info("Path added and saved!");

                } catch (Exception e) {
                    log.warn("Failed to save config.");
                }

            } else if (command.equalsIgnoreCase("list")) {
                log.info("Watched Folders:");
                for (JsonNode n : node) {
                    log.info(" - ", n.asText());
                }

            } else if (command.equalsIgnoreCase("exit")) {
                log.info("Shutting down...");
                System.exit(0);
            }
        }
    }

    static void intro() {
        log.info("\n--- --- ---");
        log.info("Type 'add' to watch a new folder.");
        log.info("Type 'list' to see watched folders.");
        log.info("Type 'exit' to stop.");
    }
}