package net.civserver.md5remover;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static final String OPENING_BLOCK = "<plugin>";
    public static final String PLUGIN_NAME = "specialsource-maven-plugin";
    public static final String CLOSING_BLOCK = "</plugin>";

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please specify the pom.xml file to search.");
            System.exit(-1);
        }

        try {
            File f = new File(args[0]);
            if (!f.exists()) {
                System.out.println("File " + args[0] + " does not exist.");
                System.exit(-1);
            }

            Scanner in = new Scanner(f);

            List<String> lines = new ArrayList<>();
            int upperIndex = -1;
            int targetIndex = -1;
            int lowerIndex = -1;
            while (in.hasNextLine()) {
                String l = in.nextLine();
                // Only look for the first instance of the plugin
                if (targetIndex == -1 && l.contains(PLUGIN_NAME)) {
                    targetIndex = lines.size(); // we found the index of the specialsource plugin
                }
                lines.add(l);
            }
            in.close();

            if (targetIndex == -1) {
                System.out.println("We did not find the specialsource plugin in this file.");
                System.exit(-1);
            }

            for (int i = targetIndex; i >= 0; i--) {
                if (lines.get(i).contains(OPENING_BLOCK)) {
                    upperIndex = i;
                    break;
                }
            }

            if (upperIndex == -1) {
                System.out.println("We did not find the specialsource plugin in this file.");
                System.exit(-1);
            }

            for (int i = targetIndex; i < lines.size(); i++) {
                if (lines.get(i).contains(CLOSING_BLOCK)) {
                    lowerIndex = i;
                    break;
                }
            }

            if (lowerIndex == -1) {
                System.out.println("We did not find the specialsource plugin in this file.");
                System.exit(-1);
            }

            // Cleanse the file of the plugin block
            for (int i = 0; i < lowerIndex - upperIndex; i++) {
                System.out.println("Removed line: " + lines.remove(upperIndex));
            }

            FileWriter fw = new FileWriter(f, false);
            for (String s : lines) {
                fw.write(s);
            }
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}