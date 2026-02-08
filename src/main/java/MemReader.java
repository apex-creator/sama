import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;

public class MemReader {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final File FIle = new File("hello.json");

    public static Appstate read() {
        try {
            return mapper.readValue(FIle, Appstate.class);
        } catch (Exception e) {
            return null;
        }
    }

}
