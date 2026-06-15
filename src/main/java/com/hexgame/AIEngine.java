package com.hexgame;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.LinkedList;

public class AIEngine {
    private HexGrid game;

    public AIEngine(HexGrid game) {
        this.game = game;
    }

    private double calculatePlayerScore(int playerIndex) {
        double score = 0;
        if (playerIndex >= game.players.length) return 0;
        HexGrid.PlayerState state = game.players[playerIndex];
        if (state == null) return 0;
        score += (state.wood + state.iron + state.gold + state.coal + state.uranium + state.energy) / 10.0;
        
        for (Unit u : game.units) {
            if (u.ownerIndex == playerIndex) {
                score += 10.0 + (u.currentHp / 10.0);
                if (!u.isCivilian) score += 5.0;
            }
        }
        
        for (int c = 0; c < game.grid.length; c++) {
            for (int r = 0; r < game.grid[0].length; r++) {
                if (game.buildings[c][r] != null && game.buildings[c][r].ownerIndex == playerIndex) {
                    score += 15.0;
                    if (game.buildings[c][r].name.equals("HQ")) score += 50.0;
                }
            }
        }
        return score;
    }

    private boolean isSameIslandAsPlayer(int aiPlayerIndex) {
        int enemyIdx = (aiPlayerIndex == 0) ? 1 : 0;
        Unit aiHQ = getMyHQ(aiPlayerIndex);
        Unit humanHQ = getMyHQ(enemyIdx);
        if (aiHQ == null || humanHQ == null) return true; 

        boolean[][] visited = new boolean[game.grid.length][game.grid[0].length];
        Queue<int[]> queue = new LinkedList<>();
        queue.add(new int[]{aiHQ.col, aiHQ.row});
        visited[aiHQ.col][aiHQ.row] = true;

        while(!queue.isEmpty()) {
            int[] curr = queue.poll();
            if (curr[0] == humanHQ.col && curr[1] == humanHQ.row) return true;
            for(int[] nb : game.getNeighbors(curr[0], curr[1])) {
                int nc = nb[0], nr = nb[1];
                if (game.inBounds(nc, nr) && !visited[nc][nr] && game.grid[nc][nr].terrain != Tile.Terrain.OCEAN) {
                    visited[nc][nr] = true;
                    queue.add(new int[]{nc, nr});
                }
            }
        }
        return false;
    }

    public String getRelativePower(int aiPlayerIndex) {
        int enemyIdx = (aiPlayerIndex == 0) ? 1 : 0;
        double aiScore = calculatePlayerScore(aiPlayerIndex);
        double humanScore = calculatePlayerScore(enemyIdx);
        
        if (humanScore <= 0) humanScore = 1; 
        double ratio = aiScore / humanScore;
        
        if (ratio < 0.5) return "Pathetic";
        if (ratio < 0.8) return "Inferior";
        if (ratio <= 1.25) return "Equivalent";
        if (ratio <= 2.0) return "Superior";
        return "Overwhelming";
    }

    public int calculateAIAggressivity(int aiPlayerIndex) {
        int enemyIdx = (aiPlayerIndex == 0) ? 1 : 0;
        double aiScore = calculatePlayerScore(aiPlayerIndex);
        double humanScore = calculatePlayerScore(enemyIdx);
        
        double advantage = aiScore - humanScore + (Math.random() * 8 - 4);
        
        if (!isSameIslandAsPlayer(aiPlayerIndex)) {
            return 0; 
        }

        if (advantage > 10) return 100;
        else if (advantage > 0) return 50;
        else return 0;
    }

    private Unit getClosestPlayerUnit(int col, int row, int playerIndex) {
        int enemyIdx = (playerIndex == 0) ? 1 : 0;
        Unit closest = null;
        int minD = Integer.MAX_VALUE;
        for (Unit u : game.units) {
            if (u.ownerIndex == enemyIdx) { 
                int d = game.getDistance(col, row, u.col, u.row);
                if (u instanceof HQ) d -= 50; 
                if (d < minD) {
                    minD = d;
                    closest = u;
                }
            }
        }
        return closest;
    }

