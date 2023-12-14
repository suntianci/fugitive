package com.stc.fugitive.test.porn;

import java.io.IOException;

/**
 * @author suntianci on 2022/6/9
 */
public class ProcessMain {

    public static void runCommand(String command) {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("bash", "-c", command);
        try {
            System.out.println(String.join(" ", processBuilder.command()));
            Process process = processBuilder.start();
        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
