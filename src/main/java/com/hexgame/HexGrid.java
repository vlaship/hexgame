package com.hexgame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Path2D;
import java.util.*;
import java.util.List;

public class HexGrid extends JPanel {

    // ── grid dimensions ───────────────────────────────────────────────────────
    private static final int COLS = 50;
    private static final int ROWS = 32;
    private static final int SIZE = 22;

    // ── viewport ──────────────────────────────────────────────────────────────
    private static int VIEW_W = 980;
    private static int VIEW_H = 750;

    public static void setViewSize(int w, int h) { VIEW_W = w; VIEW_H = h; }

    // ── Camera key bindings ───────────────────────────────────────────────────
    private static Set<Integer> keysUp    = new HashSet<>(Arrays.asList(KeyEvent.VK_UP,    KeyEvent.VK_W));
    private static Set<Integer> keysDown  = new HashSet<>(Arrays.asList(KeyEvent.VK_DOWN,  KeyEvent.VK_S));
    private static Set<Integer> keysLeft  = new HashSet<>(Arrays.asList(KeyEvent.VK_LEFT,  KeyEvent.VK_A));
    private static Set<Integer> keysRight = new HashSet<>(Arrays.asList(KeyEvent.VK_RIGHT, KeyEvent.VK_D));

    public static void setCameraKeys(Set<Integer> up, Set<Integer> down, Set<Integer> left, Set<Integer> right) {
        keysUp = up; keysDown = down; keysLeft = left; keysRight = right;
    }

    // ── derived hex geometry (flat-top) ──────────────────────────────────────
    private static final double HEX_H      = Math.sqrt(3) * SIZE;
    private static final double COL_STRIDE = SIZE * 1.5;
    private static final double ROW_STRIDE = HEX_H;

    // ── world-space origin offset ─────────────────────────────────────────────
    private static final int OFFSET_X = SIZE + 10;
    private static final int OFFSET_Y = (int)(HEX_H / 2) + 10;

    // ── flat-top hex neighbours (odd-column-down offset coords) ──────────────
    private static final int[][] NB_EVEN = {{1,0},{1,-1},{0,-1},{-1,-1},{-1,0},{0,1}};
    private static final int[][] NB_ODD  = {{1,1},{1, 0},{0,-1},{ -1,0},{-1,1},{0,1}};

    private int cameraX = 0, cameraY = 0;
    private static final int SCROLL_STEP    = 44;
    private static final int EDGE_THRESHOLD = 40;
    private static final int EDGE_SPEED     = 8;
    private Point mousePos      = new Point(0, 0);
    private boolean mouseInside = false;

    private final int worldW = (int)(OFFSET_X + (COLS - 1) * COL_STRIDE + SIZE * 2);
    private final int worldH = (int)(OFFSET_Y + (ROWS - 1) * ROW_STRIDE + HEX_H);

    // ── grid data ─────────────────────────────────────────────────────────────
    private final Tile[][] grid = new Tile[COLS][ROWS];
    private int selectedCol = -1, selectedRow = -1;
    private List<Point> currentPath = null;

    // ── turn system & economy ─────────────────────────────────────────────────
    private int turnNumber = 1;
    private int currentPlayerIndex = 0;
    private boolean isAITurnProcessing = false;

    // NEW: Structured Player Class to hold economy data
    private static class PlayerState {
        String name;
        Color color;
        double coal = 0, gold = 0, iron = 0, wood = 0, uranium = 0, energy = 0;

        PlayerState(String n, Color c) { this.name = n; this.color = c; }
    }

    private final PlayerState[] players = {
        new PlayerState("Player 1 (Human)", new Color(245, 195, 35)),
        new PlayerState("Player 2 (AI)",    new Color(255, 100, 100)),
        new PlayerState("Player 3 (AI)",    new Color(100, 255, 100)),
        new PlayerState("Player 4 (AI)",    new Color(100, 150, 255))
    };

