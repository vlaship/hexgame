package com.hexgame;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class Main {

    public static void main(String[] args) {
        Properties config = new Properties();
        try (FileInputStream in = new FileInputStream("config.properties")) {
            config.load(in);
        } catch (IOException e) {
            // file missing — use defaults defined in HexGrid
        }

        int w = Integer.parseInt(config.getProperty("screen.width",  "980"));
        int h = Integer.parseInt(config.getProperty("screen.height", "750"));
        HexGrid.setViewSize(w, h);

        HexGrid.setCameraKeys(
            parseKeys(config.getProperty("camera.up",    "W UP")),
            parseKeys(config.getProperty("camera.down",  "S DOWN")),
            parseKeys(config.getProperty("camera.left",  "A LEFT")),
            parseKeys(config.getProperty("camera.right", "D RIGHT"))
        );

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("4X Strategy – Hex Map");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);

            HexGrid hexGrid = new HexGrid();
            frame.add(hexGrid);

            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            hexGrid.requestFocusInWindow();
        });
    }

    private static Set<Integer> parseKeys(String value) {
        Set<Integer> result = new HashSet<>();
        for (String name : value.trim().split("\\s+")) {
            try {
                int vk = KeyEvent.class.getField("VK_" + name.toUpperCase()).getInt(null);
                result.add(vk);
            } catch (Exception ignored) {
                System.err.println("Unknown key name in config: " + name);
            }
        }
        return result;
    }
}
