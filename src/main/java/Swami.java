import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Swami {

    public static final Logger log = LoggerFactory.getLogger(Swami.class);
    private static final Map<WatchKey, Path> keys = new ConcurrentHashMap<>();
    private static DataOutputStream dataOUT = null;
    private static DataInputStream dataIN = null;
    WatchService watchService = FileSystems.getDefault().newWatchService();

    public Swami() throws IOException {
    }

    private static void recieveFile(Path filePath) throws IOException {
        int bytes = 0;
        FileOutputStream fileout = new FileOutputStream(filePath.toFile());
        long size = dataIN.readLong();
        byte[] buffer = new byte[4 * 1024];
        while (size > 0
                && (bytes = dataIN.read(
                buffer, 0,
                (int) Math.min(buffer.length, size)))
                != -1) {
            // Here we write the file using write method
            fileout.write(buffer, 0, bytes);
            size -= bytes; // read upto file size
        }
        log.info("FIle recieved");
        fileout.close();
    }


    public static void Master_socket(Path filePath) {
        String ipv4 = networkUtils.FindIp();
        try (ServerSocket servSocket = new ServerSocket(9000)) {
            log.info("master is at port {} ", 9000);
            Socket clientSocket = servSocket.accept();
            log.info("connected.");
            dataIN = new DataInputStream(clientSocket.getInputStream());
            dataOUT = new DataOutputStream(clientSocket.getOutputStream());
            recieveFile(filePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void master(ArrayNode node) throws IOException {

        Path dir = null;

        for (JsonNode Jnode : node) {

            String PathtoSync = Jnode.asText().trim();
            dir = Paths.get(PathtoSync);

            if (!Files.exists(dir) || !Files.isDirectory(dir)) {
                log.warn("Invalid Directory {}", dir);
                continue;
            }

            WatchKey key = dir.register(
                    watchService,
                    StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_DELETE,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.OVERFLOW
            );

            keys.put(key, dir);
        }

        while (true) {
            WatchKey key;

            try {
                key = watchService.take();
            } catch (InterruptedException e) {
                log.error("Interrupt in the main API EXITING!!!");
                break;
            }

            dir = keys.get(key);
            if (dir == null) {
                key.reset();
                continue;
            }

            for (WatchEvent<?> event_master : key.pollEvents()) {
                WatchEvent.Kind<?> kind_master = event_master.kind();

                if (kind_master == StandardWatchEventKinds.OVERFLOW) {
                    continue;
                }

                Path filename_master = (Path) event_master.context();
                Path fullpath_master = dir.resolve(filename_master);

                String EventName_master = kind_master.name();


                if (EventName_master.equals("ENTRY_CREATE")) {
                    log.info("file created  : {}   path: {}", filename_master, fullpath_master);

                }

                if (EventName_master.equals("ENTRY_MODIFY")) {
                    log.info("file modified  : {}   path: {}", filename_master, fullpath_master);

                }

                if (EventName_master.equals("ENTRY_DELETE")) {
                    log.info("file deleted  : {}   path: {}", filename_master, fullpath_master);
                }

                Master_socket(filename_master);
            }

            boolean valid = key.reset();
            if (!valid) {
                keys.remove(key);
                if (keys.isEmpty()) {
                    break;
                }
            }
        }


    }


    public void registerNewPath(String path) throws Exception {

        path = path.trim();
        Path dir = Paths.get(path);

        if (!Files.exists(dir) || !Files.isDirectory(dir)) {
            log.warn("Invalid directory: {} ", dir);
            return;
        }

        WatchKey key = dir.register(
                watchService,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_DELETE
        );

        keys.put(key, dir);
        log.info("Now watching: {}", dir);
    }
}


