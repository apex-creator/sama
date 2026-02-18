import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class SlaveSocket implements notif {

    public static final Logger log = LoggerFactory.getLogger(SlaveSocket.class);
    private static String FileToUpdate = null;
    private static Socket s = null;
    private static DataInputStream in = null;
    private static DataOutputStream out = null;


    public SlaveSocket() throws IOException {


    }

    private static void sendFile(String Update) {
        int bytes = 0;


    }


    public void client(String addr, int port) throws InterruptedException {
        log.info("IP address, {}", addr);
        log.info("port, {}", port);
        try {

            s = new Socket(addr, port);
            in = new DataInputStream(s.getInputStream());
            out = new DataOutputStream(s.getOutputStream());
        } catch (Exception e) {
            log.error(String.valueOf(e));
        }

        sendFile(FileToUpdate);

    }

    @Override
    public void notif(String notif) {
        if (notif == null || notif.length() <= 13) return;

        String EventName = notif.substring(0, 12);

        String rest = notif.substring(13);
        String[] parts = rest.split(" ", 2);

        if (parts.length < 2) return;

        String fileName = parts[0];
        String fullPath = parts[1];

        log.info("Event: {}", EventName);
        log.info("File: {}", fileName);
        log.info("Path: {}", fullPath);

        FileToUpdate = notif;
    }


}
