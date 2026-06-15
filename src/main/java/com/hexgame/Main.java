package com.hexgame;
import javax.swing.*; // Imports Java's standard GUI toolkit components

public class Main {

    public static void main(String[] args) { // The main application entry loop
        // Always create Swing UI on the Event Dispatch Thread to prevent graphical or threading glitches
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("4X Strategy – Hex Map"); // Creates the main OS-level window frame with a custom title
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Program fully terminates when clicking the 'X' button
            frame.setResizable(false); // Disables resizing to keep your specific layout dimensions intact

            MainMenu menu = new MainMenu(frame); // Creates the new start menu
            frame.add(menu); // Attaches the menu layout inside the window frame

            frame.pack(); // Sizes the frame window automatically so the Menu fits with no extra border space
            frame.setLocationRelativeTo(null); // Centers the completed game window perfectly in the middle of the user's monitor
            frame.setVisible(true); // Commands the OS to display the window graphically on screen
        });
    }
}
