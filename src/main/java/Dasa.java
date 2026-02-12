import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

import java.io.IOException;
import java.net.Socket;
import java.nio.file.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Dasa {

    public static final Logger log = LoggerFactory.getLogger(Dasa.class);
    private static final DataOutputStream dataOutputStream = null;
    private static final DataInputStream dataInputStream = null;
    final Map<WatchKey, Path> keys = new ConcurrentHashMap<>();
    WatchService watchService = FileSystems.getDefault().newWatchService();

    public Dasa() throws IOException {
    }

    private static void sendFIle(Path filepath) throws IOException {
        int bytes = 0;

        File file = filepath.toFile();

        FileInputStream fileIN = new FileInputStream(file);

        dataOutputStream.writeLong(file.length());

        byte[] buffer = new byte[4 * 1024];
        while ((bytes = fileIN.read(buffer)) != -1) {
            dataOutputStream.write(buffer, 0, bytes);
            dataOutputStream.flush();
        }
        fileIN.close();

    }

    public void slave_socket(Path filepath) {
        DataOutputStream dataOUT = null;
        DataInputStream dataIN = null;
        String ipv4 = networkUtils.FindIp();
        try (Socket socket = new Socket(ipv4, 9000)) {
            dataIN = new DataInputStream(socket.getInputStream());
            dataOUT = new DataOutputStream(socket.getOutputStream());
            log.info("initiating Synchronisation.");
            sendFIle(filepath);
        } catch (Exception e) {
            log.error("Socketing FAILED.", e);
        }
    }

    public void slave(ArrayNode node) throws Exception {

        Path dir = null;


        for (JsonNode Jnode : node) {

            String PathtoSync = Jnode.asText().trim();
            dir = Paths.get(PathtoSync);

            if (!Files.exists(dir) || !Files.isDirectory(dir)) {
                log.warn("Invalid Directory ", dir);
                continue;
            }

            WatchKey key = dir.register(
                    watchService,
                    StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE,
                    StandardWatchEventKinds.OVERFLOW
            );

            keys.put(key, dir);
        }
        while (true) {
            WatchKey key;

            try {

                key = watchService.take();
            } catch (InterruptedException e) {
                break;
            }

            dir = keys.get(key);
            if (dir == null) {
                key.reset();
                continue;
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();

                if (kind == StandardWatchEventKinds.OVERFLOW) {
                    continue;
                }

                Path filename = (Path) event.context();
                Path fullpath = dir.resolve(filename);

                String EventName = kind.name();

                if (EventName.equals("ENTRY_CREATE")) {
                    log.info("file created  : {}   path: {}", filename, fullpath);

                }

                if (EventName.equals("ENTRY_MODIFY")) {
                    log.info("file modified  : {}   path: {}", filename, fullpath);

                }

                if (EventName.equals("ENTRY_DELETE")) {
                    log.info("file deleted  : {}   path: {}", filename, fullpath);
                }

                slave_socket(fullpath);
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
            log.warn("Invalid directory: ", dir);
            return;
        }

        WatchKey key = dir.register(
                watchService,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_DELETE
        );

        keys.put(key, dir);
        log.info("Now watching: ", dir);
    }


}