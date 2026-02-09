import java.nio.file.*;
import java.sql.Time;

public class Dasa {

    public void  slave(String fileName) throws Exception{

       WatchService watchService = FileSystems.getDefault().newWatchService();

       Path DirToWatch = Paths.get("/home/Rappy/sama/src/main/java/test_dir");

       DirToWatch.register(watchService,StandardWatchEventKinds.ENTRY_MODIFY,
               StandardWatchEventKinds.ENTRY_CREATE,
               StandardWatchEventKinds.ENTRY_DELETE,
               StandardWatchEventKinds.OVERFLOW);

       while (true){
           WatchKey key;

           try{
               key = watchService.take();
           }catch (InterruptedException e){
               break;
           }

           for (WatchEvent<?> event : key.pollEvents()){
               WatchEvent.Kind<?> kind = event.kind();

               if(kind == StandardWatchEventKinds.OVERFLOW){
                   continue;
               }

               Path filename = (Path)  event.context();
               Path fullpath = DirToWatch.resolve(filename);
               System.out.println(" pinged  " + kind.name() + fullpath);
               System.out.println("...");

           }


           boolean valid= key.reset();
           if (!valid){
               break;
           }
       }


    }

}