    private Unit getMyHQ(int ownerIndex) {
        for(Unit u : game.units) {
            if (u instanceof HQ && u.ownerIndex == ownerIndex) return u;
        }
        return null;
    }

    private SkyShip getNearestTransport(Unit u) {
        SkyShip best = null;
        int minD = Integer.MAX_VALUE;
        for (Unit s : game.units) {
            if (s.ownerIndex == u.ownerIndex && s instanceof SkyShip) {
                SkyShip ship = (SkyShip) s;
                if (ship.cargo.size() < ship.capacity) {
                    int d = game.getDistance(u.col, u.row, s.col, s.row);
                    if (d < minD) {
                        minD = d;
                        best = ship;
                    }
                }
            }
        }
        return best;
    }

    private void aiWander(Unit u) {
        if (u.movesLeft > 0) {
            List<int[]> nbs = game.getNeighbors(u.col, u.row);
            Collections.shuffle(nbs);
            for (int[] nb : nbs) {
                int nc = nb[0], nr = nb[1];
                boolean canMove = game.inBounds(nc, nr) && (game.grid[nc][nr].terrain != Tile.Terrain.OCEAN || u instanceof SkyShip) && game.getUnitAt(nc, nr) == null;
                if (canMove) {
                    u.col = nc;
                    u.row = nr;
                    u.movesLeft--;
                    break;
                }
            }
        }
    }

    private void aiEvadeEnemy(Unit u) {
        while(u.movesLeft > 0) {
            Unit enemy = getClosestPlayerUnit(u.col, u.row, u.ownerIndex);
            if (enemy != null && game.getDistance(u.col, u.row, enemy.col, enemy.row) <= 4) {
                int bestCol = u.col, bestRow = u.row;
                int maxDist = game.getDistance(u.col, u.row, enemy.col, enemy.row);
                for (int[] nb : game.getNeighbors(u.col, u.row)) {
                    int nc = nb[0], nr = nb[1];
                    boolean canMove = game.inBounds(nc, nr) && (game.grid[nc][nr].terrain != Tile.Terrain.OCEAN || u instanceof SkyShip) && game.getUnitAt(nc, nr) == null;
                    if (canMove) {
                        int d = game.getDistance(nc, nr, enemy.col, enemy.row);
                        if (d > maxDist) {
                            maxDist = d;
                            bestCol = nc;
                            bestRow = nr;
                        }
                    }
                }
                if (bestCol != u.col || bestRow != u.row) {
                    u.col = bestCol;
                    u.row = bestRow;
                    u.movesLeft--;
                } else {
                    break;
                }
            } else {
                int start = u.movesLeft;
                aiWander(u);
                if (start == u.movesLeft) break; 
            }
        }
    }

    private void aiMoveTowards(Unit u, Unit target, int idealDistance) {
        if (target == null) return;
        int startMoves = u.movesLeft;
        
        for(int step = 0; step < startMoves; step++) {
            if (game.getDistance(u.col, u.row, target.col, target.row) <= idealDistance) break;

            int bestCol = u.col;
            int bestRow = u.row;
            int minDist = game.getDistance(u.col, u.row, target.col, target.row);
            
            for (int[] nb : game.getNeighbors(u.col, u.row)) {
                int nc = nb[0], nr = nb[1];
                boolean canMove = game.inBounds(nc, nr) && (game.grid[nc][nr].terrain != Tile.Terrain.OCEAN || u instanceof SkyShip) && game.getUnitAt(nc, nr) == null;
                if (canMove) {
                    int d = game.getDistance(nc, nr, target.col, target.row);
                    if (d < minDist) {
                        minDist = d;
                        bestCol = nc;
                        bestRow = nr;
                    }
                }
            }
            if (bestCol != u.col || bestRow != u.row) {
                u.col = bestCol;
                u.row = bestRow;
                u.movesLeft--;
            } else {
                break; 
            }
        }
    }

