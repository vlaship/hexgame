package com.hexgame;

import javax.swing.*;
import java.awt.*;

public class MainMenu extends JPanel {
    private JFrame frame;
    
    public MainMenu(JFrame frame) {
        this.frame = frame;
        setPreferredSize(new Dimension(980, 720)); 
        setLayout(new GridBagLayout());
        setBackground(new Color(25, 30, 40)); 
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 50, 10, 50);

        JLabel title = new JLabel("HEXGAME");
        title.setFont(new Font("Monospaced", Font.BOLD, 64));
        title.setForeground(new Color(220, 220, 230));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel subtitle = new JLabel("4X STRATEGY");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 24));
        subtitle.setForeground(new Color(150, 150, 150));
        subtitle.setHorizontalAlignment(SwingConstants.CENTER);

        JButton btnStart = createMenuButton("START NORMAL GAME", new Color(40, 140, 60));
        btnStart.addActionListener(e -> startGame(HexGrid.GameMode.NORMAL));
        
        JButton btnDev = createMenuButton("DEV MODE (TEST UNITS)", new Color(140, 80, 40));
        btnDev.addActionListener(e -> startGame(HexGrid.GameMode.DEV_MODE));

        JButton btnAiVsAi = createMenuButton("AI VS AI (SPECTATE)", new Color(100, 40, 140));
        btnAiVsAi.addActionListener(e -> startGame(HexGrid.GameMode.AI_VS_AI));

        JButton btnSettings = createMenuButton("SETTINGS / CONTROLS", new Color(60, 80, 120));
        btnSettings.addActionListener(e -> openSettings());

        JButton btnExit = createMenuButton("EXIT TO DESKTOP", new Color(140, 40, 40));
        btnExit.addActionListener(e -> System.exit(0));

        gbc.insets = new Insets(10, 50, 5, 50);
        add(title, gbc);
        gbc.insets = new Insets(0, 50, 40, 50);
        add(subtitle, gbc);
        
        gbc.insets = new Insets(8, 50, 8, 50);
        add(btnStart, gbc);
        add(btnDev, gbc);
        add(btnAiVsAi, gbc);
        add(btnSettings, gbc);
        add(btnExit, gbc);
    }
    
    private JButton createMenuButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 18));
        btn.setFocusPainted(false);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setPreferredSize(new Dimension(350, 50));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
    
    private void startGame(HexGrid.GameMode mode) {
        frame.getContentPane().removeAll();
        HexGrid hexGrid = new HexGrid(mode);
        frame.add(hexGrid);
        frame.pack();
        frame.setLocationRelativeTo(null);
        hexGrid.requestFocusInWindow();
    }
    
    private void openSettings() {
        String msg = "CONTROLS:\n\n" +
                     "  Arrow Keys: Scroll Map\n" +
                     "  Left Click: Select Unit / Move / Attack\n" +
                     "  Right Click: Open Build/Production Menu\n" +
                     "  Double-Click: Toggle Power / Detonate Nuke / Unload Ship\n" +
                     "  ENTER: End Turn\n\n" +
                     "HINT: Don't forget to build Burning Stations early for power!";
        JOptionPane.showMessageDialog(this, msg, "Settings & Controls", JOptionPane.INFORMATION_MESSAGE);
    }
}
