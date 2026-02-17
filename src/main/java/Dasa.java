import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Dasa {

    public static final Logger log = LoggerFactory.getLogger(Dasa.class);
    static String address = "hello";
    static int port = 9000;
    final Map<WatchKey, Path> keys = new ConcurrentHashMap<>();
    private final notif Notif;
    WatchService watchService = FileSystems.getDefault().newWatchService();

    public Dasa() throws IOException {
        notif NOTif = null;
        this.Notif = NOTif;
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

        String notif = null;
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
                String file = String.valueOf((filename));
                Path fullpath = dir.resolve(filename);
                String path = String.valueOf((fullpath));
                String EventName = kind.name();
                notif = (file + fullpath + EventName);

                if (Notif != null) {
                    Notif.notif(notif);

                }
                if (EventName.equals("ENTRY_CREATE")) {
                    log.info("file created  : {}   path: {}", filename, fullpath);

                }

                if (EventName.equals("ENTRY_MODIFY")) {
                    log.info("file modified  : {}   path: {}", filename, fullpath);

                }

                if (EventName.equals("ENTRY_DELETE")) log.info("file deleted  : {}   path: {}", filename, fullpath);

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