    private void aiHandleBuilder(Unit u, int playerIndex, int aggro) {
        if (u.movesLeft < 2) {
            aiEvadeEnemy(u);
            return;
        }
        HexGrid.PlayerState p = game.players[playerIndex];
        int c = u.col, r = u.row;

        int loggerCount = 0, minerCount = 0, barracksCount = 0, factoryCount = 0;
        for (int col = 0; col < game.grid.length; col++) {
            for (int row = 0; row < game.grid[0].length; row++) {
                Building b = game.buildings[col][row];
                if (b != null && b.ownerIndex == playerIndex) {
                    if (b instanceof LoggerStation) loggerCount++;
                    if (b instanceof MiningStation) minerCount++;
                    if (b instanceof Barracks) barracksCount++;
                    if (b instanceof VehicleFactory) factoryCount++;
                }
            }
        }

        String targetBuilding = null;
        Tile.Terrain targetTerrain = null;
        
        boolean hasForest = findNearestTerrain(u.col, u.row, Tile.Terrain.FOREST, "LoggerStation") != null;
        boolean hasMountain = findNearestTerrain(u.col, u.row, Tile.Terrain.MOUNTAIN, "MiningStation") != null;
        
        if (loggerCount < 1 && hasForest && p.wood >= 20 && p.iron >= 5) {
            targetBuilding = "LoggerStation"; targetTerrain = Tile.Terrain.FOREST;
        } else if (minerCount < 1 && hasMountain && p.wood >= 50 && p.iron >= 20) {
            targetBuilding = "MiningStation"; targetTerrain = Tile.Terrain.MOUNTAIN;
        } else if (barracksCount < 1 && p.wood >= 80 && p.iron >= 40 && p.gold >= 30) {
            targetBuilding = "Barracks"; targetTerrain = null;
        } else if (p.energy < 20 && p.wood >= 40 && p.iron >= 20 && p.gold >= 20) {
            targetBuilding = "BurningStation"; targetTerrain = null;
        } else if (loggerCount < 3 && hasForest && p.wood >= 20 && p.iron >= 5) {
            targetBuilding = "LoggerStation"; targetTerrain = Tile.Terrain.FOREST;
        } else if (minerCount < 3 && hasMountain && p.wood >= 50 && p.iron >= 20) {
            targetBuilding = "MiningStation"; targetTerrain = Tile.Terrain.MOUNTAIN;
        } else if (factoryCount < 1 && p.wood >= 120 && p.iron >= 60 && p.gold >= 50) {
            targetBuilding = "VehicleFactory"; targetTerrain = null;
        } else if (p.wood >= 150 && p.iron >= 100 && p.gold >= 100 && p.uranium >= 25) {
            targetBuilding = "NuclearPlant"; targetTerrain = null;
        } else if (p.wood >= 60 && p.iron >= 30 && p.gold >= 40 && p.energy < 100) {
            targetBuilding = "SolarPlant"; targetTerrain = null;
        } else if (p.wood >= 80 && p.iron >= 40 && p.gold >= 30) {
            targetBuilding = "Barracks"; targetTerrain = null;
        }

        if (targetBuilding != null) {
            Tile.Terrain currentT = game.grid[c][r].terrain;
            boolean canBuildHere = game.buildings[c][r] == null && currentT != Tile.Terrain.OCEAN && currentT != Tile.Terrain.BASE;
            
            boolean terrainMatches = false;
            if (targetBuilding.equals("MiningStation")) {
                terrainMatches = currentT == Tile.Terrain.MOUNTAIN || currentT == Tile.Terrain.TUNDRA;
            } else if (targetTerrain != null) {
                terrainMatches = currentT == targetTerrain;
            } else {
                terrainMatches = currentT == Tile.Terrain.PLAINS || currentT == Tile.Terrain.DESERT;
            }
            
            if (canBuildHere && terrainMatches) {
                if (targetBuilding.equals("LoggerStation")) { p.wood -= 20; p.iron -= 5; game.buildings[c][r] = new LoggerStation(playerIndex); }
                else if (targetBuilding.equals("MiningStation")) { p.wood -= 50; p.iron -= 20; game.buildings[c][r] = new MiningStation(playerIndex); }
                else if (targetBuilding.equals("BurningStation")) { p.wood -= 40; p.iron -= 20; p.gold -= 20; game.buildings[c][r] = new BurningStation(playerIndex); }
                else if (targetBuilding.equals("Barracks")) { p.wood -= 80; p.iron -= 40; p.gold -= 30; game.buildings[c][r] = new Barracks(playerIndex); }
                else if (targetBuilding.equals("VehicleFactory")) { p.wood -= 120; p.iron -= 60; p.gold -= 50; game.buildings[c][r] = new VehicleFactory(playerIndex); }
                else if (targetBuilding.equals("NuclearPlant")) { p.wood -= 150; p.iron -= 100; p.gold -= 100; p.uranium -= 25; game.buildings[c][r] = new NuclearPlant(playerIndex); }
                else if (targetBuilding.equals("SolarPlant")) { p.wood -= 60; p.iron -= 30; p.gold -= 40; game.buildings[c][r] = new SolarPlant(playerIndex); }
                u.movesLeft -= 2;
                return;
            } else {
                int[] bestTile = findNearestTerrain(u.col, u.row, targetTerrain, targetBuilding);
                if (bestTile != null) {
                    aiMoveTowardsHex(u, bestTile[0], bestTile[1]);
                    return;
                }
            }
        }
        aiEvadeEnemy(u); 
    }

