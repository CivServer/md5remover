package net.civserver.md5remover;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static final String OPENING_PLUGIN_BLOCK = "<plugin>";
    public static final String CLOSING_PLUGIN_BLOCK = "</plugin>";
    public static final String OPENING_PROFILE_BLOCK = "<profile>";
    public static final String CLOSING_PROFILE_BLOCK = "</profile>";
    public static final String PLUGIN_NAME = "specialsource-maven-plugin";

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

            Scanner sc = new Scanner(f);

            List<String> lines = new ArrayList<>();
            int targetIndex = -1;
            while (sc.hasNextLine()) {
                String l = sc.nextLine();
                // Only look for the first instance of the plugin
                if (targetIndex == -1 && l.contains(PLUGIN_NAME)) {
                    targetIndex = lines.size(); // we found the index of the specialsource plugin
                }
                lines.add(l);
            }
            sc.close();

            if (targetIndex == -1) {
                System.out.println("We did not find the specialsource plugin in this file.");
                System.exit(-1);
            }

            // Create a backup file
            InputStream in = new BufferedInputStream(new FileInputStream(f));
            OutputStream out = new BufferedOutputStream(new FileOutputStream(new File(f.getParent(), f.getName() + ".bak")));

            byte[] buffer = new byte[1024];
            int lengthRead;
            while ((lengthRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, lengthRead);
                out.flush();
            }
            in.close();
            out.close();

            removeBlock(lines, PLUGIN_NAME, OPENING_PLUGIN_BLOCK, CLOSING_PLUGIN_BLOCK);
            removeBlock(lines, PLUGIN_NAME, OPENING_PROFILE_BLOCK, CLOSING_PROFILE_BLOCK);

            FileWriter fw = new FileWriter(f, false);
            for (String s : lines) {
                fw.write(s);
            }
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void removeBlock(List<String> lines, String target, String opening, String closing) {
        int upperIndex = -1;
        int targetIndex = -1;
        int lowerIndex = -1;

        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).contains(target)) {
                targetIndex = i;
                break;
            }
        }

        if (targetIndex == -1) {
            System.out.println("Found plugin name, but invalid block!");
            return;
        }

        for (int i = targetIndex; i >= 0; i--) {
            if (lines.get(i).contains(opening)) {
                upperIndex = i;
                break;
            }
        }

        if (upperIndex == -1) {
            System.out.println("Found plugin name, but invalid block!");
            return;
        }

        for (int i = targetIndex; i < lines.size(); i++) {
            if (lines.get(i).contains(closing)) {
                lowerIndex = i;
                break;
            }
        }

        if (lowerIndex == -1) {
            System.out.println("Found plugin name, but invalid block!");
            return;
        }

        // Cleanse the file of the plugin block
        for (int i = 0; i < lowerIndex - upperIndex + 1; i++) {
            System.out.println("Removed line: " + lines.remove(upperIndex));
        }
    }
}