import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import javax.sound.sampled.Line;
import java.io.IOException;
import java.util.List;

public class CLI {


    private final Dasa dasa;
    private final Swami swami;


    public CLI(Dasa dasa, Swami swami) {

        this.dasa = dasa;

        this.swami = swami;
    }

    public void start() {

        ConfigManager config = new ConfigManager();

        String newDesign;
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
                    line = Reader.readLine(": ");
                } catch (UserInterruptException | EndOfFileException e) {
                    terminal.writer().println("\nCaught exit signal. Shutting down SAMA...");
                    terminal.writer().flush();
                    System.exit(0);
                }
                String command = line.trim();
                String design = config.load("design");
                if (design == null) {
                    terminal.writer().println("Configuration not found,,,");
                    terminal.writer().println("please enter your designation in the network.");
                    do {
                        newDesign = Reader.readLine(": ").trim();
                        if (!newDesign.equalsIgnoreCase("dasa") && !newDesign.equalsIgnoreCase("Swami")) {
                            terminal.writer().println("Invalid input please enter if you are dasa or swami in your network");
                            terminal.writer().flush();
                        }
                    } while (!newDesign.equalsIgnoreCase("dasa") && !newDesign.equalsIgnoreCase("Swami"));
                    terminal.writer().println("Updating config...");
                    config.save("design", newDesign);
                    design = newDesign;

                    terminal.writer().flush();
                }

                List<String> pathList = config.LoadPaths();
                if (pathList.isEmpty()) {
                    terminal.writer().println("Please add paths you want to synchronise");
                    String newAddition = command;
                    config.addPath(command);
                }

                if (design.equals("dasa")) {
                    Thread Slave = new Thread(() -> {
                        try {
                            dasa.slave(pathList);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });

                    Slave.start();
                }

                if (command.toLowerCase().startsWith("add ")) {

                    String newPath = command.substring(3).trim();
                    config.addPath(newPath);
                    dasa.registerNewPath(newPath);
                    terminal.writer().println("Started watching " + newPath);
                } else {
                    terminal.writer().println("Unknown command.");
                }
                if (command.toLowerCase().startsWith("list")) {

                    List allpath = config.LoadPaths();
                    terminal.writer().println(allpath);

                } else {
                    terminal.writer().println("Unknown command.");
                }


                terminal.writer().flush();
            }


        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
}