    private int[] findNearestTerrain(int startCol, int startRow, Tile.Terrain targetTerrain, String targetBuilding) {
        java.util.Queue<int[]> queue = new java.util.LinkedList<>();
        java.util.Set<String> visited = new java.util.HashSet<>();
        queue.add(new int[]{startCol, startRow});
        visited.add(startCol + "," + startRow);
        
        while (!queue.isEmpty()) {
            int[] curr = queue.poll();
            int c = curr[0];
            int r = curr[1];
            
            if (game.buildings[c][r] == null && game.grid[c][r].terrain != Tile.Terrain.OCEAN && game.grid[c][r].terrain != Tile.Terrain.BASE) {
                boolean matches = false;
                Tile.Terrain ct = game.grid[c][r].terrain;
                if (targetBuilding != null && targetBuilding.equals("MiningStation")) {
                    matches = ct == Tile.Terrain.MOUNTAIN || ct == Tile.Terrain.TUNDRA;
                } else if (targetTerrain != null) {
                    matches = ct == targetTerrain;
                } else {
                    matches = ct == Tile.Terrain.PLAINS || ct == Tile.Terrain.DESERT;
                }
                if (matches) {
                    return new int[]{c, r};
                }
            }
            
            for (int[] nb : game.getNeighbors(c, r)) {
                int nc = nb[0];
                int nr = nb[1];
                if (game.inBounds(nc, nr)) {
                    if (game.grid[nc][nr].terrain == Tile.Terrain.OCEAN) continue;
                    String key = nc + "," + nr;
                    if (!visited.contains(key)) {
                        visited.add(key);
                        queue.add(new int[]{nc, nr});
                    }
                }
            }
        }
        return null;
    }

    private void aiMoveTowardsHex(Unit u, int tCol, int tRow) {
        int startMoves = u.movesLeft;
        for(int step = 0; step < startMoves; step++) {
            if (u.col == tCol && u.row == tRow) break;
            int bestCol = u.col, bestRow = u.row;
            int minDist = game.getDistance(u.col, u.row, tCol, tRow);
            
            for (int[] nb : game.getNeighbors(u.col, u.row)) {
                int nc = nb[0], nr = nb[1];
                boolean canMove = game.inBounds(nc, nr) && (game.grid[nc][nr].terrain != Tile.Terrain.OCEAN || u instanceof SkyShip) && game.getUnitAt(nc, nr) == null;
                if (canMove) {
                    int d = game.getDistance(nc, nr, tCol, tRow);
                    if (d < minDist) {
                        minDist = d;
                        bestCol = nc;
                        bestRow = nr;
                    }
                }
            }
            if (bestCol != u.col || bestRow != u.row) {
                u.col = bestCol;
                u.row = bestRow;
                u.movesLeft--;
            } else {
                break; 
            }
        }
    }

