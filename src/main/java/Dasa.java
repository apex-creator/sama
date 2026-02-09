import java.nio.file.*;

public class Dasa {

    public void slave(String PathToSync) throws Exception {


        WatchService watchService = FileSystems.getDefault().newWatchService();

        Path DirToWatch = Paths.get(PathToSync);

        DirToWatch.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.OVERFLOW);

        while (true) {
            WatchKey key;

            try {
                key = watchService.take();
            } catch (InterruptedException e) {
                break;
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();

                if (kind == StandardWatchEventKinds.OVERFLOW) {
                    continue;
                }

                Path filename = (Path) event.context();
                Path fullpath = DirToWatch.resolve(filename);
                String EventName = kind.name();
                if (EventName == "ENTRY_CREATE") {
                    System.out.println(" file created : " + filename);
                }
                if (EventName == "ENTRY_MODIFY") {
                    System.out.println(" File modified : " + filename);
                }
                if (EventName == "ENTRY_DELETE") {
                    System.out.println(" file deleted : " + filename);
                }

            }


            boolean valid = key.reset();
            if (!valid) {
                break;
            }
        }


    }

}
