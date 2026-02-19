import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Swami {

    public static final Logger log = LoggerFactory.getLogger(Swami.class);
    private static final Map<WatchKey, Path> keys = new ConcurrentHashMap<>();
    private static final DataOutputStream dataOUT = null;
    private static final DataInputStream dataIN = null;
    WatchService watchService = FileSystems.getDefault().newWatchService();

    public Swami() throws IOException {
    }


    public void master(List<String> node) throws IOException {

        Path dir = null;

        for (String Jnode : node) {

            String PathtoSync = Jnode.trim();
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

        Path filename_master = null;
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

                filename_master = (Path) event_master.context();
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


    public void registerNewPath(String pathString) {
        String cleanPath = pathString.trim();
        Path dir = Paths.get(cleanPath);

        if (!Files.exists(dir) || !Files.isDirectory(dir)) {
            log.warn("Invalid directory: {}", dir);
            return;
        }

        try {
            WatchKey key = dir.register(
                    watchService,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_DELETE
            );

            keys.put(key, dir);


        } catch (IOException e) {
            log.error("Failed to register path: {}", dir, e);
        }
    }
}


