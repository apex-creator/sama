import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class main {

    private static final Logger print = LoggerFactory.getLogger(main.class);
    static Dasa dasa;
    static Swami swami;
    static SlaveSocket dosa;


    static {
        try {
            dosa = new SlaveSocket();
            dasa = new Dasa(dosa);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    static {
        try {
            swami = new Swami();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static void main(String[] args) {


        Scanner sc = new Scanner(System.in);
        ConfigManager config = new ConfigManager();
        String newDesign;
        String design = ConfigManager.load("design");
        if (design == null) {
            print.info("configuration not found... ");
            print.info("please enter your designation in the network.");
            do {
                newDesign = sc.nextLine();
                if (!newDesign.equals("dasa") && !newDesign.equals("swami")) {
                    print.info("Invalid input please enter if you are master or slave in the network");
                }
            } while (!newDesign.equals("dasa") && !newDesign.equals("swami"));
            print.info("Updating config");
            config.save("design", newDesign);
            design = newDesign;
        }

        List<String> pathlist = config.LoadPaths();
        if (pathlist.isEmpty()) {
            print.info("please add paths you want to synchronise");
            String newAddition = sc.nextLine();
            config.addPath(newAddition);
        }
        if (design.equalsIgnoreCase("dasa")) {
            Thread slave = new Thread(() -> {
                try {
                    dasa.slave(pathlist);
                } catch (Exception e) {
                    print.info("failure in starter thread, {}", e.getCause());
                }
            });
            slave.start();
        }
    }

}