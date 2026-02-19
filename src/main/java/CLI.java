import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import javax.sound.sampled.Line;
import java.io.IOException;

public class CLI {

    private final ConfigManager config;
    private final Dasa dasa;
    private final String mode;

    public CLI(ConfigManager config, Dasa dasa, String mode) {
        this.config = config;
        this.dasa = dasa;
        this.mode = mode;
    }

    public void start() {

        try {
            Terminal terminal = TerminalBuilder.builder()
                    .system(true)
                    .build();

            LineReader Reader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .build();

            while (true) {
                String line = null;

                try {
                    line = Reader.readLine(":");
                } catch (UserInterruptException | EndOfFileException e) {
                    // This catches Ctrl+C or Ctrl+D and shuts down safely
                    terminal.writer().println("\nCaught exit signal. Shutting down SAMA...");
                    terminal.writer().flush();
                    System.exit(0);
                }
                String command = line.trim();
            }


        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
}
