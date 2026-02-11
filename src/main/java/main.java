import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class main {
    static File configFile = new File("config.json");
    static ObjectMapper mapper = new ObjectMapper();
    static ObjectNode config;
    static ArrayNode node = null;
    String path = null;
    static Dasa dasa;

    static {
        try {
            dasa = new Dasa();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static void main(String[] args) throws Exception {


        Scanner sc = new Scanner(System.in);

        String ipv4 = null;
        String design = null;


        Thread access = new Thread(() -> {
            try {
                // PASS THE STRING, NOT THE NODE
                dasa.slave(node);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });


        //finds IP if fails retries every 2 seconds
        while (ipv4 == null) {
            ipv4 = networkUtils.FindIp();
            if (ipv4 == null) {
                System.out.println("couldnt connect! CHECK YOUR INTERNET CONNECTION");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("Interrupted, exiting...");
                    return;
                }
            }
        }


        //checks if config files exist if it doesn't then create it
        if (configFile.exists() && configFile.length() > 0) {
            config = (ObjectNode) mapper.readTree(configFile);
        } else {
            config = mapper.createObjectNode();
        }


        // LOAD OR CREATE PATH ARRAY
        JsonNode existing = config.get("path");
        if (existing != null && existing.isArray()) {
            node = (ArrayNode) existing;
        } else {
            node = mapper.createArrayNode();
            config.set("path", node);
        }


        if (config.hasNonNull("design")) {
            design = config.get("design").asText();
            access.start(); // I changed run() to start() so it doesn't freeze your app!
        } else {
            System.out.println("please enter your designations in the network....");
            System.out.println("Dasa(slave)   ==>  1");
            System.out.println("Swami(master) ==>  2");
            System.out.print("================>  ");
            int des = sc.nextInt();
            sc.nextLine(); // consume newline
            if ((des == 1) || (des == 2)) {
                System.out.println("thanks.");
                if (des == 1) {
                    config.put("design", "slave");

                    if (node.isEmpty()) {
                        System.out.println("no directories found ...");
                        System.out.println("Enter the directores you want synchronise...");
                        String path = sc.nextLine();
                        node.add(path);
                        mapper.writeValue(configFile, config);
                        System.out.println("<<<MEOMORY_UPDATED>>>");
                    }
                    access.start(); // Changed run() to start() here too


                }
            }




        }
        commandLoop(sc);
    }

    // This runs in the MAIN thread
    static void commandLoop(Scanner sc) throws Exception {
        System.out.println("\n--- --- ---");
        System.out.println("Type 'add' to watch a new folder.");
        System.out.println("Type 'list' to see watched folders.");
        System.out.println("Type 'exit' to stop.");

        while (true) {
            System.out.print("SAMA> ");
            String command = sc.nextLine().trim();

            if (command.equalsIgnoreCase("add")) {
                System.out.print("Enter path: ");
                String newPath = sc.nextLine();

                // Add to memory
                node.add(newPath);
                dasa.registerNewPath(newPath);

                // Save to disk
                try {
                    mapper.writeValue(configFile, config);
                    System.out.println("Path added and saved!");

                } catch (Exception e) {
                    System.out.println("Failed to save config.");
                }

            } else if (command.equalsIgnoreCase("list")) {
                System.out.println("Watched Folders:");
                for (JsonNode n : node) {
                    System.out.println(" - " + n.asText());
                }

            } else if (command.equalsIgnoreCase("exit")) {
                System.out.println("Shutting down...");
                System.exit(0);
            }
        }
    }
}