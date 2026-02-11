import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Dasa {
    WatchService watchService = FileSystems.getDefault().newWatchService();
    final Map<WatchKey, Path> keys = new ConcurrentHashMap<>();
    public Dasa() throws IOException {
    }

    public void slave(ArrayNode node) throws Exception {

        Path dir = null;



        for (JsonNode Jnode : node) {

            String PathtoSync = Jnode.asText().trim();
             dir = Paths.get(PathtoSync);

            if (!Files.exists(dir) || !Files.isDirectory(dir)) {
                System.out.println("Skipping invalid directory: " + dir);
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

        // EVENT LOOP
        while (true) {
            WatchKey key;

            try {
                key = watchService.take();
            } catch (InterruptedException e) {
                break;
            }

            // ✅ FIX 3: resolve directory from key
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
                    System.out.println("file created : " + filename + " path: " + fullpath);
                }

                if (EventName.equals("ENTRY_MODIFY")) {
                    System.out.println("file modified : " + filename + " path: " + fullpath);
                }

                if (EventName.equals("ENTRY_DELETE")) {
                    System.out.println("file deleted : " + filename + " path: " + fullpath);
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

    public void registerNewPath(String path) throws Exception {

        path = path.trim();
        Path dir = Paths.get(path);

        if (!Files.exists(dir) || !Files.isDirectory(dir)) {
            System.out.println("Invalid directory: " + dir);
            return;
        }

        WatchKey key = dir.register(
                watchService,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_DELETE
        );

        keys.put(key, dir);
        System.out.println("Now watching: " + dir);
    }

}
