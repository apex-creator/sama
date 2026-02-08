import java.nio.file.*;
import java.sql.Time;

public class Dasa {

    public void  slave(String fileName) throws Exception{

        Path dir  = Paths.get(".");
        WatchService  watcher = FileSystems.getDefault().newWatchService();

        dir.register(
                watcher,
                StandardWatchEventKinds.ENTRY_MODIFY
        );

        while (true){
            WatchKey key  =watcher.take();

            for(WatchEvent<?> event : key.pollEvents()){
                Path changed = (Path)event.context();
                if(changed.toString().equals(fileName)){
                    long TimeInstance = System.currentTimeMillis();
                    Appstate state = MemReader.read();
                    System.out.println(fileName +" was modifiled " + TimeInstance + state.UpdatedFilepath );

                    state.lastUpdated = TimeInstance;
                    MemWriter.write(state);

                }

            }

            key.reset();
        }

    }

}
