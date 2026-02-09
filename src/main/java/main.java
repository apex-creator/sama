import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.util.Scanner;

public class main {

    static void main(String[] args) throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();


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

        System.out.println("What's your designation in the network> ?");
        System.out.println("dasa  > 1.");
        System.out.println("swami > 2.");

        Scanner designation = new Scanner(System.in);
        System.out.print("Please enter your designation >>> ");
        int input = Integer.parseInt(designation.nextLine());

        switch (input) {
            case 1:

                System.out.println("Understood >>> " + ipv4 + " >> " + "Dasa(slave).");
                System.out.print("Enter the Directory you want to Sync.>>> ");
                String PathToSync = designation.nextLine();
                String RegisteredPath = PathToSync;

                node.put(ipv4, "dasa");
                node.put("File Path: ", RegisteredPath);
                mapper.writeValue(new File("register.json"), node);
                Dasa dasa = new Dasa();
                Thread access = new Thread(() -> {
                    try {
                        dasa.slave(PathToSync);
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
