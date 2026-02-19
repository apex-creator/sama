import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class main {

    private static final Logger print = LoggerFactory.getLogger(main.class);
    static Dasa dasa;
    static Swami swami;
    static SlaveSocket dosa;


    static {
        try {
            dosa = new SlaveSocket();
            dasa = new Dasa(dosa);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    static {
        try {
            swami = new Swami();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static void main(String[] args) {


        CLI commandLine = new CLI(dasa, swami);
        commandLine.start();
    }

}