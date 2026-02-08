import java.util.Scanner;

public class main {

    static void main(String[] args) throws Exception {

        System.out.println("intiating SAMA...");

        String ipv4 = null;

        while( ipv4  == null ){
            ipv4 = networkUtils.FindIp();
            if(ipv4 == null){
                System.out.println("Couldn't connect to the network...");
                try {
                    Thread.sleep(2000);
                }catch (InterruptedException e){
                    Thread.currentThread().interrupt();
                    System.out.println("interrupted, exiting...");
                    return;
                }
            }
        }

        Appstate update = MemReader.read();

        System.out.println("What's your designation in the network> ?");
        System.out.println("dasa  > 1.");
        System.out.println("swami > 2.");

        Scanner designation = new Scanner(System.in);
        System.out.println("Please enter your designation >>> ");
        int input = Integer.parseInt(designation.nextLine());
        switch (input){
            case 1:
                Dasa slave = new Dasa();
                slave.slave("hello.json");

                break;
            case 2:
                Swami master = new Swami();
                master.master();
                break;
            default:

        }
    }

}
