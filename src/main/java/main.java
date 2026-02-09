import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.util.Scanner;

public class main {

    static void main(String[] args) throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();
        JsonNode jsonNode = mapper.readTree(new File("register.json"));


        System.out.println("intiating SAMA...");

        String ipv4 = null;

        while (ipv4 == null) {
            ipv4 = networkUtils.FindIp();
            if (ipv4 == null) {
                System.out.println("Couldn't connect to the network...");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("interrupted, exiting...");
                    return;
                }
            }
        }
        if (jsonNode.has(ipv4)) {
            String status = jsonNode.get(ipv4).asText();

        } else {

            System.out.println("What's your designation in the network> ?");
            System.out.println("dasa  > 1.");
            System.out.println("swami > 2.");
            node.put(ipv4, "dasa");
        }
        Scanner designation = new Scanner(System.in);
        System.out.print("Please enter your designation >>> ");
        int input = Integer.parseInt(designation.nextLine());

        switch (input) {
            case 1:
                String PathToSync = null ;
                if (jsonNode.has("FIle path: ")){
                    PathToSync = jsonNode.get("File path ").asText();
                    System.out.println("Memeory Exists");
                }

                if (PathToSync == null || PathToSync.isEmpty()){
                    System.out.print("Enter the directory you want to synchronise, ");
                    PathToSync = designation.nextLine();
                    node.put("File path: ", PathToSync);
                }
                mapper.writeValue(new File("register.json"), node);
                Dasa dasa = new Dasa();
                String finalPathToSync = PathToSync;
                Thread access = new Thread(() -> {
                    try {
                        dasa.slave(finalPathToSync);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
                access.start();

                break;
            case 2:
                Swami master = new Swami();
                master.master();
                break;
            default:

        }
    }

}