    private double getMilitaryScore(int playerIndex) {
        double s = 0;
        for (Unit u : game.units) {
            if (u.ownerIndex == playerIndex && !u.isCivilian && !u.typeName.equals("HQ")) {
                s += 10.0 + (u.currentHp / 10.0);
            }
        }
        return s;
    }

    private java.util.Set<String> getIslandTiles(int startCol, int startRow) {
        java.util.Set<String> visited = new java.util.HashSet<>();
        if (startCol < 0 || startRow < 0) return visited;
        java.util.Queue<int[]> queue = new java.util.LinkedList<>();
        queue.add(new int[]{startCol, startRow});
        visited.add(startCol + "," + startRow);
        
        while (!queue.isEmpty()) {
            int[] curr = queue.poll();
            for (int[] nb : game.getNeighbors(curr[0], curr[1])) {
                int nc = nb[0], nr = nb[1];
                if (game.inBounds(nc, nr) && game.grid[nc][nr].terrain != Tile.Terrain.OCEAN) {
                    String key = nc + "," + nr;
                    if (!visited.contains(key)) {
                        visited.add(key);
                        queue.add(new int[]{nc, nr});
                    }
                }
            }
        }
        return visited;
    }

    private int[] findNearestEmptyLand(int startCol, int startRow) {
        java.util.Queue<int[]> queue = new java.util.LinkedList<>();
        java.util.Set<String> visited = new java.util.HashSet<>();
        queue.add(new int[]{startCol, startRow});
        visited.add(startCol + "," + startRow);
        
        while (!queue.isEmpty()) {
            int[] curr = queue.poll();
            int c = curr[0], r = curr[1];
            
            if (game.grid[c][r].terrain != Tile.Terrain.OCEAN && game.getUnitAt(c, r) == null) {
                return new int[]{c, r};
            }
            
            if (game.getDistance(startCol, startRow, c, r) <= 3) {
                for (int[] nb : game.getNeighbors(c, r)) {
                    int nc = nb[0], nr = nb[1];
                    if (game.inBounds(nc, nr)) {
                        String key = nc + "," + nr;
                        if (!visited.contains(key)) {
                            visited.add(key);
                            queue.add(new int[]{nc, nr});
                        }
                    }
                }
            }
        }
        return null;
    }

    private void managePowerGrid(int playerIndex) {
        HexGrid.PlayerState p = game.players[playerIndex];
        
        // If energy is critically low, turn off production buildings that aren't actively building
        if (p.energy < 10) {
            for (int c = 0; c < game.grid.length; c++) {
                for (int r = 0; r < game.grid[0].length; r++) {
                    Building b = game.buildings[c][r];
                    if (b != null && b.ownerIndex == playerIndex && b.usesPower) {
                        if (b instanceof Barracks) {
                            Barracks bar = (Barracks) b;
                            if (bar.activeProject == null) bar.isOperational = false;
                        } else if (b instanceof VehicleFactory) {
                            VehicleFactory vf = (VehicleFactory) b;
                            if (vf.activeProject == null) vf.isOperational = false;
                        }
                    }
                }
            }
        } else if (p.energy > 30) {
            // Turn them back on if we have plenty of energy
            for (int c = 0; c < game.grid.length; c++) {
                for (int r = 0; r < game.grid[0].length; r++) {
                    Building b = game.buildings[c][r];
                    if (b != null && b.ownerIndex == playerIndex && b.usesPower && !b.isOperational) {
                        b.isOperational = true;
                    }
                }
            }
        }
    }

