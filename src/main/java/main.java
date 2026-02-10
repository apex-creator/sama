import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.util.Scanner;

public class main {
    static File configFile = new File("config.json");
    static ObjectMapper mapper = new ObjectMapper();
    static ObjectNode config;
    static ArrayNode node = null;
    String path = null;


    static void main(String[] args) throws Exception {


        Scanner sc = new Scanner(System.in);

        String ipv4 = null;
        String design = null;


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


                    Dasa dasa = new Dasa();
                    Thread access = new Thread(() -> {
                        try {
                            // PASS THE STRING, NOT THE NODE
                            dasa.slave(node.get(0).asText());
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
                    access.start();
                }
            }


        }

    }

    static void Path() {
        Thread newENT = new Thread();
        Scanner sc = new Scanner(System.in);
        System.out.println("PLease enter additional paths...");
        String newPaths = sc.nextLine();
        node.add(newPaths);

    }

}
