import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;

public class MemWriter {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final File File = new File("hello.json");

    public static void write(Appstate state) throws Exception{
        mapper.writerWithDefaultPrettyPrinter()
                .writeValue(File, state);
    }

}