    private void aiHandleProduction(int playerIndex, int aggro, boolean isStaging, boolean differentIsland) {
        int enemyIdx = (playerIndex == 0) ? 1 : 0;
        HexGrid.PlayerState p = game.players[playerIndex];
        double myMil = getMilitaryScore(playerIndex);
        double enemyMil = getMilitaryScore(enemyIdx);
        boolean panic = enemyMil > myMil + 15.0 && !differentIsland;
        
        int factoryCount = 0;
        for (int c = 0; c < game.grid.length; c++) {
            for (int r = 0; r < game.grid[0].length; r++) {
                Building b = game.buildings[c][r];
                if (b != null && b.ownerIndex == playerIndex && b instanceof VehicleFactory) factoryCount++;
            }
        }
        int skyShipCount = 0;
        for (Unit u : game.units) {
            if (u.ownerIndex == playerIndex && u.typeName.equals("Sky Ship")) skyShipCount++;
        }
        boolean savingForTech = differentIsland && (factoryCount == 0 || skyShipCount == 0);

        for (int c = 0; c < game.grid.length; c++) {
            for (int r = 0; r < game.grid[0].length; r++) {
                Building b = game.buildings[c][r];
                if (b != null && b.ownerIndex == playerIndex) {
                    if (b instanceof VehicleFactory && b.isOperational) {
                        VehicleFactory vf = (VehicleFactory) b;
                        if (vf.activeProject != null) continue;
                        
                        // Cross-Ocean Invasion tech
                        if (differentIsland && p.iron >= 60 && p.gold >= 30) {
                            p.iron -= 60; p.gold -= 30;
                            vf.startManufacturing("Sky Ship", 6);
                        } else if (p.iron >= 80 && p.gold >= 40) {
                            p.iron -= 80; p.gold -= 40;
                            vf.startManufacturing("Tank", 8);
                        }
                    } else if (b instanceof Barracks && b.isOperational) {
                        Barracks barracks = (Barracks) b;
                        if (barracks.activeProject != null) continue;
                        
                        // Nuke Carrier
                        if (p.wood >= 50 && p.iron >= 50 && p.uranium >= 50) {
                            p.wood -= 50; p.iron -= 50; p.uranium -= 50;
                            barracks.startTraining("Nuke Carrier", 8);
                            continue;
                        }

                        if (panic) {
                            if (p.wood >= 20 && p.iron >= 30 && p.gold >= 10) {
                                p.wood -= 20; p.iron -= 30; p.gold -= 10;
                                barracks.startTraining("Legion", 4);
                            } else if (p.wood >= 20 && p.iron >= 10) {
                                p.wood -= 20; p.iron -= 10;
                                barracks.startTraining("Scout", 2);
                            }
                        } else {
                            int builderCount = 0, legionCount = 0, mortarCount = 0;
                            for (Unit u : game.units) {
                                if (u.ownerIndex == playerIndex) {
                                    if (u.typeName.equals("Builder")) builderCount++;
                                    else if (u.typeName.equals("Legion")) legionCount++;
                                    else if (u.typeName.startsWith("Mortar")) mortarCount++;
                                }
                            }
                            
                            boolean needsMeatShield = legionCount <= mortarCount * 1.5;
                            
                            if (builderCount < 2 && p.wood >= 30 && p.iron >= 10) {
                                p.wood -= 30; p.iron -= 10;
                                barracks.startTraining("Builder", 3);
                            } else if (!savingForTech) {
                                if (needsMeatShield && p.wood >= 20 && p.iron >= 30 && p.gold >= 10) {
                                    p.wood -= 20; p.iron -= 30; p.gold -= 10;
                                    barracks.startTraining("Legion", 4);
                                } else if (!needsMeatShield && p.wood >= 30 && p.iron >= 20 && p.gold >= 15) {
                                    p.wood -= 30; p.iron -= 20; p.gold -= 15;
                                    barracks.startTraining("Mortar", 3);
                                } else if (p.wood >= 20 && p.iron >= 30 && p.gold >= 10) {
                                    p.wood -= 20; p.iron -= 30; p.gold -= 10;
                                    barracks.startTraining("Legion", 4);
                                } else if (p.wood >= 30 && p.iron >= 20 && p.gold >= 15) {
                                    p.wood -= 30; p.iron -= 20; p.gold -= 15;
                                    barracks.startTraining("Mortar", 3);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void processAITurn(int currentPlayerIndex) {
        managePowerGrid(currentPlayerIndex);

        int aggro = calculateAIAggressivity(currentPlayerIndex);
        boolean differentIsland = !isSameIslandAsPlayer(currentPlayerIndex);
        int enemyIdx = (currentPlayerIndex == 0) ? 1 : 0;
        double myMil = getMilitaryScore(currentPlayerIndex);
        double enemyMil = getMilitaryScore(enemyIdx);
        double bravery = 10.0 + (Math.random() * 20.0);
        
        boolean enemyNearHQ = false;
        Unit myHQBase = getMyHQ(currentPlayerIndex);
        if (myHQBase != null) {
            for (Unit u : game.units) {
                if (u.ownerIndex == enemyIdx && game.getDistance(myHQBase.col, myHQBase.row, u.col, u.row) <= 7) {
                    enemyNearHQ = true;
                    break;
                }
            }
        }
        
        boolean isDefending = (myMil < enemyMil - 10.0) || enemyNearHQ;
        boolean isStaging = isDefending || (myMil < 60.0 && myMil < enemyMil + bravery);

        Unit enemyHQBase = getMyHQ(enemyIdx);
        java.util.Set<String> enemyIslandTiles = new java.util.HashSet<>();
        if (enemyHQBase != null) {
            enemyIslandTiles = getIslandTiles(enemyHQBase.col, enemyHQBase.row);
        }

        List<Unit> safeUnitList = new ArrayList<>(game.units);
        for (Unit u : safeUnitList) {
            if (!game.units.contains(u)) continue;

            if (u.ownerIndex == currentPlayerIndex) {
                if (u instanceof HQ) continue;

                boolean attacked = false;
                List<Unit> safeTargetList = new ArrayList<>(game.units);
                
                // 1. Combat Phase
                for (Unit enemy : safeTargetList) {
                    if (!game.units.contains(enemy)) continue; 
                    if (enemy.ownerIndex == enemyIdx) { 
                        int dist = game.getDistance(u.col, u.row, enemy.col, enemy.row);
                        if (dist <= u.attackRange && u.canAttack) {
                            game.resolveCombat(u, enemy, dist);
                            attacked = true;
                            break; 
                        }
                    }
                }
                
                // 2. Movement Phase
                if (!attacked && game.units.contains(u)) {
                    // Tactical Retreat
                    if (u.currentHp <= u.maxHp * 0.3) {
                        aiEvadeEnemy(u);
                        continue;
                    }

                    if (u.typeName.equals("Builder")) {
                        aiHandleBuilder(u, currentPlayerIndex, aggro);
                    } else if (u.isCivilian) {
                        aiEvadeEnemy(u); 
                    } else if (u.typeName.equals("Nuke Carrier")) {
                        Unit target = getClosestPlayerUnit(u.col, u.row, u.ownerIndex);
                        if (target != null && game.getDistance(u.col, u.row, target.col, target.row) <= 2) {
                            game.detonateNuke(u.col, u.row);
                            game.units.remove(u); // Destroy self
                            continue;
                        } else if (target != null) {
                            aiMoveTowards(u, target, 0); // Rush enemy
                        } else {
                            aiWander(u);
                        }
                    } else if (u instanceof SkyShip) {
                        SkyShip ship = (SkyShip) u;
                        Unit humanHQ = getMyHQ(enemyIdx);
                        if (ship.cargo.size() >= 3 && humanHQ != null) {
                            // Invade
                            if (game.getDistance(ship.col, ship.row, humanHQ.col, humanHQ.row) <= 4) {
                                // Unload
                                for (Unit cargoUnit : ship.cargo) {
                                    int[] dropTile = findNearestEmptyLand(ship.col, ship.row);
                                    if (dropTile != null) {
                                        cargoUnit.col = dropTile[0];
                                        cargoUnit.row = dropTile[1];
                                    } else {
                                        cargoUnit.col = ship.col;
                                        cargoUnit.row = ship.row;
                                    }
                                    game.units.add(cargoUnit);
                                }
                                ship.cargo.clear();
                            } else {
                                aiMoveTowards(ship, humanHQ, 0);
                            }
                        } else {
                            // Go back to pick up units
                            aiMoveTowards(ship, getMyHQ(currentPlayerIndex), 0);
                        }
                    } else {
                        // Regular Military
                        boolean onEnemyIsland = enemyIslandTiles.contains(u.col + "," + u.row);
                        if (differentIsland && !onEnemyIsland) {
                            SkyShip transport = getNearestTransport(u);
                            if (transport != null && transport.cargo.size() < transport.capacity) {
                                if (game.getDistance(u.col, u.row, transport.col, transport.row) <= 1) {
                                    transport.cargo.add(u);
                                    game.units.remove(u);
                                    continue;
                                } else {
                                    aiMoveTowards(u, transport, 0);
                                    continue;
                                }
                            }
                        }

                        if (isDefending) {
                            Unit myHQ = getMyHQ(u.ownerIndex);
                            if (myHQ != null) {
                                if (game.getDistance(u.col, u.row, myHQ.col, myHQ.row) > 3) {
                                    aiMoveTowards(u, myHQ, 3);
                                } else {
                                    aiWander(u);
                                }
                            }
                        } else if (isStaging) {
                            Unit myHQ = getMyHQ(u.ownerIndex);
                            if (myHQ != null && game.getDistance(u.col, u.row, myHQ.col, myHQ.row) > 2) {
                                aiMoveTowards(u, myHQ, 2); // Stage around HQ
                            } else {
                                aiWander(u);
                            }
                        } else {
                            Unit nearestLegion = getNearestFriendly(u, "Legion");
                            Unit nearestMortar = getNearestFriendly(u, "Mortar");
                            boolean formingUp = false;
                            
                            if (u.typeName.startsWith("Mortar") && nearestLegion != null) {
                                int dist = game.getDistance(u.col, u.row, nearestLegion.col, nearestLegion.row);
                                if (dist > 1) {
                                    aiMoveTowards(u, nearestLegion, 1);
                                    formingUp = true;
                                }
                            } else if (u.typeName.equals("Legion") && nearestMortar != null) {
                                int dist = game.getDistance(u.col, u.row, nearestMortar.col, nearestMortar.row);
                                if (dist > 2) {
                                    aiMoveTowards(u, nearestMortar, 1);
                                    formingUp = true;
                                }
                            }
                            
                            if (!formingUp) {
                                Unit target = getClosestPlayerUnit(u.col, u.row, u.ownerIndex);
                                int idealRange = (u.typeName.startsWith("Mortar")) ? u.attackRange : 1;
                                aiMoveTowards(u, target, idealRange);
                            }
                        }
                    }
                }
            }
        }
        aiHandleProduction(currentPlayerIndex, aggro, isStaging, differentIsland);
    }

    private Unit getNearestFriendly(Unit u, String typePrefix) {
        Unit nearest = null;
        int minDist = Integer.MAX_VALUE;
        for (Unit f : game.units) {
            if (f.ownerIndex == u.ownerIndex && f != u && f.typeName.startsWith(typePrefix)) {
                int d = game.getDistance(u.col, u.row, f.col, f.row);
                if (d < minDist) {
                    minDist = d;
                    nearest = f;
                }
            }
        }
        return nearest;
    }
}
