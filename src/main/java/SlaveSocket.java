import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;

public class SlaveSocket implements notif {

    public static final Logger log = LoggerFactory.getLogger(SlaveSocket.class);
    private static Socket s = null;
    private static DataInputStream in = null;
    private static DataOutputStream out = null;
    private static volatile String FileToUpdate = null;
    Dasa dasa = new Dasa();

    public SlaveSocket() throws IOException {


    }

    private static void sendFile(String Update) {
        int bytes = 0;
        log.info("printed from socket class ", Update);


    }

    public void client(String addr, int port) {

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
        if (notif == null || notif.isEmpty()) return;
        log.info("from notif", notif);
        FileToUpdate = notif;
    }


}