    // ── units ─────────────────────────────────────────────────────────────────
    private List<Scout> units = new ArrayList<>();
    private Scout selectedUnit = null;

    // ─────────────────────────────────────────────────────────────────────────

    public HexGrid() {
        setBackground(new Color(20, 30, 48));
        setFocusable(true);
        generateMap();
        hookMouse();
        hookKeyboard();
        startEdgeScroll();
    }

    private void hookKeyboard() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int kc = e.getKeyCode();
                if (keysLeft.contains(kc))  cameraX = Math.max(0,            cameraX - SCROLL_STEP);
                if (keysRight.contains(kc)) cameraX = Math.min(worldW - VIEW_W, cameraX + SCROLL_STEP);
                if (keysUp.contains(kc))    cameraY = Math.max(0,            cameraY - SCROLL_STEP);
                if (keysDown.contains(kc))  cameraY = Math.min(worldH - VIEW_H, cameraY + SCROLL_STEP);
                if (kc == KeyEvent.VK_ENTER && !isAITurnProcessing) nextTurn();
                repaint();
            }
        });
    }

    private void hookMouse() {
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { mouseInside = true; }
            @Override public void mouseExited(MouseEvent e)  { mouseInside = false; }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                mousePos = e.getPoint();
                if (isAITurnProcessing) return;

                for (int c = 0; c < COLS; c++)
                    for (int r = 0; r < ROWS; r++)
                        grid[c][r].hovered = false;

                int[] cr = pixelToHex(e.getX() + cameraX, e.getY() + cameraY);
                if (cr != null) {
                    grid[cr[0]][cr[1]].hovered = true;
                    if (selectedUnit != null && selectedUnit.ownerIndex == currentPlayerIndex) {
                        currentPath = calculatePath(selectedUnit.col, selectedUnit.row, cr[0], cr[1], selectedUnit.movesLeft);
                    } else {
                        currentPath = null;
                    }
                } else {
                    currentPath = null;
                }
                repaint();
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (isAITurnProcessing) return;

                int[] cr = pixelToHex(e.getX() + cameraX, e.getY() + cameraY);
                if (cr != null) {
                    int clickedCol = cr[0];
                    int clickedRow = cr[1];

                    if (selectedUnit != null && selectedUnit.ownerIndex == currentPlayerIndex &&
                        currentPath != null && !currentPath.isEmpty()) {
                        Point target = currentPath.get(currentPath.size() - 1);
                        if (target.x == clickedCol && target.y == clickedRow) {
                            selectedUnit.col = clickedCol;
                            selectedUnit.row = clickedRow;
                            selectedUnit.movesLeft -= currentPath.size();
                            selectedCol = selectedUnit.col;
                            selectedRow = selectedUnit.row;
                            currentPath = null;
                            updateVision();
                            repaint();
                            return;
                        }
                    }

                    if (selectedCol >= 0) grid[selectedCol][selectedRow].selected = false;
                    selectedCol = clickedCol;
                    selectedRow = clickedRow;
                    grid[selectedCol][selectedRow].selected = true;

                    selectedUnit = null;
                    for (Scout u : units) {
                        if (u.col == clickedCol && u.row == clickedRow && u.ownerIndex == currentPlayerIndex) {
                            selectedUnit = u;
                            break;
                        }
                    }
                    currentPath = null;
                }
                repaint();
            }
        });
    }

    // ── Edge Scrolling ────────────────────────────────────────────────────────
    private void startEdgeScroll() {
        new javax.swing.Timer(16, e -> {
            if (!mouseInside) return;
            boolean moved = false;
            if (mousePos.x < EDGE_THRESHOLD)            { cameraX = Math.max(0,            cameraX - EDGE_SPEED); moved = true; }
            if (mousePos.x > VIEW_W - EDGE_THRESHOLD)   { cameraX = Math.min(worldW - VIEW_W, cameraX + EDGE_SPEED); moved = true; }
            if (mousePos.y < EDGE_THRESHOLD)            { cameraY = Math.max(0,            cameraY - EDGE_SPEED); moved = true; }
            if (mousePos.y > VIEW_H - EDGE_THRESHOLD)   { cameraY = Math.min(worldH - VIEW_H, cameraY + EDGE_SPEED); moved = true; }
            if (moved) repaint();
        }).start();
    }

    // ── Vision System ─────────────────────────────────────────────────────────
    private void updateVision() {
        for (int c = 0; c < COLS; c++) {
            for (int r = 0; r < ROWS; r++) {
                grid[c][r].isVisible = false;
            }
        }
        for (int c = 0; c < COLS; c++) {
            for (int r = 0; r < ROWS; r++) {
                if (grid[c][r].terrain == Tile.Terrain.BASE && grid[c][r].isPlayer) {
                    revealHexes(c, r, 6);
                }
            }
        }
        for (Scout u : units) {
            if (u.ownerIndex == 0) {
                revealHexes(u.col, u.row, u.visionRadius);
            }
        }
    }

    private void revealHexes(int startCol, int startRow, int radius) {
        Queue<Point> frontier = new LinkedList<>();
        Map<Point, Integer> costSoFar = new HashMap<>();

        Point start = new Point(startCol, startRow);
        frontier.add(start);
        costSoFar.put(start, 0);

        grid[startCol][startRow].isVisible = true;
        grid[startCol][startRow].isExplored = true;

        while(!frontier.isEmpty()) {
            Point current = frontier.poll();
            int currentCost = costSoFar.get(current);
            if (currentCost >= radius) continue;
            for (int[] nb : getNeighbors(current.x, current.y)) {
                Point next = new Point(nb[0], nb[1]);
                int newCost = currentCost + 1;
                if (!costSoFar.containsKey(next) || newCost < costSoFar.get(next)) {
                    costSoFar.put(next, newCost);
                    frontier.add(next);
                    grid[nb[0]][nb[1]].isVisible = true;
                    grid[nb[0]][nb[1]].isExplored = true;
                }
            }
        }
    }

    // ── Pathfinding ───────────────────────────────────────────────────────────
    private List<Point> calculatePath(int startCol, int startRow, int targetCol, int targetRow, int movesAvailable) {
        if (startCol == targetCol && startRow == targetRow) return null;
        if (!grid[targetCol][targetRow].isExplored) return null;
        if (grid[targetCol][targetRow].terrain == Tile.Terrain.OCEAN) return null;

        Queue<Point> frontier = new LinkedList<>();
        Map<Point, Point> cameFrom = new HashMap<>();
        Map<Point, Integer> costSoFar = new HashMap<>();

        Point start = new Point(startCol, startRow);
        frontier.add(start);
        cameFrom.put(start, null);
        costSoFar.put(start, 0);

        while (!frontier.isEmpty()) {
            Point current = frontier.poll();
            if (current.x == targetCol && current.y == targetRow) break;
            for (int[] nb : getNeighbors(current.x, current.y)) {
                if (!grid[nb[0]][nb[1]].isExplored) continue;
                if (grid[nb[0]][nb[1]].terrain == Tile.Terrain.OCEAN) continue;
                int newCost = costSoFar.get(current) + 1;
                if (newCost > movesAvailable) continue;
                Point next = new Point(nb[0], nb[1]);
                if (!costSoFar.containsKey(next) || newCost < costSoFar.get(next)) {
                    costSoFar.put(next, newCost);
                    cameFrom.put(next, current);
                    frontier.add(next);
                }
            }
        }

        Point goal = new Point(targetCol, targetRow);
        if (!cameFrom.containsKey(goal)) return null;

        List<Point> path = new ArrayList<>();
        Point curr = goal;
        while (curr != null && !curr.equals(start)) {
            path.add(curr);
            curr = cameFrom.get(curr);
        }
        Collections.reverse(path);
        return path;
    }

    // ── Map Generation ────────────────────────────────────────────────────────
    private void generateMap() {
        Random rng = new Random();

        for (int col = 0; col < COLS; col++)
            for (int row = 0; row < ROWS; row++)
                grid[col][row] = new Tile(Tile.Terrain.OCEAN);

        double[][] centers = {
            { COLS * 0.22, ROWS * 0.27 },
            { COLS * 0.78, ROWS * 0.27 },
            { COLS * 0.22, ROWS * 0.73 },
            { COLS * 0.78, ROWS * 0.73 }
        };
        double islandRadius = 8.0;

        for (int col = 0; col < COLS; col++) {
            for (int row = 0; row < ROWS; row++) {
                double minDist = Double.MAX_VALUE;
                for (double[] c : centers) {
                    double dx = col - c[0], dy = row - c[1];
                    if (Math.sqrt(dx*dx + dy*dy) < minDist) minDist = Math.sqrt(dx*dx + dy*dy);
                }
                double d = minDist + rng.nextDouble() * 2.0 - 1.0;

                Tile t;
                if      (d < islandRadius * 0.20) t = new Tile(Tile.Terrain.MOUNTAIN);
                else if (d < islandRadius * 0.55) t = new Tile(Tile.Terrain.FOREST);
                else if (d < islandRadius * 0.80) t = new Tile(Tile.Terrain.PLAINS);
                else if (d < islandRadius)        t = new Tile(rng.nextBoolean() ? Tile.Terrain.TUNDRA : Tile.Terrain.DESERT);
                else                              t = new Tile(Tile.Terrain.OCEAN);

                // NEW: Initialize yields for the generated tile!
                t.generateYields(rng);
                grid[col][row] = t;
            }
        }

        for (int i = 0; i < centers.length; i++) {
            int bc = (int) Math.round(centers[i][0]), br = (int) Math.round(centers[i][1]);
            if (inBounds(bc, br)) {
                grid[bc][br] = new Tile(Tile.Terrain.BASE);
                grid[bc][br].generateYields(rng); // Init base yields
                if (i == 0) grid[bc][br].isPlayer = true;

                units.add(spawnScout(bc, br, i));
            }
        }
        updateVision();
    }

    private Scout spawnScout(int baseCol, int baseRow, int ownerIndex) {
        for (int[] nb : getNeighbors(baseCol, baseRow)) {
            if (grid[nb[0]][nb[1]].terrain != Tile.Terrain.OCEAN) {
                return new Scout(nb[0], nb[1], ownerIndex);
            }
        }
        return new Scout(baseCol, baseRow, ownerIndex);
    }

    private List<int[]> getNeighbors(int col, int row) {
        int[][] dirs = (col % 2 == 0) ? NB_EVEN : NB_ODD;
        List<int[]> result = new ArrayList<>();
        for (int[] d : dirs) {
            int nc = col + d[0], nr = row + d[1];
            if (inBounds(nc, nr)) result.add(new int[]{nc, nr});
        }
        return result;
    }

    private boolean inBounds(int c, int r) {
        return c >= 0 && c < COLS && r >= 0 && r < ROWS;
    }

    // ── Rendering ─────────────────────────────────────────────────────────────
    private int visColMin() { return Math.max(0,        (int)((cameraX - OFFSET_X - SIZE)          / COL_STRIDE));     }
    private int visColMax() { return Math.min(COLS - 1, (int)((cameraX + VIEW_W - OFFSET_X + SIZE) / COL_STRIDE) + 1); }
    private int visRowMin() { return Math.max(0,        (int)((cameraY - OFFSET_Y - SIZE)          / ROW_STRIDE) - 1); }
    private int visRowMax() { return Math.min(ROWS - 1, (int)((cameraY + VIEW_H - OFFSET_Y + SIZE) / ROW_STRIDE) + 2); }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.translate(-cameraX, -cameraY);

        for (int col = visColMin(); col <= visColMax(); col++)
            for (int row = visRowMin(); row <= visRowMax(); row++)
                drawHex(g2, col, row);

        for (Scout u : units) drawScout(g2, u);
        drawPath(g2);

        g2.translate(cameraX, cameraY);
        drawHUD(g2);
    }

    private void drawPath(Graphics2D g2) {
        if (currentPath == null || currentPath.isEmpty() || selectedUnit == null) return;

        g2.setColor(new Color(255, 255, 255, 200));
        g2.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{8f}, 0));

        Point prev = new Point(selectedUnit.col, selectedUnit.row);
        for (Point p : currentPath) {
            double cx1 = OFFSET_X + prev.x * COL_STRIDE;
            double cy1 = OFFSET_Y + prev.y * ROW_STRIDE + (prev.x % 2 == 1 ? HEX_H / 2 : 0);

            double cx2 = OFFSET_X + p.x * COL_STRIDE;
            double cy2 = OFFSET_Y + p.y * ROW_STRIDE + (p.x % 2 == 1 ? HEX_H / 2 : 0);

            g2.drawLine((int)cx1, (int)cy1, (int)cx2, (int)cy2);
            g2.fillOval((int)cx2 - 5, (int)cy2 - 5, 10, 10);
            prev = p;
        }
    }

    private void drawScout(Graphics2D g2, Scout unit) {
        if (unit.ownerIndex != 0 && !grid[unit.col][unit.row].isVisible) return;

        double cx = OFFSET_X + unit.col * COL_STRIDE;
        double cy = OFFSET_Y + unit.row * ROW_STRIDE + (unit.col % 2 == 1 ? HEX_H / 2 : 0);
        int r = SIZE / 3 + 1;

        if (unit == selectedUnit) {
            g2.setColor(new Color(255, 215, 0, 150));
            g2.fillOval((int)(cx - r - 4), (int)(cy - r - 4), (r + 4) * 2, (r + 4) * 2);
        }

        g2.setColor(players[unit.ownerIndex].color);
        g2.fillOval((int)(cx - r), (int)(cy - r), r * 2, r * 2);

        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawOval((int)(cx - r), (int)(cy - r), r * 2, r * 2);

        g2.setFont(new Font("Monospaced", Font.BOLD, 11));
        FontMetrics fm = g2.getFontMetrics();
        String lbl = "S";
        g2.drawString(lbl, (int)(cx - fm.stringWidth(lbl) / 2.0), (int)(cy + fm.getAscent() / 2.0 - 1));

        if (unit.ownerIndex == currentPlayerIndex && unit == selectedUnit) {
            String movesStr = unit.movesLeft + "/" + unit.maxMoves;
            g2.setFont(new Font("SansSerif", Font.BOLD, 10));
            g2.drawString(movesStr, (int)(cx - fm.stringWidth(movesStr) / 2.0) - 2, (int)(cy - r - 4));
        }
    }

    private void drawHUD(Graphics2D g2) {
        PlayerState curPlayer = players[currentPlayerIndex];

        // 1. Turn Information Box
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRoundRect(10, 10, 220, 50, 10, 10);
        g2.setColor(curPlayer.color);
        g2.setFont(new Font("SansSerif", Font.BOLD, 16));
        g2.drawString("Turn " + turnNumber + " | " + curPlayer.name, 20, 30);
        g2.setColor(Color.LIGHT_GRAY);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g2.drawString("Press ENTER to end turn", 20, 50);

        // 2. NEW: Economy Top Bar
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRoundRect(240, 10, 600, 30, 10, 10);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Monospaced", Font.BOLD, 13));
        String bankStr = String.format("Bank - Coal:%.0f | Gold:%.0f | Iron:%.0f | Wood:%.0f | U-235:%.0f | Energy:%.0f",
                curPlayer.coal, curPlayer.gold, curPlayer.iron, curPlayer.wood, curPlayer.uranium, curPlayer.energy);
        g2.drawString(bankStr, 255, 30);

        // 3. Enemy Phase Banner
        if (isAITurnProcessing) {
            int bannerW = 320;
            int bannerH = 60;
            int bx = VIEW_W / 2 - bannerW / 2;

            g2.setColor(new Color(150, 30, 30, 220));
            g2.fillRoundRect(bx, 60, bannerW, bannerH, 15, 15);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(bx, 60, bannerW, bannerH, 15, 15);
            g2.setFont(new Font("SansSerif", Font.BOLD, 22));
            String aiText = "Enemy Phase...";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(aiText, bx + (bannerW - fm.stringWidth(aiText)) / 2, 97);
        }

        // 4. NEW: Selected Tile Information (Yields & Efficiency)
        if (selectedCol >= 0) {
            Tile t = grid[selectedCol][selectedRow];

            // Draw a bigger dark box for the selection HUD
            g2.setColor(new Color(0, 0, 0, 190));
            g2.fillRoundRect(10, VIEW_H - 100, 280, 85, 10, 10);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Monospaced", Font.BOLD, 13));

            String terrainName = t.isExplored ? t.terrain.name() : "UNKNOWN";
            g2.drawString("Tile: " + terrainName + " [" + selectedCol + ", " + selectedRow + "]", 20, VIEW_H - 75);

            g2.setFont(new Font("Monospaced", Font.PLAIN, 12));
            if (t.isExplored) {
                int yOffset = VIEW_H - 55;
                if (t.yieldCoal > 0)    { g2.drawString(String.format("Coal: %.1f", t.yieldCoal), 20, yOffset); yOffset+=15; }
                if (t.yieldIron > 0)    { g2.drawString(String.format("Iron: %.1f", t.yieldIron), 20, yOffset); yOffset+=15; }
                if (t.yieldWood > 0)    { g2.drawString(String.format("Wood: %.1f", t.yieldWood), 20, yOffset); yOffset+=15; }
                if (t.yieldGold > 0)    { g2.drawString(String.format("Gold: %.1f", t.yieldGold), 20, yOffset); yOffset+=15; }
                if (t.yieldUranium > 0) { g2.drawString(String.format("Urnm: %.2f", t.yieldUranium), 20, yOffset); yOffset+=15; }
                if (t.yieldEnergy > 0)  { g2.drawString("Solar: 100%", 20, yOffset); yOffset+=15; }

                // Draw efficiency in right column
                g2.setColor(t.buildingEfficiency < 1.0 ? new Color(255, 150, 150) : Color.WHITE);
                g2.drawString(String.format("Efficiency: %.0f%%", t.buildingEfficiency * 100), 130, VIEW_H - 55);
            }
        }

        // Scroll Instructions & Bars
        g2.setColor(new Color(255, 255, 255, 120));
        g2.setFont(new Font("Monospaced", Font.PLAIN, 11));
        g2.drawString("Arrow keys to scroll", VIEW_W - 140, VIEW_H - 10);

        int barW = 120, barH = 8, barX = VIEW_W - barW - 14;
        g2.setColor(new Color(255,255,255, 40));
        g2.fillRoundRect(barX, 14, barW, barH, 4, 4);
        g2.setColor(new Color(255,255,255,150));
        g2.fillRoundRect(barX + (int)(((float)cameraX / (worldW - VIEW_W)) * (barW - 30)), 14, 30, barH, 4, 4);

        g2.setColor(new Color(255,255,255, 40));
        g2.fillRoundRect(VIEW_W - 22, 30, barH, barW, 4, 4);
        g2.setColor(new Color(255,255,255,150));
        g2.fillRoundRect(VIEW_W - 22, 30 + (int)(((float)cameraY / (worldH - VIEW_H)) * (barW - 30)), barH, 30, 4, 4);
    }

    private void drawHex(Graphics2D g2, int col, int row) {
        double cx = OFFSET_X + col * COL_STRIDE;
        double cy = OFFSET_Y + row * ROW_STRIDE + (col % 2 == 1 ? HEX_H / 2 : 0);
        Path2D hex = buildHexPath(cx, cy);
        Tile tile = grid[col][row];

        g2.setColor(tile.getRenderColor());
        g2.fill(hex);

        g2.setColor(tile.getRenderBorderColor());
        g2.setStroke(tile.selected ? new BasicStroke(2.5f) : new BasicStroke(1.0f));
        g2.draw(hex);

        if (tile.isExplored) {
            g2.setColor(tile.isVisible ? new Color(0, 0, 0, 150) : new Color(0, 0, 0, 60));
            g2.setFont(new Font("Monospaced", Font.PLAIN, 10));
            String label = tile.terrain.name().substring(0, 2);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(label, (int)(cx - fm.stringWidth(label) / 2.0), (int)(cy + fm.getAscent() / 2.0));
        }
    }

    private Path2D buildHexPath(double cx, double cy) {
        Path2D path = new Path2D.Double();
        for (int i = 0; i < 6; i++) {
            double angle = Math.toRadians(60 * i);
            double px = cx + SIZE * Math.cos(angle);
            double py = cy + SIZE * Math.sin(angle);
            if (i == 0) path.moveTo(px, py); else path.lineTo(px, py);
        }
        path.closePath();
        return path;
    }

    private int[] pixelToHex(int px, int py) {
        for (int col = visColMin(); col <= visColMax(); col++) {
            for (int row = visRowMin(); row <= visRowMax(); row++) {
                double cx = OFFSET_X + col * COL_STRIDE;
                double cy = OFFSET_Y + row * ROW_STRIDE + (col % 2 == 1 ? HEX_H / 2 : 0);
                if (buildHexPath(cx, cy).contains(px, py)) return new int[]{col, row};
            }
        }
        return null;
    }

    // ── AI Movement ───────────────────────────────────────────────────────────
    private void moveScoutCircular(Scout u) {
        for (int attempt = 0; attempt < 6; attempt++) {
            int[][] dirs = (u.col % 2 == 0) ? NB_EVEN : NB_ODD;
            int[] d = dirs[u.dirIndex];
            int tc = u.col + d[0], tr = u.row + d[1];
            u.dirIndex = (u.dirIndex + 1) % 6;
            if (inBounds(tc, tr) && grid[tc][tr].terrain != Tile.Terrain.OCEAN) {
                u.col = tc;
                u.row = tr;
                return;
            }
        }
    }

    // ── Turn Logic with Timer ────────────────────────────────────────────────
    private void nextTurn() {
        if (isAITurnProcessing) return;

        currentPlayerIndex++;

        if (currentPlayerIndex > 0 && currentPlayerIndex < players.length) {
            isAITurnProcessing = true;
            selectedUnit = null;
            currentPath = null;
            repaint();

            javax.swing.Timer aiTimer = new javax.swing.Timer(1000, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {

                    while (currentPlayerIndex > 0 && currentPlayerIndex < players.length) {
                        for (Scout u : units) {
                            if (u.ownerIndex == currentPlayerIndex) {
                                moveScoutCircular(u);
                            }
                        }
                        currentPlayerIndex++;
                    }

                    completeTurnCycle();
                    ((javax.swing.Timer)e.getSource()).stop();
                }
            });
            aiTimer.setRepeats(false);
            aiTimer.start();

        } else {
            completeTurnCycle();
        }
    }

    private void completeTurnCycle() {
        currentPlayerIndex = 0;
        turnNumber++;
        isAITurnProcessing = false;

        for (Scout u : units) {
            if (u.ownerIndex == 0) {
                u.resetMoves();
            }
        }

        updateVision();
        repaint();
    }

    @Override public Dimension getPreferredSize() { return new Dimension(VIEW_W, VIEW_H); }
}
