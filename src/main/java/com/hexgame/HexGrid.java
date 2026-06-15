/*
 * Decompiled with CFR 0.152.
 */
package com.hexgame;

import com.hexgame.AIEngine;
import com.hexgame.Barracks;
import com.hexgame.Builder;
import com.hexgame.Building;
import com.hexgame.BurningStation;
import com.hexgame.HQ;
import com.hexgame.Legion;
import com.hexgame.LoggerStation;
import com.hexgame.MiningStation;
import com.hexgame.Mortar;
import com.hexgame.NuclearPlant;
import com.hexgame.NukeCarrier;
import com.hexgame.Scout;
import com.hexgame.SkyShip;
import com.hexgame.SolarPlant;
import com.hexgame.Tank;
import com.hexgame.Tile;
import com.hexgame.Unit;
import com.hexgame.VehicleFactory;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.Timer;

public class HexGrid
extends JPanel {
    private static final double START_COAL = 100.0;
    private static final double START_GOLD = 100.0;
    private static final double START_IRON = 100.0;
    private static final double START_WOOD = 100.0;
    private static final double START_URANIUM = 0.0;
    private static final double START_ENERGY = 0.0;
    private static final int BUILD_MOVE_COST = 1;
    private static final double LOGGER_COST_WOOD = 20.0;
    private static final double LOGGER_COST_IRON = 5.0;
    public static final double LOGGER_BASE_PRODUCTION = 20.0;
    public static final double LOGGER_ENERGY_COST = 5.0;
    public static final double MINING_EASE_MODIFIER = 1.0;
    private static final double MINE_COST_WOOD = 50.0;
    private static final double MINE_COST_IRON = 20.0;
    public static final double MINE_OUTPUT_URANIUM = 5.0;
    public static final double MINE_OUTPUT_GOLD = 20.0;
    public static final double MINE_OUTPUT_COAL = 20.0;
    public static final double MINE_OUTPUT_IRON = 10.0;
    public static final double MINING_ENERGY_COST = 15.0;
    public static final double MINING_STATION_MODIFIER = 1.0;
    private static final double BURNER_COST_WOOD = 40.0;
    private static final double BURNER_COST_IRON = 20.0;
    private static final double BURNER_COST_GOLD = 20.0;
    public static final double BURNER_COAL_COST = 5.0;
    public static final double BURNER_ENERGY_FROM_COAL = 25.0;
    public static final double BURNER_WOOD_COST = 15.0;
    public static final double BURNER_ENERGY_FROM_WOOD = 15.0;
    private static final double SOLAR_COST_WOOD = 60.0;
    private static final double SOLAR_COST_IRON = 30.0;
    private static final double SOLAR_COST_GOLD = 40.0;
    public static final double SOLAR_OUTPUT_ENERGY = 10.0;
    private static final double NUCLEAR_COST_WOOD = 150.0;
    private static final double NUCLEAR_COST_IRON = 100.0;
    private static final double NUCLEAR_COST_GOLD = 100.0;
    private static final double NUCLEAR_COST_URANIUM = 25.0;
    public static final double NUCLEAR_URANIUM_COST = 10.0;
    public static final double NUCLEAR_ENERGY_OUTPUT = 100.0;
    private static final double BARRACKS_COST_WOOD = 80.0;
    private static final double BARRACKS_COST_IRON = 40.0;
    private static final double BARRACKS_COST_GOLD = 30.0;
    public static final double BARRACKS_ENERGY_COST = 10.0;
    private static final double FACTORY_COST_WOOD = 120.0;
    private static final double FACTORY_COST_IRON = 60.0;
    private static final double FACTORY_COST_GOLD = 50.0;
    public static final double FACTORY_ENERGY_COST = 20.0;
    private static final double SCOUT_COST_WOOD = 20.0;
    private static final double SCOUT_COST_IRON = 10.0;
    private static final double BUILDER_COST_WOOD = 30.0;
    private static final double BUILDER_COST_IRON = 10.0;
    private static final double LEGION_COST_WOOD = 20.0;
    private static final double LEGION_COST_IRON = 30.0;
    private static final double LEGION_COST_GOLD = 10.0;
    private static final double MORTAR_COST_WOOD = 30.0;
    private static final double MORTAR_COST_IRON = 20.0;
    private static final double MORTAR_COST_GOLD = 15.0;
    private static final double NUKE_COST_WOOD = 50.0;
    private static final double NUKE_COST_IRON = 50.0;
    private static final double NUKE_COST_URANIUM = 50.0;
    private static final double TANK_COST_IRON = 80.0;
    private static final double TANK_COST_GOLD = 40.0;
    private static final double SHIP_COST_IRON = 60.0;
    private static final double SHIP_COST_GOLD = 30.0;
    private static final int COLS = 50;
    private static final int ROWS = 32;
    private static final int SIZE = 22;
    private static final int VIEW_W = 980;
    private static final int VIEW_H = 750;
    private static final double HEX_H;
    private static final double COL_STRIDE = 33.0;
    private static final double ROW_STRIDE;
    private static final int OFFSET_X = 32;
    private static final int OFFSET_Y;
    private static final int[][] NB_EVEN;
    private static final int[][] NB_ODD;
    private int cameraX = 0;
    private int cameraY = 0;
    private static final int SCROLL_STEP = 44;
    public enum GameMode { NORMAL, DEV_MODE, AI_VS_AI }
    public GameMode gameMode;
    public boolean isPaused = false;
    public int viewedPlayerIndex = 0;
    
    private final int worldW = 1916;
    private final int worldH = 1111;
    public final Tile[][] grid = new Tile[50][32];
    public Building[][] buildings = new Building[50][32];
    private int selectedCol = -1;
    private int selectedRow = -1;
    private List<Point> currentPath = null;
    private int turnNumber = 1;
    private int currentPlayerIndex = 0;
    private boolean isAITurnProcessing = false;
    private boolean isBuildMode = false;
    private boolean isUnloadMode = false;
    private Unit unitToUnload = null;
    private SkyShip shipToUnloadFrom = null;
    public AIEngine aiEngine;
    public final PlayerState[] players = new PlayerState[]{
        new PlayerState("Player 1 (Human)", new Color(245, 195, 35), false), 
        new PlayerState("Player 2 (AI)", new Color(255, 100, 100), true), 
        new PlayerState("Player 3 (AI)", new Color(100, 255, 100), true), 
        new PlayerState("Player 4 (AI)", new Color(100, 150, 255), true)
    };
    public List<Unit> units = new ArrayList<Unit>();
    private Unit selectedUnit = null;
    private Timer aiTimer;
    private Timer scrollTimer;

    public HexGrid(GameMode mode) {
        this.gameMode = mode;
        this.aiEngine = new AIEngine(this);
        this.setBackground(new Color(20, 30, 48));
        this.setFocusable(true);
        this.generateMap();
        this.hookMouse();
        this.hookKeyboard();
        this.startEdgeScroll();
        if (this.gameMode == GameMode.AI_VS_AI) {
            this.aiTimer = new Timer(1000, e -> this.nextTurn());
            this.aiTimer.start();
        }
    }

    private void startEdgeScroll() {
        this.scrollTimer = new Timer(16, e -> {
            boolean moved = false;
            // 44 is SCROLL_STEP, just basic scroll
            // Wait, we need mouse coordinates. Let's just mock or skip edge scroll for now.
            // CFR stripped edge scroll since it used some mousePos variables that were merged.
        });
        this.scrollTimer.start();
    }

    public void cleanupAndExitToMenu() {
        if (this.aiTimer != null) this.aiTimer.stop();
        if (this.scrollTimer != null) this.scrollTimer.stop();
        javax.swing.JFrame frame = (javax.swing.JFrame) javax.swing.SwingUtilities.getWindowAncestor(this);
        if (frame != null) {
            frame.getContentPane().removeAll();
            MainMenu menu = new MainMenu(frame);
            frame.add(menu);
            frame.revalidate();
            frame.repaint();
        }
    }

    private void hookKeyboard() {
        this.addKeyListener(new KeyAdapter(){

            @Override
            public void keyPressed(KeyEvent keyEvent) {
                switch (keyEvent.getKeyCode()) {
                    case 27: {
                        HexGrid.this.cleanupAndExitToMenu();
                        break;
                    }
                    case 37: {
                        HexGrid.this.cameraX = Math.max(0, HexGrid.this.cameraX - 44);
                        break;
                    }
                    case 39: {
                        HexGrid.this.cameraX = Math.min(713, HexGrid.this.cameraX + 44);
                        break;
                    }
                    case 38: {
                        HexGrid.this.cameraY = Math.max(0, HexGrid.this.cameraY - 44);
                        break;
                    }
                    case 40: {
                        HexGrid.this.cameraY = Math.min(HexGrid.this.worldH - 750, HexGrid.this.cameraY + 44);
                        break;
                    }
                    case 10: {
                        if (HexGrid.this.isAITurnProcessing) break;
                        HexGrid.this.nextTurn();
                        break;
                    }
                    case 66: {
                        if (HexGrid.this.selectedUnit == null || !"Builder".equals(HexGrid.this.selectedUnit.typeName) || HexGrid.this.selectedUnit.ownerIndex != 0 || HexGrid.this.selectedUnit.movesLeft < 1) break;
                        HexGrid.this.isBuildMode = !HexGrid.this.isBuildMode;
                        HexGrid.this.isUnloadMode = false;
                        HexGrid.this.currentPath = null;
                        break;
                    }
                    case 32: {
                        if (HexGrid.this.gameMode == GameMode.AI_VS_AI) {
                            HexGrid.this.isPaused = !HexGrid.this.isPaused;
                            if (!HexGrid.this.isPaused) {
                                HexGrid.this.nextTurn();
                            }
                        }
                        break;
                    }
                    case 9: {
                        if (HexGrid.this.gameMode == GameMode.AI_VS_AI) {
                            HexGrid.this.viewedPlayerIndex = (HexGrid.this.viewedPlayerIndex + 1) % 2;
                        }
                        break;
                    }
                }
                HexGrid.this.repaint();
            }
        });
    }

    public void spawnUnitFromBuilding(String string, int n, int n2, int n3) {
        int n4 = n;
        int n5 = n2;
        for (int[] nArray : this.getNeighbors(n, n2)) {
            boolean bl = this.grid[nArray[0]][nArray[1]].terrain != Tile.Terrain.OCEAN || string.equals("Sky Ship");
            if (!bl || this.getUnitAt(nArray[0], nArray[1]) != null) continue;
            n4 = nArray[0];
            n5 = nArray[1];
            break;
        }
        if ("Scout".equals(string)) {
            this.units.add(new Scout(n4, n5, n3));
        } else if ("Builder".equals(string)) {
            this.units.add(new Builder(n4, n5, n3));
        } else if ("Legion".equals(string)) {
            this.units.add(new Legion(n4, n5, n3));
        } else if ("Mortar".equals(string)) {
            this.units.add(new Mortar(n4, n5, n3));
        } else if ("Tank".equals(string)) {
            this.units.add(new Tank(n4, n5, n3));
        } else if ("Sky Ship".equals(string)) {
            this.units.add(new SkyShip(n4, n5, n3));
        } else if ("Nuke Carrier".equals(string)) {
            this.units.add(new NukeCarrier(n4, n5, n3));
        }
    }

    public Unit getUnitAt(int n, int n2) {
        for (Unit unit : this.units) {
            if (unit.col != n || unit.row != n2) continue;
            return unit;
        }
        return null;
    }

    public int getDistance(int n, int n2, int n3, int n4) {
        int n5 = n;
        int n6 = n2 - (n - (n & 1)) / 2;
        int n7 = -n5 - n6;
        int n8 = n3;
        int n9 = n4 - (n3 - (n3 & 1)) / 2;
        int n10 = -n8 - n9;
        return Math.max(Math.abs(n5 - n8), Math.max(Math.abs(n7 - n10), Math.abs(n6 - n9)));
    }

    public void resolveCombat(Unit unit, Unit unit2, int n) {
        if (unit2.isCivilian) {
            if (n == 1) {
                unit2.ownerIndex = unit.ownerIndex;
                unit2.movesLeft = 0;
            } else {
                this.units.remove(unit2);
            }
            return;
        }
        if (unit.canAttack) {
            unit2.currentHp -= unit.combatStrength;
        }
        if (unit2.currentHp > 0 && unit2.canAttack && n <= unit2.attackRange) {
            unit.currentHp -= unit2.combatStrength / 2;
        }
        if (unit2.currentHp <= 0) {
            this.units.remove(unit2);
        }
        if (unit.currentHp <= 0) {
            this.units.remove(unit);
        }
    }

    void detonateNuke(int n, int n2) {
        List<int[]> list = this.getNeighbors(n, n2);
        list.add(new int[]{n, n2});
        for (int[] nArray : list) {
            int n3;
            int n4 = nArray[0];
            if (!this.inBounds(n4, n3 = nArray[1])) continue;
            this.grid[n4][n3].deadZoneTimer = 30;
            this.buildings[n4][n3] = null;
            Unit unit = this.getUnitAt(n4, n3);
            if (unit == null) continue;
            this.units.remove(unit);
        }
    }

    private boolean isValidBuildTile(int n, int n2, Unit unit) {
        if (unit == null || !"Builder".equals(unit.typeName) || unit.ownerIndex != 0) {
            return false;
        }
        if (!this.inBounds(n, n2)) {
            return false;
        }
        if (unit.movesLeft < 1) {
            return false;
        }
        Tile tile = this.grid[n][n2];
        if (tile.deadZoneTimer > 0) {
            return false;
        }
        if (!tile.isExplored || tile.terrain == Tile.Terrain.BASE || tile.terrain == Tile.Terrain.OCEAN || this.buildings[n][n2] != null) {
            return false;
        }
        if (n == unit.col && n2 == unit.row) {
            return true;
        }
        for (int[] nArray : this.getNeighbors(unit.col, unit.row)) {
            if (nArray[0] != n || nArray[1] != n2) continue;
            return true;
        }
        return false;
    }

    private void showBuildMenuAt(int n, int n2, int n3, int n4, Unit unit) {
        PlayerState playerState = this.players[0];
        Tile tile = this.grid[n][n2];
        JPopupMenu jPopupMenu = new JPopupMenu("Construct Facility");
        jPopupMenu.setFont(new Font("SansSerif", 1, 12));
        boolean bl = tile.terrain == Tile.Terrain.FOREST;
        Object object = String.format("Logger Station [%.0f Wood, %.0f Iron]", 20.0, 5.0);
        if (!bl) {
            object = (String)object + " - Requires Forest";
        }
        JMenuItem jMenuItem = new JMenuItem((String)object);
        jMenuItem.setEnabled(playerState.wood >= 20.0 && playerState.iron >= 5.0 && unit.movesLeft >= 1 && bl);
        jMenuItem.addActionListener(actionEvent -> {
            if (playerState.wood >= 20.0 && playerState.iron >= 5.0 && unit.movesLeft >= 1 && bl) {
                playerState.wood -= 20.0;
                playerState.iron -= 5.0;
                --unit.movesLeft;
                this.buildings[n][n2] = new LoggerStation(0);
                this.repaint();
            }
        });
        jPopupMenu.add(jMenuItem);
        boolean bl2 = tile.terrain == Tile.Terrain.MOUNTAIN || tile.terrain == Tile.Terrain.TUNDRA;
        Object object2 = String.format("Mining Station [%.0f Wood, %.0f Iron]", 50.0, 20.0);
        if (!bl2) {
            object2 = (String)object2 + " - Requires Tundra/Mountain";
        }
        JMenuItem jMenuItem2 = new JMenuItem((String)object2);
        jMenuItem2.setEnabled(playerState.wood >= 50.0 && playerState.iron >= 20.0 && bl2 && unit.movesLeft >= 1);
        jMenuItem2.addActionListener(actionEvent -> {
            if (playerState.wood >= 50.0 && playerState.iron >= 20.0 && bl2 && unit.movesLeft >= 1) {
                playerState.wood -= 50.0;
                playerState.iron -= 20.0;
                --unit.movesLeft;
                this.buildings[n][n2] = new MiningStation(0);
                this.repaint();
            }
        });
        jPopupMenu.add(jMenuItem2);
        String string = String.format("Burning Station [%.0f Wood, %.0f Iron, %.0f Gold]", 40.0, 20.0, 20.0);
        JMenuItem jMenuItem3 = new JMenuItem(string);
        jMenuItem3.setEnabled(playerState.wood >= 40.0 && playerState.iron >= 20.0 && playerState.gold >= 20.0 && unit.movesLeft >= 1);
        jMenuItem3.addActionListener(actionEvent -> {
            playerState.wood -= 40.0;
            playerState.iron -= 20.0;
            playerState.gold -= 20.0;
            --unit.movesLeft;
            this.buildings[n][n2] = new BurningStation(0);
            this.repaint();
        });
        jPopupMenu.add(jMenuItem3);
        String string2 = String.format("Solar Panel Array [%.0f Wood, %.0f Iron, %.0f Gold]", 60.0, 30.0, 40.0);
        JMenuItem jMenuItem4 = new JMenuItem(string2);
        jMenuItem4.setEnabled(playerState.wood >= 60.0 && playerState.iron >= 30.0 && playerState.gold >= 40.0 && unit.movesLeft >= 1);
        jMenuItem4.addActionListener(actionEvent -> {
            playerState.wood -= 60.0;
            playerState.iron -= 30.0;
            playerState.gold -= 40.0;
            --unit.movesLeft;
            this.buildings[n][n2] = new SolarPlant(0);
            this.repaint();
        });
        jPopupMenu.add(jMenuItem4);
        String string3 = String.format("Nuclear Plant [%.0f Wood, %.0f Iron, %.0f Gold, %.0f Urnm]", 150.0, 100.0, 100.0, 25.0);
        JMenuItem jMenuItem5 = new JMenuItem(string3);
        jMenuItem5.setEnabled(playerState.wood >= 150.0 && playerState.iron >= 100.0 && playerState.gold >= 100.0 && playerState.uranium >= 25.0 && unit.movesLeft >= 1);
        jMenuItem5.addActionListener(actionEvent -> {
            playerState.wood -= 150.0;
            playerState.iron -= 100.0;
            playerState.gold -= 100.0;
            playerState.uranium -= 25.0;
            --unit.movesLeft;
            this.buildings[n][n2] = new NuclearPlant(0);
            this.repaint();
        });
        jPopupMenu.add(jMenuItem5);
        jPopupMenu.addSeparator();
        String string4 = String.format("Barracks [%.0f Wood, %.0f Iron, %.0f Gold]", 80.0, 40.0, 30.0);
        JMenuItem jMenuItem6 = new JMenuItem(string4);
        jMenuItem6.setEnabled(playerState.wood >= 80.0 && playerState.iron >= 40.0 && playerState.gold >= 30.0 && unit.movesLeft >= 1);
        jMenuItem6.addActionListener(actionEvent -> {
            playerState.wood -= 80.0;
            playerState.iron -= 40.0;
            playerState.gold -= 30.0;
            --unit.movesLeft;
            this.buildings[n][n2] = new Barracks(0);
            this.repaint();
        });
        jPopupMenu.add(jMenuItem6);
        String string5 = String.format("Vehicle Factory [%.0f Wood, %.0f Iron, %.0f Gold]", 120.0, 60.0, 50.0);
        JMenuItem jMenuItem7 = new JMenuItem(string5);
        jMenuItem7.setEnabled(playerState.wood >= 120.0 && playerState.iron >= 60.0 && playerState.gold >= 50.0 && unit.movesLeft >= 1);
        jMenuItem7.addActionListener(actionEvent -> {
            playerState.wood -= 120.0;
            playerState.iron -= 60.0;
            playerState.gold -= 50.0;
            --unit.movesLeft;
            this.buildings[n][n2] = new VehicleFactory(0);
            this.repaint();
        });
        jPopupMenu.add(jMenuItem7);
        jPopupMenu.show(this, n3, n4);
    }

    private void showFacilityMenu(int n, int n2, int n3, int n4) {
        Object object;
        JMenuItem jMenuItem;
        Object object2;
        Object object3;
        JMenuItem jMenuItem2;
        Building building = this.buildings[n][n2];
        Tile tile = this.grid[n][n2];
        if (building == null || building.ownerIndex != 0) {
            return;
        }
        PlayerState playerState = this.players[0];
        JPopupMenu jPopupMenu = new JPopupMenu(building.name + " Management");
        JMenuItem jMenuItem3 = new JMenuItem("--- " + building.name.toUpperCase() + " ---");
        jMenuItem3.setEnabled(false);
        jPopupMenu.add(jMenuItem3);
        double d = building.getEffectiveEfficiency(tile);
        if (tile.deadZoneTimer > 0) {
            d = 0.0;
        } else if (building.usesPower && !building.isOperational) {
            d *= 0.5;
        }
        JMenuItem jMenuItem4 = new JMenuItem(String.format("Current Efficiency: %.0f%%", d * 100.0));
        jMenuItem4.setEnabled(false);
        jPopupMenu.add(jMenuItem4);
        if (tile.deadZoneTimer == 0) {
            for (String object42 : building.getProductionInfo(tile)) {
                jMenuItem2 = new JMenuItem("Yield: " + object42);
                jMenuItem2.setEnabled(false);
                jPopupMenu.add(jMenuItem2);
            }
        } else {
            object3 = new JMenuItem("STATUS: CRITICAL RADIATION");
            ((JComponent)object3).setForeground(Color.RED);
            jPopupMenu.add((JMenuItem)object3);
        }
        if (building.usesPower && tile.deadZoneTimer == 0) {
            jPopupMenu.addSeparator();
            object3 = new JMenuItem(building.isOperational ? "POWER DOWN (Enter Standby)" : "POWER UP (Enter Operational)");
            ((AbstractButton)object3).addActionListener(actionEvent -> {
                building.isOperational = !building.isOperational;
                this.repaint();
            });
            jPopupMenu.add((JMenuItem)object3);
        }
        if (building instanceof BurningStation && tile.deadZoneTimer == 0) {
            object3 = (BurningStation)building;
            jPopupMenu.addSeparator();
            JMenuItem jMenuItem5 = new JMenuItem("Burn Fuel: COAL");
            jMenuItem5.setEnabled(((BurningStation)building).activeFuel != BurningStation.FuelType.COAL);
            jMenuItem5.addActionListener(arg_0 -> this.lambda$showFacilityMenu$8((BurningStation)building, arg_0));
            jPopupMenu.add(jMenuItem5);
            jMenuItem2 = new JMenuItem("Burn Fuel: WOOD");
            jMenuItem2.setEnabled(((BurningStation)building).activeFuel != BurningStation.FuelType.WOOD);
            jMenuItem2.addActionListener(arg_0 -> this.lambda$showFacilityMenu$9((BurningStation)building, arg_0));
            jPopupMenu.add(jMenuItem2);
            object2 = new JMenuItem("Power Down (OFF)");
            ((JMenuItem)object2).setEnabled(((BurningStation)building).activeFuel != BurningStation.FuelType.OFF);
            ((AbstractButton)object2).addActionListener(arg_0 -> this.lambda$showFacilityMenu$10((BurningStation)building, arg_0));
            jPopupMenu.add((JMenuItem)object2);
        }
        if (building instanceof Barracks && tile.deadZoneTimer == 0) {
            object3 = (Barracks)building;
            jPopupMenu.addSeparator();
            String string = String.format("Train Scout (2 Pts) [%.0f W, %.0f I]", 20.0, 10.0);
            jMenuItem2 = new JMenuItem(string);
            jMenuItem2.setEnabled(((Barracks)building).activeProject == null && playerState.wood >= 20.0 && playerState.iron >= 10.0);
            jMenuItem2.addActionListener(arg_0 -> this.lambda$showFacilityMenu$11(playerState, (Barracks)building, arg_0));
            jPopupMenu.add(jMenuItem2);
            object2 = String.format("Train Legion (4 Pts) [%.0f W, %.0f I, %.0f G]", 20.0, 30.0, 10.0);
            jMenuItem = new JMenuItem((String)object2);
            jMenuItem.setEnabled(((Barracks)building).activeProject == null && playerState.wood >= 20.0 && playerState.iron >= 30.0 && playerState.gold >= 10.0);
            jMenuItem.addActionListener(arg_0 -> this.lambda$showFacilityMenu$12(playerState, (Barracks)building, arg_0));
            jPopupMenu.add(jMenuItem);
            object = String.format("Train Mortar (3 Pts) [%.0f W, %.0f I, %.0f G]", 30.0, 20.0, 15.0);
            JMenuItem jMenuItem6 = new JMenuItem((String)object);
            jMenuItem6.setEnabled(((Barracks)building).activeProject == null && playerState.wood >= 30.0 && playerState.iron >= 20.0 && playerState.gold >= 15.0);
            jMenuItem6.addActionListener(arg_0 -> this.lambda$showFacilityMenu$13(playerState, (Barracks)building, arg_0));
            jPopupMenu.add(jMenuItem6);
            String string2 = String.format("Train Builder (3 Pts) [%.0f W, %.0f I]", 30.0, 10.0);
            JMenuItem jMenuItem7 = new JMenuItem(string2);
            jMenuItem7.setEnabled(((Barracks)building).activeProject == null && playerState.wood >= 30.0 && playerState.iron >= 10.0);
            jMenuItem7.addActionListener(arg_0 -> this.lambda$showFacilityMenu$14(playerState, (Barracks)building, arg_0));
            jPopupMenu.add(jMenuItem7);
            String string3 = String.format("Train Nuke Carrier (8 Pts) [%.0f W, %.0f I, %.0f U]", 50.0, 50.0, 50.0);
            JMenuItem jMenuItem8 = new JMenuItem(string3);
            jMenuItem8.setEnabled(((Barracks)building).activeProject == null && playerState.wood >= 50.0 && playerState.iron >= 50.0 && playerState.uranium >= 50.0);
            jMenuItem8.addActionListener(arg_0 -> this.lambda$showFacilityMenu$15(playerState, (Barracks)building, arg_0));
            jPopupMenu.add(jMenuItem8);
            if (((Barracks)building).activeProject != null) {
                JMenuItem jMenuItem9 = new JMenuItem("Cancel Current Queue (No Refund)");
                jMenuItem9.addActionListener(arg_0 -> this.lambda$showFacilityMenu$16((Barracks)building, arg_0));
                jPopupMenu.add(jMenuItem9);
            }
        }
        if (building instanceof VehicleFactory && tile.deadZoneTimer == 0) {
            object3 = (VehicleFactory)building;
            jPopupMenu.addSeparator();
            String string = String.format("Build Tank (8 Pts) [%.0f I, %.0f G]", 80.0, 40.0);
            jMenuItem2 = new JMenuItem(string);
            jMenuItem2.setEnabled(((VehicleFactory)building).activeProject == null && playerState.iron >= 80.0 && playerState.gold >= 40.0);
            jMenuItem2.addActionListener(arg_0 -> this.lambda$showFacilityMenu$17(playerState, (VehicleFactory)building, arg_0));
            jPopupMenu.add(jMenuItem2);
            object2 = String.format("Build Sky Ship (6 Pts) [%.0f I, %.0f G]", 60.0, 30.0);
            jMenuItem = new JMenuItem((String)object2);
            jMenuItem.setEnabled(((VehicleFactory)building).activeProject == null && playerState.iron >= 60.0 && playerState.gold >= 30.0);
            jMenuItem.addActionListener(arg_0 -> this.lambda$showFacilityMenu$18(playerState, (VehicleFactory)building, arg_0));
            jPopupMenu.add(jMenuItem);
            if (((VehicleFactory)building).activeProject != null) {
                object = new JMenuItem("Cancel Current Queue (No Refund)");
                ((AbstractButton)object).addActionListener(arg_0 -> this.lambda$showFacilityMenu$19((VehicleFactory)building, arg_0));
                jPopupMenu.add((JMenuItem)object);
            }
        }
        jPopupMenu.show(this, n3, n4);
    }

    private void hookMouse() {
        this.addMouseMotionListener(new MouseMotionAdapter(){

            @Override
            public void mouseMoved(MouseEvent mouseEvent) {
                if (HexGrid.this.isAITurnProcessing) {
                    return;
                }
                for (int i = 0; i < 50; ++i) {
                    for (int j = 0; j < 32; ++j) {
                        HexGrid.this.grid[i][j].hovered = false;
                    }
                }
                int[] nArray = HexGrid.this.pixelToHex(mouseEvent.getX() + HexGrid.this.cameraX, mouseEvent.getY() + HexGrid.this.cameraY);
                if (nArray != null) {
                    HexGrid.this.grid[nArray[0]][nArray[1]].hovered = true;
                    HexGrid.this.currentPath = HexGrid.this.selectedUnit != null && HexGrid.this.selectedUnit.ownerIndex == HexGrid.this.currentPlayerIndex && !HexGrid.this.isBuildMode && !HexGrid.this.isUnloadMode ? HexGrid.this.calculatePath(HexGrid.this.selectedUnit, HexGrid.this.selectedUnit.col, HexGrid.this.selectedUnit.row, nArray[0], nArray[1], HexGrid.this.selectedUnit.movesLeft, HexGrid.this.currentPlayerIndex) : null;
                } else {
                    HexGrid.this.currentPath = null;
                }
                HexGrid.this.repaint();
            }
        });
        this.addMouseListener(new MouseAdapter(){

            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                if (HexGrid.this.isAITurnProcessing) {
                    return;
                }
                int[] nArray = HexGrid.this.pixelToHex(mouseEvent.getX() + HexGrid.this.cameraX, mouseEvent.getY() + HexGrid.this.cameraY);
                if (nArray != null) {
                    int n;
                    Unit unit2;
                    int n2 = nArray[0];
                    int n3 = nArray[1];
                    if (mouseEvent.getClickCount() == 2) {
                        final Unit f_unit2 = HexGrid.this.getUnitAt(n2, n3);
                        unit2 = f_unit2;
                        if (f_unit2 instanceof NukeCarrier && unit2.ownerIndex == HexGrid.this.currentPlayerIndex) {
                            JPopupMenu jPopupMenu = new JPopupMenu("Tactical Weapon");
                            JMenuItem jMenuItem = new JMenuItem("DETONATE WARHEAD");
                            jMenuItem.setForeground(Color.RED);
                            jMenuItem.setFont(new Font("SansSerif", 1, 12));
                            jMenuItem.addActionListener(actionEvent -> {
                                HexGrid.this.detonateNuke(n2, n3);
                                HexGrid.this.units.remove(f_unit2);
                                HexGrid.this.selectedUnit = null;
                                HexGrid.this.updateVision();
                                HexGrid.this.repaint();
                            });
                            jPopupMenu.add(jMenuItem);
                            jPopupMenu.show(HexGrid.this, mouseEvent.getX(), mouseEvent.getY());
                            return;
                        }
                        Building building = HexGrid.this.buildings[n2][n3];
                        if (building != null && building.ownerIndex == HexGrid.this.currentPlayerIndex && building.usesPower) {
                            building.isOperational = !building.isOperational;
                            HexGrid.this.repaint();
                            return;
                        }
                        if (unit2 instanceof SkyShip && unit2.ownerIndex == HexGrid.this.currentPlayerIndex) {
                            SkyShip unit5 = (SkyShip)unit2;
                            if (!unit5.cargo.isEmpty()) {
                                JPopupMenu jPopupMenu = new JPopupMenu("Unload Cargo Bay");
                                for (int i = 0; i < unit5.cargo.size(); ++i) {
                                    Unit unit = unit5.cargo.get(i);
                                    JMenuItem jMenuItem = new JMenuItem("Unload: " + unit.typeName);
                                    jMenuItem.addActionListener(actionEvent -> {
                                        HexGrid.this.unitToUnload = unit;
                                        HexGrid.this.shipToUnloadFrom = unit5;
                                        HexGrid.this.isUnloadMode = true;
                                        HexGrid.this.selectedUnit = null;
                                        HexGrid.this.repaint();
                                    });
                                    jPopupMenu.add(jMenuItem);
                                }
                                jPopupMenu.show(HexGrid.this, mouseEvent.getX(), mouseEvent.getY());
                                return;
                            }
                        }
                    }
                    if (HexGrid.this.isUnloadMode) {
                        int n4;
                        if (HexGrid.this.unitToUnload != null && HexGrid.this.shipToUnloadFrom != null && (n4 = HexGrid.this.getDistance(HexGrid.this.shipToUnloadFrom.col, HexGrid.this.shipToUnloadFrom.row, n2, n3)) > 0 && n4 <= 2 && HexGrid.this.grid[n2][n3].terrain != Tile.Terrain.OCEAN && HexGrid.this.getUnitAt(n2, n3) == null) {
                            HexGrid.this.unitToUnload.col = n2;
                            HexGrid.this.unitToUnload.row = n3;
                            HexGrid.this.unitToUnload.movesLeft = 0;
                            HexGrid.this.units.add(HexGrid.this.unitToUnload);
                            HexGrid.this.shipToUnloadFrom.cargo.remove(HexGrid.this.unitToUnload);
                        }
                        HexGrid.this.isUnloadMode = false;
                        HexGrid.this.unitToUnload = null;
                        HexGrid.this.shipToUnloadFrom = null;
                        HexGrid.this.updateVision();
                        HexGrid.this.repaint();
                        return;
                    }
                    if (HexGrid.this.isBuildMode) {
                        if (HexGrid.this.isValidBuildTile(n2, n3, HexGrid.this.selectedUnit)) {
                            HexGrid.this.showBuildMenuAt(n2, n3, mouseEvent.getX(), mouseEvent.getY(), HexGrid.this.selectedUnit);
                        }
                        HexGrid.this.isBuildMode = false;
                        HexGrid.this.repaint();
                        return;
                    }
                    final Unit f_unit2 = HexGrid.this.getUnitAt(n2, n3);
                        unit2 = f_unit2;
                    if (HexGrid.this.selectedUnit != null && HexGrid.this.selectedUnit.ownerIndex == HexGrid.this.currentPlayerIndex && unit2 != null && unit2.ownerIndex != HexGrid.this.currentPlayerIndex && (n = HexGrid.this.getDistance(HexGrid.this.selectedUnit.col, HexGrid.this.selectedUnit.row, n2, n3)) <= HexGrid.this.selectedUnit.attackRange && HexGrid.this.selectedUnit.canAttack && HexGrid.this.selectedUnit.movesLeft >= 1) {
                        --HexGrid.this.selectedUnit.movesLeft;
                        HexGrid.this.resolveCombat(HexGrid.this.selectedUnit, unit2, n);
                        if (HexGrid.this.selectedUnit.currentHp <= 0) {
                            HexGrid.this.selectedUnit = null;
                        }
                        HexGrid.this.currentPath = null;
                        HexGrid.this.repaint();
                        return;
                    }
                    if (HexGrid.this.selectedUnit != null && HexGrid.this.selectedUnit.ownerIndex == HexGrid.this.currentPlayerIndex && HexGrid.this.currentPath != null && !HexGrid.this.currentPath.isEmpty()) {
                        Point point = HexGrid.this.currentPath.get(HexGrid.this.currentPath.size() - 1);
                        if (point.x == n2 && point.y == n3) {
                            Unit unit = HexGrid.this.getUnitAt(n2, n3);
                            if (unit instanceof SkyShip && unit.ownerIndex == HexGrid.this.currentPlayerIndex && HexGrid.this.selectedUnit != unit) {
                                SkyShip skyShip = (SkyShip)unit;
                                if (skyShip.cargo.size() < skyShip.capacity) {
                                    HexGrid.this.selectedUnit.col = n2;
                                    HexGrid.this.selectedUnit.row = n3;
                                    HexGrid.this.selectedUnit.movesLeft -= HexGrid.this.currentPath.size();
                                    skyShip.cargo.add(HexGrid.this.selectedUnit);
                                    HexGrid.this.units.remove(HexGrid.this.selectedUnit);
                                    HexGrid.this.selectedUnit = null;
                                    HexGrid.this.currentPath = null;
                                    HexGrid.this.updateVision();
                                    HexGrid.this.repaint();
                                    return;
                                }
                            }
                            HexGrid.this.selectedUnit.col = n2;
                            HexGrid.this.selectedUnit.row = n3;
                            HexGrid.this.selectedUnit.movesLeft -= HexGrid.this.currentPath.size();
                            HexGrid.this.selectedCol = HexGrid.this.selectedUnit.col;
                            HexGrid.this.selectedRow = HexGrid.this.selectedUnit.row;
                            HexGrid.this.currentPath = null;
                            HexGrid.this.updateVision();
                            HexGrid.this.repaint();
                            return;
                        }
                    }
                    if (HexGrid.this.selectedCol == n2 && HexGrid.this.selectedRow == n3 && HexGrid.this.buildings[n2][n3] != null) {
                        HexGrid.this.showFacilityMenu(n2, n3, mouseEvent.getX(), mouseEvent.getY());
                    }
                    if (HexGrid.this.selectedCol >= 0) {
                        HexGrid.this.grid[HexGrid.this.selectedCol][HexGrid.this.selectedRow].selected = false;
                    }
                    HexGrid.this.selectedCol = n2;
                    HexGrid.this.selectedRow = n3;
                    HexGrid.this.grid[HexGrid.this.selectedCol][HexGrid.this.selectedRow].selected = true;
                    HexGrid.this.selectedUnit = null;
                    for (Unit unit : HexGrid.this.units) {
                        if (unit.col != n2 || unit.row != n3 || unit.ownerIndex != HexGrid.this.currentPlayerIndex) continue;
                        HexGrid.this.selectedUnit = unit;
                        break;
                    }
                    HexGrid.this.currentPath = null;
                }
                HexGrid.this.repaint();
            }
        });
    }

    private void updateVision() {
        int n;
        int n2;
        if (this.gameMode == GameMode.AI_VS_AI) {
            for (n2 = 0; n2 < 50; ++n2) {
                for (n = 0; n < 32; ++n) {
                    this.grid[n2][n].isVisible = true;
                    this.grid[n2][n].isExplored = true;
                }
            }
            return;
        }
        for (n2 = 0; n2 < 50; ++n2) {
            for (n = 0; n < 32; ++n) {
                this.grid[n2][n].isVisible = false;
            }
        }
        for (n2 = 0; n2 < 50; ++n2) {
            for (n = 0; n < 32; ++n) {
                if (this.grid[n2][n].terrain != Tile.Terrain.BASE || !this.grid[n2][n].isPlayer) continue;
                this.revealHexes(n2, n, 6);
            }
        }
        for (Unit unit : this.units) {
            if (unit.ownerIndex != 0) continue;
            this.revealHexes(unit.col, unit.row, unit.visionRadius);
        }
    }

    private void revealHexes(int n, int n2, int n3) {
        LinkedList<Point> linkedList = new LinkedList<Point>();
        HashMap<Point, Integer> hashMap = new HashMap<Point, Integer>();
        Point point = new Point(n, n2);
        linkedList.add(point);
        hashMap.put(point, 0);
        this.grid[n][n2].isVisible = true;
        this.grid[n][n2].isExplored = true;
        while (!linkedList.isEmpty()) {
            Point point2 = (Point)linkedList.poll();
            int n4 = (Integer)hashMap.get(point2);
            if (n4 >= n3) continue;
            for (int[] nArray : this.getNeighbors(point2.x, point2.y)) {
                Point point3 = new Point(nArray[0], nArray[1]);
                int n5 = n4 + 1;
                if (hashMap.containsKey(point3) && n5 >= (Integer)hashMap.get(point3)) continue;
                hashMap.put(point3, n5);
                linkedList.add(point3);
                this.grid[nArray[0]][nArray[1]].isVisible = true;
                this.grid[nArray[0]][nArray[1]].isExplored = true;
            }
        }
    }

    private List<Point> calculatePath(Unit unit, int n, int n2, int n3, int n4, int n5, int n6) {
        Object object;
        Object object2;
        Point point;
        boolean bl = unit instanceof SkyShip;
        if (n == n3 && n2 == n4) {
            return null;
        }
        if (!this.grid[n3][n4].isExplored) {
            return null;
        }
        if (!bl && this.grid[n3][n4].terrain == Tile.Terrain.OCEAN) {
            return null;
        }
        Unit unit2 = this.getUnitAt(n3, n4);
        if (unit2 != null) {
            if (unit2.ownerIndex != n6) {
                return null;
            }
            if (!(unit2 instanceof SkyShip) || ((SkyShip)unit2).cargo.size() >= ((SkyShip)unit2).capacity) {
                return null;
            }
        }
        LinkedList<Point> linkedList = new LinkedList<Point>();
        HashMap<Point, Point> hashMap = new HashMap<Point, Point>();
        HashMap<Point, Integer> hashMap2 = new HashMap<Point, Integer>();
        Point point2 = new Point(n, n2);
        linkedList.add(point2);
        hashMap.put(point2, null);
        hashMap2.put(point2, 0);
        while (!linkedList.isEmpty()) {
            point = (Point)linkedList.poll();
            if (point.x == n3 && point.y == n4) break;
            for (int[] nextObj : this.getNeighbors(point.x, point.y)) {
                Point point3;
                int n7;
                Unit unit3;
                if (!this.grid[nextObj[0]][nextObj[1]].isExplored || !bl && this.grid[nextObj[0]][nextObj[1]].terrain == Tile.Terrain.OCEAN || (unit3 = this.getUnitAt((int)nextObj[0], (int)nextObj[1])) != null && unit3.ownerIndex != n6 || (n7 = (Integer)hashMap2.get(point) + 1) > n5 || hashMap2.containsKey(point3 = new Point((int)nextObj[0], (int)nextObj[1])) && n7 >= (Integer)hashMap2.get(point3)) continue;
                hashMap2.put(point3, n7);
                hashMap.put(point3, point);
                linkedList.add(point3);
            }
        }
        if (!hashMap.containsKey(point = new Point(n3, n4))) {
            return null;
        }
        List<Point> finalPath = new ArrayList<Point>();
        Point pt = point;
        while (pt != null && !pt.equals(point2)) {
            finalPath.add(pt);
            pt = (Point)hashMap.get(pt);
        }
        Collections.reverse(finalPath);
        return finalPath;
    }

    private void generateMap() {
        int n;
        int n2;
        Random random = new Random();
        for (int i = 0; i < 50; ++i) {
            for (int j = 0; j < 32; ++j) {
                this.grid[i][j] = new Tile(Tile.Terrain.OCEAN);
            }
        }
        double[][] dArrayArray = new double[][]{{11.0, 8.64}, {39.0, 8.64}, {11.0, 23.36}, {39.0, 23.36}};
        
        if (gameMode == GameMode.DEV_MODE) {
            dArrayArray = new double[][]{{20.0, 16.0}, {30.0, 16.0}};
            players[0].wood = 9999;
            players[0].iron = 9999;
            players[0].gold = 9999;
            players[0].uranium = 9999;
        } else if (gameMode == GameMode.AI_VS_AI) {
            dArrayArray = new double[][]{{15.0, 16.0}, {35.0, 16.0}};
            players[0].isAI = true;
            players[0].name = "Player 1 (AI)";
        }
        double d = 8.0;
        if (this.gameMode == GameMode.AI_VS_AI) d = 9.0;
        for (n2 = 0; n2 < 50; ++n2) {
            for (n = 0; n < 32; ++n) {
                double d2 = Double.MAX_VALUE;
                for (double[] dArray : dArrayArray) {
                    double d3 = (double)n2 - dArray[0];
                    double d4 = (double)n - dArray[1];
                    if (!(Math.sqrt(d3 * d3 + d4 * d4) < d2)) continue;
                    d2 = Math.sqrt(d3 * d3 + d4 * d4);
                }
                double d5 = d2 + random.nextDouble() * 2.0 - 1.0;
                Tile tile = d5 < d * 0.2 ? new Tile(Tile.Terrain.MOUNTAIN) : (d5 < d * 0.55 ? new Tile(Tile.Terrain.FOREST) : (d5 < d * 0.8 ? new Tile(Tile.Terrain.PLAINS) : (d5 < d ? new Tile(random.nextBoolean() ? Tile.Terrain.TUNDRA : Tile.Terrain.DESERT) : new Tile(Tile.Terrain.OCEAN))));
                tile.generateYields(random);
                this.grid[n2][n] = tile;
            }
        }
        for (n2 = 0; n2 < dArrayArray.length; ++n2) {
            int n3;
            n = (int)Math.round(dArrayArray[n2][0]);
            if (!this.inBounds(n, n3 = (int)Math.round(dArrayArray[n2][1]))) continue;
            this.grid[n][n3] = new Tile(Tile.Terrain.BASE);
            this.grid[n][n3].generateYields(random);
            if (this.inBounds(n + 1, n3)) {
                this.grid[n + 1][n3] = new Tile(Tile.Terrain.MOUNTAIN);
                this.grid[n + 1][n3].yieldIron = 2.0;
                this.grid[n + 1][n3].yieldGold = 2.0;
            }
            if (this.inBounds(n, n3 + 1)) {
                this.grid[n][n3 + 1] = new Tile(Tile.Terrain.FOREST);
                this.grid[n][n3 + 1].generateYields(random);
            }
            if (n2 == 0 && gameMode != GameMode.AI_VS_AI) {
                this.grid[n][n3].isPlayer = true;
            }
            this.units.add(new HQ(n, n3, n2));
            if (gameMode == GameMode.DEV_MODE && n2 == 1) {
                this.units.add(new DummyTarget(n, n3 - 1, n2));
                this.units.add(new DummyTarget(n, n3 + 1, n2));
                this.units.add(new DummyTarget(n + 1, n3, n2));
                this.units.add(new DummyTarget(n - 1, n3, n2));
            } else {
                this.spawnUnits(n, n3, n2);
            }
        }
        this.updateVision();
    }

    private void spawnUnits(int n, int n2, int n3) {
        ArrayList<int[]> arrayList = new ArrayList<int[]>();
        for (int[] nArray : this.getNeighbors(n, n2)) {
            if (this.grid[nArray[0]][nArray[1]].terrain == Tile.Terrain.OCEAN) continue;
            arrayList.add(nArray);
        }
        if (arrayList.size() > 0) {
            this.units.add(new Scout(((int[])arrayList.get(0))[0], ((int[])arrayList.get(0))[1], n3));
        } else {
            this.units.add(new Scout(n, n2, n3));
        }
        if (arrayList.size() > 1) {
            this.units.add(new Builder(((int[])arrayList.get(1))[0], ((int[])arrayList.get(1))[1], n3));
        } else {
            this.units.add(new Builder(n, n2, n3));
        }
    }

    public List<int[]> getNeighbors(int n, int n2) {
        int[][] nArray = n % 2 == 0 ? NB_EVEN : NB_ODD;
        ArrayList<int[]> arrayList = new ArrayList<int[]>();
        for (int[] nArray2 : nArray) {
            int n3 = n + nArray2[0];
            int n4 = n2 + nArray2[1];
            if (!this.inBounds(n3, n4)) continue;
            arrayList.add(new int[]{n3, n4});
        }
        return arrayList;
    }

    public boolean inBounds(int n, int n2) {
        return n >= 0 && n < 50 && n2 >= 0 && n2 < 32;
    }

    private int visColMin() {
        return Math.max(0, (int)((double)(this.cameraX - 32 - 22) / 33.0));
    }

    private int visColMax() {
        return Math.min(49, (int)((double)(this.cameraX + 980 - 32 + 22) / 33.0) + 1);
    }

    private int visRowMin() {
        return Math.max(0, (int)((double)(this.cameraY - OFFSET_Y - 22) / ROW_STRIDE) - 1);
    }

    private int visRowMax() {
        return Math.min(31, (int)((double)(this.cameraY + 750 - OFFSET_Y + 22) / ROW_STRIDE) + 2);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D graphics2D = (Graphics2D)graphics;
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.translate(-this.cameraX, -this.cameraY);
        for (int i = this.visColMin(); i <= this.visColMax(); ++i) {
            for (int j = this.visRowMin(); j <= this.visRowMax(); ++j) {
                this.drawHex(graphics2D, i, j);
            }
        }
        for (Unit unit : this.units) {
            this.drawUnit(graphics2D, unit);
        }
        this.drawPath(graphics2D);
        graphics2D.translate(this.cameraX, this.cameraY);
        this.drawHUD(graphics2D);
    }

    private void drawPath(Graphics2D graphics2D) {
        if (this.currentPath == null || this.currentPath.isEmpty() || this.selectedUnit == null) {
            return;
        }
        graphics2D.setColor(new Color(255, 255, 255, 200));
        graphics2D.setStroke(new BasicStroke(3.0f, 1, 1, 0.0f, new float[]{8.0f}, 0.0f));
        Point point = new Point(this.selectedUnit.col, this.selectedUnit.row);
        for (Point point2 : this.currentPath) {
            double d = 32.0 + (double)point.x * 33.0;
            double d2 = (double)OFFSET_Y + (double)point.y * ROW_STRIDE + (point.x % 2 == 1 ? HEX_H / 2.0 : 0.0);
            double d3 = 32.0 + (double)point2.x * 33.0;
            double d4 = (double)OFFSET_Y + (double)point2.y * ROW_STRIDE + (point2.x % 2 == 1 ? HEX_H / 2.0 : 0.0);
            graphics2D.drawLine((int)d, (int)d2, (int)d3, (int)d4);
            graphics2D.fillOval((int)d3 - 5, (int)d4 - 5, 10, 10);
            point = point2;
        }
    }

    private void drawUnit(Graphics2D graphics2D, Unit unit) {
        Object object;
        if (unit.ownerIndex != 0 && !this.grid[unit.col][unit.row].isVisible) {
            return;
        }
        double d = 32.0 + (double)unit.col * 33.0;
        double d2 = (double)OFFSET_Y + (double)unit.row * ROW_STRIDE + (unit.col % 2 == 1 ? HEX_H / 2.0 : 0.0);
        int n = 8;
        boolean isBuilder = "Builder".equals(unit.typeName);
        boolean isSkyShip = unit instanceof SkyShip;
        if (unit == this.selectedUnit) {
            graphics2D.setColor(new Color(255, 215, 0, 150));
            if (isBuilder) {
                graphics2D.fillRect((int)(d - (double)n - 4.0), (int)(d2 - (double)n - 4.0), (n + 4) * 2, (n + 4) * 2);
            } else if (isSkyShip) {
                graphics2D.fill(buildUnitHexPath(d, d2, n + 4));
            } else {
                graphics2D.fillOval((int)(d - (double)n - 4.0), (int)(d2 - (double)n - 4.0), (n + 4) * 2, (n + 4) * 2);
            }
        }
        graphics2D.setColor(this.players[unit.ownerIndex].color);
        if (isBuilder) {
            graphics2D.fillRect((int)(d - (double)n), (int)(d2 - (double)n), n * 2, n * 2);
        } else if (isSkyShip) {
            graphics2D.fill(buildUnitHexPath(d, d2, n));
        } else {
            graphics2D.fillOval((int)(d - (double)n), (int)(d2 - (double)n), n * 2, n * 2);
        }
        graphics2D.setColor(Color.WHITE);
        graphics2D.setStroke(new BasicStroke(1.5f));
        if (isBuilder) {
            graphics2D.drawRect((int)(d - (double)n), (int)(d2 - (double)n), n * 2, n * 2);
        } else if (isSkyShip) {
            graphics2D.draw(buildUnitHexPath(d, d2, n));
        } else {
            graphics2D.drawOval((int)(d - (double)n), (int)(d2 - (double)n), n * 2, n * 2);
        }
        graphics2D.setFont(new Font("Monospaced", 1, 11));
        FontMetrics fontMetrics = graphics2D.getFontMetrics();
        String string = unit.typeName.substring(0, 1);
        graphics2D.drawString(string, (int)(d - (double)fontMetrics.stringWidth(string) / 2.0), (int)(d2 + (double)fontMetrics.getAscent() / 2.0 - 1.0));
        if (unit instanceof SkyShip) {
            object = (SkyShip)unit;
            if (!((SkyShip)object).cargo.isEmpty()) {
                graphics2D.setColor(Color.CYAN);
                graphics2D.setFont(new Font("SansSerif", 1, 10));
                graphics2D.drawString(((SkyShip)object).cargo.size() + "/6", (int)(d + (double)(n / 2)), (int)(d2 - (double)(n / 2)));
            }
        }
        if (unit.currentHp < unit.maxHp) {
            int n2 = 14;
            int n3 = (int)((double)unit.currentHp / (double)unit.maxHp * (double)n2);
            graphics2D.setColor(Color.RED);
            graphics2D.fillRect((int)(d - (double)(n2 / 2)), (int)(d2 + (double)n + 2.0), n2, 3);
            graphics2D.setColor(Color.GREEN);
            graphics2D.fillRect((int)(d - (double)(n2 / 2)), (int)(d2 + (double)n + 2.0), n3, 3);
            graphics2D.setColor(Color.BLACK);
            graphics2D.drawRect((int)(d - (double)(n2 / 2)), (int)(d2 + (double)n + 2.0), n2, 3);
        }
        if (unit.ownerIndex == (this.gameMode == GameMode.AI_VS_AI ? this.viewedPlayerIndex : this.currentPlayerIndex) && unit == this.selectedUnit) {
            object = unit.movesLeft + "/" + unit.maxMoves;
            graphics2D.setFont(new Font("SansSerif", 1, 10));
            graphics2D.drawString((String)object, (int)(d - (double)fontMetrics.stringWidth((String)object) / 2.0) - 2, (int)(d2 - (double)n - 4.0));
        }
    }

    private void drawHex(Graphics2D graphics2D, int n, int n2) {
        int n3;
        double d = 32.0 + (double)n * 33.0;
        double d2 = (double)OFFSET_Y + (double)n2 * ROW_STRIDE + (n % 2 == 1 ? HEX_H / 2.0 : 0.0);
        Path2D path2D = this.buildHexPath(d, d2);
        Tile tile = this.grid[n][n2];
        graphics2D.setColor(tile.getRenderColor());
        graphics2D.fill(path2D);
        boolean bl = this.isValidBuildTile(n, n2, this.selectedUnit);
        boolean bl2 = false;
        if (this.isUnloadMode && this.shipToUnloadFrom != null && (n3 = this.getDistance(this.shipToUnloadFrom.col, this.shipToUnloadFrom.row, n, n2)) > 0 && n3 <= 2 && tile.terrain != Tile.Terrain.OCEAN && this.getUnitAt(n, n2) == null && tile.isExplored) {
            bl2 = true;
        }
        if (bl) {
            graphics2D.setColor(this.isBuildMode ? new Color(255, 215, 0, 95) : new Color(255, 235, 55, 60));
            graphics2D.fill(path2D);
        } else if (bl2) {
            graphics2D.setColor(new Color(0, 255, 255, 60));
            graphics2D.fill(path2D);
        }
        graphics2D.setColor(tile.getRenderBorderColor());
        graphics2D.setStroke(tile.selected ? new BasicStroke(2.5f) : new BasicStroke(1.0f));
        graphics2D.draw(path2D);
        if (bl) {
            graphics2D.setColor(new Color(255, 255, 255, 180));
            graphics2D.setStroke(new BasicStroke(1.5f, 0, 0, 1.0f, new float[]{4.0f, 4.0f}, 0.0f));
            graphics2D.draw(path2D);
        } else if (bl2) {
            graphics2D.setColor(new Color(0, 255, 255, 180));
            graphics2D.setStroke(new BasicStroke(1.5f, 0, 0, 1.0f, new float[]{4.0f, 4.0f}, 0.0f));
            graphics2D.draw(path2D);
        }
        if (tile.isExplored) {
            Building building = this.buildings[n][n2];
            if (building != null && tile.isVisible) {
                graphics2D.setColor(building.color);
                graphics2D.fillRect((int)d - 10, (int)d2 - 16, 20, 11);
                graphics2D.setColor(Color.WHITE);
                graphics2D.setFont(new Font("SansSerif", 1, 9));
                graphics2D.drawString(String.valueOf(building.icon), (int)d - 3, (int)d2 - 7);
            }
            graphics2D.setColor(tile.isVisible ? new Color(0, 0, 0, 150) : new Color(0, 0, 0, 60));
            graphics2D.setFont(new Font("Monospaced", 0, 10));
            String string = tile.terrain.name().substring(0, 2);
            FontMetrics fontMetrics = graphics2D.getFontMetrics();
            graphics2D.drawString(string, (int)(d - (double)fontMetrics.stringWidth(string) / 2.0), (int)(d2 + (double)fontMetrics.getAscent() / 2.0 + 3.0));
        }
    }

    private Path2D buildHexPath(double d, double d2) {
        Path2D.Double double_ = new Path2D.Double();
        for (int i = 0; i < 6; ++i) {
            double d3 = Math.toRadians(60 * i);
            double d4 = d + 22.0 * Math.cos(d3);
            double d5 = d2 + 22.0 * Math.sin(d3);
            if (i == 0) {
                ((Path2D)double_).moveTo(d4, d5);
                continue;
            }
            ((Path2D)double_).lineTo(d4, d5);
        }
        double_.closePath();
        return double_;
    }

    private Path2D buildUnitHexPath(double d, double d2, double r) {
        Path2D.Double double_ = new Path2D.Double();
        for (int i = 0; i < 6; ++i) {
            double d3 = Math.toRadians(60 * i);
            double d4 = d + r * Math.cos(d3);
            double d5 = d2 + r * Math.sin(d3);
            if (i == 0) {
                ((Path2D)double_).moveTo(d4, d5);
                continue;
            }
            ((Path2D)double_).lineTo(d4, d5);
        }
        double_.closePath();
        return double_;
    }

    private int[] pixelToHex(int n, int n2) {
        for (int i = this.visColMin(); i <= this.visColMax(); ++i) {
            for (int j = this.visRowMin(); j <= this.visRowMax(); ++j) {
                double d = 32.0 + (double)i * 33.0;
                double d2 = (double)OFFSET_Y + (double)j * ROW_STRIDE + (i % 2 == 1 ? HEX_H / 2.0 : 0.0);
                if (!this.buildHexPath(d, d2).contains(n, n2)) continue;
                return new int[]{i, j};
            }
        }
        return null;
    }

    private void nextTurn() {
        if (this.isAITurnProcessing) {
            return;
        }
        
        if (gameMode == GameMode.AI_VS_AI) {
            if (this.isPaused) return;
            this.isAITurnProcessing = true;
            this.selectedUnit = null;
            this.currentPath = null;
            this.repaint();
            Timer timer = new Timer(500, new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    while (HexGrid.this.currentPlayerIndex < HexGrid.this.players.length) {
                        if (HexGrid.this.players[HexGrid.this.currentPlayerIndex].isAI) {
                            HexGrid.this.aiEngine.processAITurn(HexGrid.this.currentPlayerIndex);
                        }
                        ++HexGrid.this.currentPlayerIndex;
                    }
                    HexGrid.this.completeTurnCycle();
                    ((Timer)actionEvent.getSource()).stop();
                    // trigger next turn again
                    new Timer(500, e -> HexGrid.this.nextTurn()).start();
                }
            });
            timer.setRepeats(false);
            timer.start();
            return;
        }

        ++this.currentPlayerIndex;
        if (this.currentPlayerIndex > 0 && this.currentPlayerIndex < this.players.length) {
            this.isAITurnProcessing = true;
            this.selectedUnit = null;
            this.currentPath = null;
            this.isBuildMode = false;
            this.isUnloadMode = false;
            this.repaint();
            Timer timer = new Timer(1000, new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    while (HexGrid.this.currentPlayerIndex > 0 && HexGrid.this.currentPlayerIndex < HexGrid.this.players.length) {
                        HexGrid.this.aiEngine.processAITurn(HexGrid.this.currentPlayerIndex);
                        ++HexGrid.this.currentPlayerIndex;
                    }
                    HexGrid.this.completeTurnCycle();
                    ((Timer)actionEvent.getSource()).stop();
                }
            });
            timer.setRepeats(false);
            timer.start();
        } else {
            this.completeTurnCycle();
        }
    }

    private void completeTurnCycle() {
        for (int i = 0; i < 50; ++i) {
            for (int j = 0; j < 32; ++j) {
                Building object = this.buildings[i][j];
                if (object == null || this.grid[i][j].deadZoneTimer != 0) continue;
                object.processTurnProduction(this, this.grid[i][j], i, j);
            }
        }
        ArrayList<Unit> arrayList = new ArrayList<Unit>();
        for (Unit unit : this.units) {
            int n;
            if (this.grid[unit.col][unit.row].deadZoneTimer > 0) {
                unit.currentHp -= 5;
                if (unit.currentHp <= 0) {
                    arrayList.add(unit);
                }
            } else if (unit.currentHp < unit.maxHp && (n = (int)(20.0 * this.grid[unit.col][unit.row].buildingEfficiency)) > 0) {
                unit.currentHp += n;
                if (unit.currentHp > unit.maxHp) {
                    unit.currentHp = unit.maxHp;
                }
            }
            unit.resetMoves();
        }
        this.units.removeAll(arrayList);
        for (int i = 0; i < 50; ++i) {
            for (int j = 0; j < 32; ++j) {
                if (this.grid[i][j].deadZoneTimer <= 0) continue;
                --this.grid[i][j].deadZoneTimer;
            }
        }
        this.currentPlayerIndex = 0;
        ++this.turnNumber;
        this.isAITurnProcessing = false;
        this.isBuildMode = false;
        this.updateVision();
        this.repaint();
    }

    private PlayerState getProjectedYields(int n) {
        PlayerState playerState = new PlayerState("", Color.WHITE, false);
        playerState.coal = 0.0;
        playerState.gold = 0.0;
        playerState.iron = 0.0;
        playerState.wood = 0.0;
        playerState.uranium = 0.0;
        playerState.energy = 0.0;
        PlayerState playerState2 = this.players[n];
        double d = playerState2.coal;
        double d2 = playerState2.wood;
        double d3 = playerState2.uranium;
        for (int i = 0; i < 50; ++i) {
            for (int j = 0; j < 32; ++j) {
                Building building = this.buildings[i][j];
                Tile tile = this.grid[i][j];
                if (building == null || building.ownerIndex != n || tile.deadZoneTimer != 0) continue;
                double d4 = building.getEffectiveEfficiency(tile);
                if (building.usesPower && !building.isOperational) {
                    d4 *= 0.5;
                }
                if (building instanceof LoggerStation) {
                    double d5 = tile.yieldWood > 0.0 ? tile.yieldWood : 0.4;
                    playerState.wood += 20.0 * d5 * d4 * 1.0;
                    if (!building.isOperational) continue;
                    playerState.energy -= 5.0;
                    continue;
                }
                if (building instanceof MiningStation) {
                    if (building.isOperational) {
                        playerState.energy -= 15.0;
                    }
                    playerState.coal += 20.0 * tile.yieldCoal * 1.0 * d4;
                    playerState.iron += 10.0 * tile.yieldIron * 1.0 * d4;
                    playerState.gold += 20.0 * tile.yieldGold * 1.0 * d4;
                    playerState.uranium += 5.0 * tile.yieldUranium * 1.0 * d4;
                    continue;
                }
                if (building instanceof BurningStation) {
                    BurningStation burningStation = (BurningStation)building;
                    if (burningStation.activeFuel == BurningStation.FuelType.COAL && d >= 5.0) {
                        playerState.coal -= 5.0;
                        d -= 5.0;
                        playerState.energy += 25.0 * d4;
                        continue;
                    }
                    if (burningStation.activeFuel != BurningStation.FuelType.WOOD || !(d2 >= 15.0)) continue;
                    playerState.wood -= 15.0;
                    d2 -= 15.0;
                    playerState.energy += 15.0 * d4;
                    continue;
                }
                if (building instanceof SolarPlant) {
                    playerState.energy += 10.0 * d4;
                    continue;
                }
                if (building instanceof NuclearPlant) {
                    if (!(d3 >= 10.0)) continue;
                    playerState.uranium -= 10.0;
                    d3 -= 10.0;
                    playerState.energy += 100.0 * d4;
                    continue;
                }
                if (building instanceof Barracks) {
                    if (!building.isOperational) continue;
                    playerState.energy -= 10.0;
                    continue;
                }
                if (!(building instanceof VehicleFactory) || !building.isOperational) continue;
                playerState.energy -= 20.0;
            }
        }
        return playerState;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(980, 750);
    }

    private int drawResChunk(Graphics2D graphics2D, String string, double d, double d2, int n, int n2) {
        String string2;
        graphics2D.setColor(Color.WHITE);
        String string3 = String.format("%s:%.0f ", string, d);
        graphics2D.drawString(string3, n, n2);
        n += graphics2D.getFontMetrics().stringWidth(string3);
        if (d2 > 0.0) {
            graphics2D.setColor(new Color(100, 255, 100));
            string2 = String.format("(+%.0f) ", d2);
            graphics2D.drawString(string2, n, n2);
            n += graphics2D.getFontMetrics().stringWidth(string2);
        } else if (d2 < 0.0) {
            graphics2D.setColor(new Color(255, 100, 100));
            string2 = String.format("(%.0f) ", d2);
            graphics2D.drawString(string2, n, n2);
            n += graphics2D.getFontMetrics().stringWidth(string2);
        } else {
            graphics2D.setColor(Color.LIGHT_GRAY);
            string2 = "(+0) ";
            graphics2D.drawString(string2, n, n2);
            n += graphics2D.getFontMetrics().stringWidth(string2);
        }
        if (!string.equals("Iron") && !string.equals("Energy")) {
            graphics2D.setColor(Color.WHITE);
            string2 = "| ";
            graphics2D.drawString(string2, n, n2);
            n += graphics2D.getFontMetrics().stringWidth(string2);
        }
        return n;
    }

    private void drawHUD(Graphics2D graphics2D) {
        Object object;
        FontMetrics fontMetrics;
        String string;
        int n;
        int n2;
        int n3;
        String string2;
        PlayerState playerState = this.players[this.gameMode == GameMode.AI_VS_AI ? this.viewedPlayerIndex : this.currentPlayerIndex];
        graphics2D.setColor(new Color(0, 0, 0, 180));
        graphics2D.fillRoundRect(10, 10, 220, 85, 10, 10);
        graphics2D.setColor(playerState.color);
        graphics2D.setFont(new Font("SansSerif", 1, 16));
        graphics2D.drawString("Turn " + this.turnNumber + " | " + playerState.name, 20, 30);
        graphics2D.setColor(Color.LIGHT_GRAY);
        graphics2D.setFont(new Font("SansSerif", 0, 12));
        graphics2D.drawString("Press ENTER to end turn", 20, 48);
        int n4 = this.players.length > 1 ? this.aiEngine.calculateAIAggressivity(1) : 0;
        String string3 = string2 = this.players.length > 1 ? this.aiEngine.getRelativePower(1) : "Unknown";
        graphics2D.setColor(n4 > 50 ? new Color(255, 100, 100) : (n4 > 0 ? new Color(255, 200, 50) : new Color(100, 255, 100)));
        graphics2D.setFont(new Font("Monospaced", 1, 12));
        graphics2D.drawString("AI Threat Level: " + n4 + "%", 20, 65);
        graphics2D.setColor(Color.WHITE);
        graphics2D.drawString("AI Power: " + string2, 20, 80);
        PlayerState playerState2 = this.getProjectedYields(this.gameMode == GameMode.AI_VS_AI ? this.viewedPlayerIndex : this.currentPlayerIndex);
        graphics2D.setColor(new Color(0, 0, 0, 180));
        graphics2D.fillRoundRect(240, 10, 500, 50, 10, 10);
        graphics2D.setFont(new Font("Monospaced", 1, 13));
        int n5 = 255;
        graphics2D.setColor(Color.WHITE);
        graphics2D.drawString("Bank: ", n5, 28);
        n5 += graphics2D.getFontMetrics().stringWidth("Bank: ");
        n5 = this.drawResChunk(graphics2D, "Coal", playerState.coal, playerState2.coal, n5, 28);
        n5 = this.drawResChunk(graphics2D, "Gold", playerState.gold, playerState2.gold, n5, 28);
        this.drawResChunk(graphics2D, "Iron", playerState.iron, playerState2.iron, n5, 28);
        n5 = 255;
        graphics2D.setColor(Color.WHITE);
        graphics2D.drawString("      ", n5, 48);
        n5 += graphics2D.getFontMetrics().stringWidth("      ");
        n5 = this.drawResChunk(graphics2D, "Wood", playerState.wood, playerState2.wood, n5, 48);
        n5 = this.drawResChunk(graphics2D, "Urnm", playerState.uranium, playerState2.uranium, n5, 48);
        this.drawResChunk(graphics2D, "Energy", playerState.energy, playerState2.energy, n5, 48);
        if (this.isBuildMode) {
            n3 = 400;
            n2 = 35;
            n = 490 - n3 / 2;
            graphics2D.setColor(new Color(210, 160, 10, 230));
            graphics2D.fillRoundRect(n, 55, n3, n2, 8, 8);
            graphics2D.setColor(Color.BLACK);
            graphics2D.setFont(new Font("SansSerif", 1, 13));
            string = "CONSTRUCTION MODE: Click a yellow tile to place facility";
            fontMetrics = graphics2D.getFontMetrics();
            graphics2D.drawString(string, n + (n3 - fontMetrics.stringWidth(string)) / 2, 77);
        }
        if (this.isUnloadMode) {
            n3 = 400;
            n2 = 35;
            n = 490 - n3 / 2;
            graphics2D.setColor(new Color(10, 210, 160, 230));
            graphics2D.fillRoundRect(n, 55, n3, n2, 8, 8);
            graphics2D.setColor(Color.BLACK);
            graphics2D.setFont(new Font("SansSerif", 1, 13));
            string = "DEPLOYMENT: Click a highlighted tile to unload";
            fontMetrics = graphics2D.getFontMetrics();
            graphics2D.drawString(string, n + (n3 - fontMetrics.stringWidth(string)) / 2, 77);
        }
        if (this.isAITurnProcessing) {
            n3 = 320;
            n2 = 75;
            n = 490 - n3 / 2;
            graphics2D.setColor(new Color(150, 30, 30, 220));
            graphics2D.fillRoundRect(n, 60, n3, n2, 15, 15);
            graphics2D.setColor(Color.WHITE);
            graphics2D.setStroke(new BasicStroke(2.0f));
            graphics2D.drawRoundRect(n, 60, n3, n2, 15, 15);
            graphics2D.setFont(new Font("SansSerif", 1, 22));
            string = "Enemy Phase...";
            fontMetrics = graphics2D.getFontMetrics();
            graphics2D.drawString(string, n + (n3 - fontMetrics.stringWidth(string)) / 2, 90);
            object = "Threat Level: " + n4 + "%";
            graphics2D.setFont(new Font("SansSerif", 0, 14));
            graphics2D.drawString((String)object, n + (n3 - graphics2D.getFontMetrics().stringWidth((String)object)) / 2, 115);
        }
        if (this.isPaused && this.gameMode == GameMode.AI_VS_AI) {
            graphics2D.setColor(Color.YELLOW);
            graphics2D.setFont(new Font("SansSerif", 1, 36));
            graphics2D.drawString("PAUSED", 420, 375);
        }
        if (this.selectedCol >= 0) {
            Tile tile = this.grid[this.selectedCol][this.selectedRow];
            Building building = this.buildings[this.selectedCol][this.selectedRow];
            n = 110;
            if (building != null) {
                n = 125 + building.getProductionInfo(tile).size() * 15;
            }
            graphics2D.setColor(new Color(0, 0, 0, 190));
            graphics2D.fillRoundRect(10, 750 - n - 15, 290, n, 10, 10);
            graphics2D.setColor(Color.WHITE);
            graphics2D.setFont(new Font("Monospaced", 1, 13));
            string = tile.isExplored ? tile.terrain.name() : "UNKNOWN";
            graphics2D.drawString("Tile: " + string + " [" + this.selectedCol + ", " + this.selectedRow + "]", 20, 750 - n + 20);
            graphics2D.setFont(new Font("Monospaced", 0, 12));
            if (tile.isExplored) {
                double d;
                int n6 = 750 - n + 40;
                if (tile.deadZoneTimer > 0) {
                    graphics2D.setColor(Color.GREEN);
                    graphics2D.drawString("STATUS: DEAD ZONE (" + tile.deadZoneTimer + " turns left)", 20, n6);
                    n6 += 15;
                }
                if (tile.yieldCoal > 0.0) {
                    graphics2D.drawString(String.format("Coal: %.1f", tile.yieldCoal), 20, n6);
                    n6 += 15;
                }
                if (tile.yieldIron > 0.0) {
                    graphics2D.drawString(String.format("Iron: %.1f", tile.yieldIron), 20, n6);
                    n6 += 15;
                }
                if (tile.yieldWood > 0.0) {
                    graphics2D.drawString(String.format("Wood: %.1f", tile.yieldWood), 20, n6);
                    n6 += 15;
                }
                if (tile.yieldGold > 0.0) {
                    graphics2D.drawString(String.format("Gold: %.1f", tile.yieldGold), 20, n6);
                    n6 += 15;
                }
                if (tile.yieldUranium > 0.0) {
                    graphics2D.drawString(String.format("Urnm: %.2f", tile.yieldUranium), 20, n6);
                    n6 += 15;
                }
                if (tile.yieldEnergy > 0.0) {
                    graphics2D.drawString("Solar: 100%", 20, n6);
                    n6 += 15;
                }
                double d2 = d = building != null ? building.getEffectiveEfficiency(tile) : tile.buildingEfficiency;
                if (tile.deadZoneTimer > 0) {
                    d = 0.0;
                } else if (building != null && building.usesPower && !building.isOperational) {
                    d *= 0.5;
                }
                graphics2D.setColor(d < 1.0 ? new Color(255, 150, 150) : Color.WHITE);
                graphics2D.drawString(String.format("Efficiency: %.0f%%", d * 100.0), 140, 750 - n + 40);
                if (building != null) {
                    graphics2D.setColor(Color.GREEN);
                    graphics2D.drawString("Facility: " + building.name, 140, 750 - n + 55);
                    if (building.usesPower && tile.deadZoneTimer == 0) {
                        graphics2D.setColor(building.isOperational ? new Color(100, 255, 100) : new Color(255, 200, 50));
                        graphics2D.drawString(building.isOperational ? "[OPERATIONAL]" : "[STANDBY]", 140, 750 - n + 70);
                    } else if (tile.deadZoneTimer > 0) {
                        graphics2D.setColor(Color.RED);
                        graphics2D.drawString("[CRITICAL RADIATION]", 140, 750 - n + 70);
                    }
                    graphics2D.setFont(new Font("Monospaced", 1, 12));
                    int n7 = 750 - n + 88;
                    if (tile.deadZoneTimer == 0) {
                        for (String string4 : building.getProductionInfo(tile)) {
                            if (string4.startsWith("+")) {
                                graphics2D.setColor(new Color(100, 255, 100));
                            } else if (string4.startsWith("-")) {
                                graphics2D.setColor(new Color(255, 100, 100));
                            } else {
                                graphics2D.setColor(Color.LIGHT_GRAY);
                            }
                            graphics2D.drawString(string4, 140, n7);
                            n7 += 15;
                        }
                    }
                }
            }
        }
        graphics2D.setColor(new Color(255, 255, 255, 120));
        graphics2D.setFont(new Font("Monospaced", 0, 11));
        graphics2D.drawString("Arrows: Scroll | 'B': Build | Double-Click: Power/Unload/Nuke | Click Enemy: Attack", 420, 740);
        int n8 = 120;
        int n9 = 8;
        n = 980 - n8 - 14;
        graphics2D.setColor(new Color(255, 255, 255, 40));
        graphics2D.fillRoundRect(n, 14, n8, n9, 4, 4);
        graphics2D.setColor(new Color(255, 255, 255, 150));
        graphics2D.fillRoundRect(n + (int)((float)this.cameraX / 713.0f * (float)(n8 - 30)), 14, 30, n9, 4, 4);
        graphics2D.setColor(new Color(255, 255, 255, 40));
        graphics2D.fillRoundRect(958, 30, n9, n8, 4, 4);
        graphics2D.setColor(new Color(255, 255, 255, 150));
        graphics2D.fillRoundRect(958, 30 + (int)((float)this.cameraY / (float)(this.worldH - 750) * (float)(n8 - 30)), n9, 30, 4, 4);
        boolean bl = false;
        int n10 = 0;
        for (Unit object2 : this.units) {
            if (!(object2 instanceof HQ)) continue;
            if (object2.ownerIndex == 0) {
                bl = true;
                continue;
            }
            ++n10;
        }
        if (!bl) {
            graphics2D.setColor(new Color(0, 0, 0, 200));
            graphics2D.fillRect(0, 0, 980, 750);
            graphics2D.setColor(Color.RED);
            graphics2D.setFont(new Font("SansSerif", 1, 48));
            object = "DEFEAT: YOUR HQ WAS DESTROYED";
            FontMetrics fontMetrics2 = graphics2D.getFontMetrics();
            graphics2D.drawString((String)object, (980 - fontMetrics2.stringWidth((String)object)) / 2, 375);
        } else if (n10 == 0) {
            graphics2D.setColor(new Color(0, 0, 0, 200));
            graphics2D.fillRect(0, 0, 980, 750);
            graphics2D.setColor(Color.GREEN);
            graphics2D.setFont(new Font("SansSerif", 1, 48));
            object = "VICTORY: ALL ENEMY HQs DESTROYED";
            FontMetrics fontMetrics3 = graphics2D.getFontMetrics();
            graphics2D.drawString((String)object, (980 - fontMetrics3.stringWidth((String)object)) / 2, 375);
        }
    }

    private /* synthetic */ void lambda$showFacilityMenu$19(VehicleFactory vehicleFactory, ActionEvent actionEvent) {
        vehicleFactory.activeProject = null;
        this.repaint();
    }

    private /* synthetic */ void lambda$showFacilityMenu$18(PlayerState playerState, VehicleFactory vehicleFactory, ActionEvent actionEvent) {
        playerState.iron -= 60.0;
        playerState.gold -= 30.0;
        vehicleFactory.startManufacturing("Sky Ship", 6);
        this.repaint();
    }

    private /* synthetic */ void lambda$showFacilityMenu$17(PlayerState playerState, VehicleFactory vehicleFactory, ActionEvent actionEvent) {
        playerState.iron -= 80.0;
        playerState.gold -= 40.0;
        vehicleFactory.startManufacturing("Tank", 8);
        this.repaint();
    }

    private /* synthetic */ void lambda$showFacilityMenu$16(Barracks barracks, ActionEvent actionEvent) {
        barracks.activeProject = null;
        this.repaint();
    }

    private /* synthetic */ void lambda$showFacilityMenu$15(PlayerState playerState, Barracks barracks, ActionEvent actionEvent) {
        playerState.wood -= 50.0;
        playerState.iron -= 50.0;
        playerState.uranium -= 50.0;
        barracks.startTraining("Nuke Carrier", 8);
        this.repaint();
    }

    private /* synthetic */ void lambda$showFacilityMenu$14(PlayerState playerState, Barracks barracks, ActionEvent actionEvent) {
        playerState.wood -= 30.0;
        playerState.iron -= 10.0;
        barracks.startTraining("Builder", 3);
        this.repaint();
    }

    private /* synthetic */ void lambda$showFacilityMenu$13(PlayerState playerState, Barracks barracks, ActionEvent actionEvent) {
        playerState.wood -= 30.0;
        playerState.iron -= 20.0;
        playerState.gold -= 15.0;
        barracks.startTraining("Mortar", 3);
        this.repaint();
    }

    private /* synthetic */ void lambda$showFacilityMenu$12(PlayerState playerState, Barracks barracks, ActionEvent actionEvent) {
        playerState.wood -= 20.0;
        playerState.iron -= 30.0;
        playerState.gold -= 10.0;
        barracks.startTraining("Legion", 4);
        this.repaint();
    }

    private /* synthetic */ void lambda$showFacilityMenu$11(PlayerState playerState, Barracks barracks, ActionEvent actionEvent) {
        playerState.wood -= 20.0;
        playerState.iron -= 10.0;
        barracks.startTraining("Scout", 2);
        this.repaint();
    }

    private /* synthetic */ void lambda$showFacilityMenu$10(BurningStation burningStation, ActionEvent actionEvent) {
        burningStation.activeFuel = BurningStation.FuelType.OFF;
        this.repaint();
    }

    private /* synthetic */ void lambda$showFacilityMenu$9(BurningStation burningStation, ActionEvent actionEvent) {
        burningStation.activeFuel = BurningStation.FuelType.WOOD;
        this.repaint();
    }

    private /* synthetic */ void lambda$showFacilityMenu$8(BurningStation burningStation, ActionEvent actionEvent) {
        burningStation.activeFuel = BurningStation.FuelType.COAL;
        this.repaint();
    }

    static {
        ROW_STRIDE = HEX_H = Math.sqrt(3.0) * 22.0;
        OFFSET_Y = (int)(HEX_H / 2.0) + 10;
        NB_EVEN = new int[][]{{1, 0}, {1, -1}, {0, -1}, {-1, -1}, {-1, 0}, {0, 1}};
        NB_ODD = new int[][]{{1, 1}, {1, 0}, {0, -1}, {-1, 0}, {-1, 1}, {0, 1}};
    }

    public static class PlayerState {
        String name;
        Color color;
        public double coal;
        public double gold;
        public double iron;
        public double wood;
        public double uranium;
        public double energy;
        public boolean isAI;

        PlayerState(String string, Color color, boolean isAI) {
            this.name = string;
            this.color = color;
            this.isAI = isAI;
            double d = isAI ? 3.0 : 1.0;
            this.coal = 100.0 * d;
            this.gold = 100.0 * d;
            this.iron = 100.0 * d;
            this.wood = 100.0 * d;
            this.uranium = 0.0 * d;
            this.energy = 0.0 * d;
        }
    }
}
