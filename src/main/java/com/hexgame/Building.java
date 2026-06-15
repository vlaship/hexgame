package com.hexgame;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

// ─────────────────────────────────────────────────────────────────────────
// ── BASE BUILDING CLASS ──────────────────────────────────────────────────
// ─────────────────────────────────────────────────────────────────────────
public abstract class Building {
    public String name;
    public char icon;
    public Color color;
    public int ownerIndex;

    public boolean usesPower = false;
    public boolean isOperational = false;
    public double powerCost = 0.0;

    public Building(String name, char icon, Color color, int ownerIndex) {
        this.name = name;
        this.icon = icon;
        this.color = color;
        this.ownerIndex = ownerIndex;
    }

    public double getEffectiveEfficiency(Tile tile) {
        return tile.buildingEfficiency; 
    }

    public abstract void processTurnProduction(HexGrid grid, Tile tile, int col, int row);
    public abstract List<String> getProductionInfo(Tile tile);
}

// ─────────────────────────────────────────────────────────────────────────
// ── BUILDING IMPLEMENTATIONS ─────────────────────────────────────────────
// ─────────────────────────────────────────────────────────────────────────

class LoggerStation extends Building {
    public LoggerStation(int ownerIndex) {
        super("Logger Station", 'L', new Color(153, 102, 51), ownerIndex);
        this.usesPower = true;
        this.powerCost = HexGrid.LOGGER_ENERGY_COST;
    }

    @Override
    public double getEffectiveEfficiency(Tile tile) {
        if (tile.terrain == Tile.Terrain.FOREST) return 1.0; 
        return tile.buildingEfficiency;
    }

    @Override
    public void processTurnProduction(HexGrid grid, Tile tile, int col, int row) {
        HexGrid.PlayerState player = grid.players[ownerIndex];
        double resourceYieldFactor = tile.yieldWood > 0 ? tile.yieldWood : 0.4;
        double efficiency = getEffectiveEfficiency(tile);

        if (isOperational) {
            if (player.energy >= powerCost) {
                player.energy -= powerCost;
            } else {
                isOperational = false; 
                efficiency *= 0.5;     
            }
        } else {
            efficiency *= 0.5;         
        }

        double outputAmount = HexGrid.LOGGER_BASE_PRODUCTION * resourceYieldFactor * efficiency * HexGrid.MINING_EASE_MODIFIER;
        player.wood += outputAmount;
    }

    @Override
    public List<String> getProductionInfo(Tile tile) {
        List<String> info = new ArrayList<>();
        double resourceYieldFactor = tile.yieldWood > 0 ? tile.yieldWood : 0.4;
        double efficiency = getEffectiveEfficiency(tile);
        
        if (isOperational) {
            info.add(String.format("-%.1f Energy", powerCost));
        } else {
            efficiency *= 0.5;
        }

        double outputAmount = HexGrid.LOGGER_BASE_PRODUCTION * resourceYieldFactor * efficiency * HexGrid.MINING_EASE_MODIFIER;
        info.add(String.format("+%.1f Wood", outputAmount));
        return info;
    }
}

class MiningStation extends Building {
    public MiningStation(int ownerIndex) {
        super("Mining Station", 'M', new Color(112, 128, 144), ownerIndex);
        this.usesPower = true;
        this.powerCost = HexGrid.MINING_ENERGY_COST;
    }

    @Override
    public double getEffectiveEfficiency(Tile tile) {
        if (tile.terrain == Tile.Terrain.MOUNTAIN || tile.terrain == Tile.Terrain.TUNDRA) return 1.0;
        return tile.buildingEfficiency;
    }

    @Override
    public void processTurnProduction(HexGrid grid, Tile tile, int col, int row) {
        HexGrid.PlayerState player = grid.players[ownerIndex];
        double modifier = HexGrid.MINING_STATION_MODIFIER;
        double efficiency = getEffectiveEfficiency(tile);

        if (isOperational) {
            if (player.energy >= powerCost) {
                player.energy -= powerCost;
            } else {
                isOperational = false; 
                efficiency *= 0.5;     
            }
        } else {
            efficiency *= 0.5;         
        }

        player.coal    += HexGrid.MINE_OUTPUT_COAL    * tile.yieldCoal    * modifier * efficiency;
        player.iron    += HexGrid.MINE_OUTPUT_IRON    * tile.yieldIron    * modifier * efficiency;
        player.gold    += HexGrid.MINE_OUTPUT_GOLD    * tile.yieldGold    * modifier * efficiency;
        player.uranium += HexGrid.MINE_OUTPUT_URANIUM * tile.yieldUranium * modifier * efficiency;
    }

    @Override
    public List<String> getProductionInfo(Tile tile) {
        List<String> info = new ArrayList<>();
        double modifier = HexGrid.MINING_STATION_MODIFIER;
        double efficiency = getEffectiveEfficiency(tile);

        if (isOperational) {
            info.add(String.format("-%.1f Energy", powerCost));
        } else {
            efficiency *= 0.5;
        }

        if (tile.yieldCoal > 0)    info.add(String.format("+%.1f Coal", HexGrid.MINE_OUTPUT_COAL * tile.yieldCoal * modifier * efficiency));
        if (tile.yieldIron > 0)    info.add(String.format("+%.1f Iron", HexGrid.MINE_OUTPUT_IRON * tile.yieldIron * modifier * efficiency));
        if (tile.yieldGold > 0)    info.add(String.format("+%.1f Gold", HexGrid.MINE_OUTPUT_GOLD * tile.yieldGold * modifier * efficiency));
        if (tile.yieldUranium > 0) info.add(String.format("+%.1f Urnm", HexGrid.MINE_OUTPUT_URANIUM * tile.yieldUranium * modifier * efficiency));
        
        return info;
    }
}

class BurningStation extends Building {
    public enum FuelType { COAL, WOOD, OFF }
    public FuelType activeFuel = FuelType.COAL; 
    
    public BurningStation(int ownerIndex) {
        super("Burning Station", 'B', new Color(220, 80, 20), ownerIndex);
    }

    @Override
    public void processTurnProduction(HexGrid grid, Tile tile, int col, int row) {
        HexGrid.PlayerState player = grid.players[ownerIndex];
        double eff = getEffectiveEfficiency(tile);
        
        if (activeFuel == FuelType.COAL && player.coal >= HexGrid.BURNER_COAL_COST) {
            player.coal -= HexGrid.BURNER_COAL_COST;
            player.energy += HexGrid.BURNER_ENERGY_FROM_COAL * eff;
        } 
        else if (activeFuel == FuelType.WOOD && player.wood >= HexGrid.BURNER_WOOD_COST) {
            player.wood -= HexGrid.BURNER_WOOD_COST;
            player.energy += HexGrid.BURNER_ENERGY_FROM_WOOD * eff;
        }
    }

    @Override
    public List<String> getProductionInfo(Tile tile) {
        List<String> info = new ArrayList<>();
        double eff = getEffectiveEfficiency(tile);

        if (activeFuel == FuelType.COAL) {
            info.add(String.format("-%.1f Coal", HexGrid.BURNER_COAL_COST));
            info.add(String.format("+%.1f Energy", HexGrid.BURNER_ENERGY_FROM_COAL * eff));
        } else if (activeFuel == FuelType.WOOD) {
            info.add(String.format("-%.1f Wood", HexGrid.BURNER_WOOD_COST));
            info.add(String.format("+%.1f Energy", HexGrid.BURNER_ENERGY_FROM_WOOD * eff));
        } else {
            info.add("Status: OFFLINE");
        }
        return info;
    }
}

class SolarPlant extends Building {
    public SolarPlant(int ownerIndex) {
        super("Solar Panel Array", 'S', new Color(255, 215, 0), ownerIndex);
    }

    @Override
    public double getEffectiveEfficiency(Tile tile) {
        if (tile.terrain == Tile.Terrain.DESERT) return 1.0; 
        return tile.buildingEfficiency;
    }

    @Override
    public void processTurnProduction(HexGrid grid, Tile tile, int col, int row) {
        HexGrid.PlayerState player = grid.players[ownerIndex];
        double eff = getEffectiveEfficiency(tile);
        player.energy += HexGrid.SOLAR_OUTPUT_ENERGY * eff;
    }

    @Override
    public List<String> getProductionInfo(Tile tile) {
        List<String> info = new ArrayList<>();
        double eff = getEffectiveEfficiency(tile);
        info.add(String.format("+%.1f Energy", HexGrid.SOLAR_OUTPUT_ENERGY * eff));
        return info;
    }
}

class NuclearPlant extends Building {
    public NuclearPlant(int ownerIndex) {
        super("Nuclear Plant", 'N', new Color(50, 255, 50), ownerIndex);
    }

    @Override
    public void processTurnProduction(HexGrid grid, Tile tile, int col, int row) {
        HexGrid.PlayerState player = grid.players[ownerIndex];
        double eff = getEffectiveEfficiency(tile);
        if (player.uranium >= HexGrid.NUCLEAR_URANIUM_COST) {
            player.uranium -= HexGrid.NUCLEAR_URANIUM_COST;
            player.energy += HexGrid.NUCLEAR_ENERGY_OUTPUT * eff;
        }
    }

    @Override
    public List<String> getProductionInfo(Tile tile) {
        List<String> info = new ArrayList<>();
        double eff = getEffectiveEfficiency(tile);
        info.add(String.format("-%.1f Urnm", HexGrid.NUCLEAR_URANIUM_COST));
        info.add(String.format("+%.1f Energy", HexGrid.NUCLEAR_ENERGY_OUTPUT * eff));
        return info;
    }
}

class Barracks extends Building {
    public String activeProject = null;
    public int accumulatedPoints = 0;
    public int targetPoints = 0;

    public Barracks(int ownerIndex) {
        super("Barracks", 'X', new Color(139, 0, 0), ownerIndex);
        this.usesPower = true;
        this.powerCost = HexGrid.BARRACKS_ENERGY_COST; 
    }

    public void startTraining(String unit, int cost) {
        this.activeProject = unit;
        this.targetPoints = cost;
        this.accumulatedPoints = 0;
    }

    @Override
    public void processTurnProduction(HexGrid grid, Tile tile, int col, int row) {
        HexGrid.PlayerState player = grid.players[this.ownerIndex];
        
        if (activeProject != null) {
            int pointsGenerated = 1; 
            if (isOperational) {
                if (player.energy >= powerCost) {
                    player.energy -= powerCost;
                    pointsGenerated = 2; 
                } else {
                    isOperational = false; 
                }
            }
            accumulatedPoints += pointsGenerated;
            if (accumulatedPoints >= targetPoints) {
                grid.spawnUnitFromBuilding(activeProject, col, row, ownerIndex);
                activeProject = null;
                accumulatedPoints = 0;
                targetPoints = 0;
            }
        }
    }

    @Override
    public List<String> getProductionInfo(Tile tile) {
        List<String> info = new ArrayList<>();
        if (isOperational) {
            info.add(String.format("-%.1f Energy", powerCost));
            info.add("Rate: 2 Pts/Turn");
        } else {
            info.add("Rate: 1 Pt/Turn");
        }
        info.add("-----------");
        if (activeProject != null) {
            info.add(String.format("Task: %s", activeProject));
            info.add(String.format("Prog: %d/%d Pts", accumulatedPoints, targetPoints));
        } else {
            info.add("Task: IDLE");
        }
        return info;
    }
}

// NEW: Heavy Machinery & Vehicle Fabrication
class VehicleFactory extends Building {
    public String activeProject = null;
    public int accumulatedPoints = 0;
    public int targetPoints = 0;

    public VehicleFactory(int ownerIndex) {
        // Steel/Industrial Grey color
        super("Vehicle Factory", 'V', new Color(105, 105, 105), ownerIndex);
        this.usesPower = true;
        this.powerCost = HexGrid.FACTORY_ENERGY_COST; 
    }

    public void startManufacturing(String unit, int cost) {
        this.activeProject = unit;
        this.targetPoints = cost;
        this.accumulatedPoints = 0;
    }

    @Override
    public void processTurnProduction(HexGrid grid, Tile tile, int col, int row) {
        HexGrid.PlayerState player = grid.players[this.ownerIndex];
        
        if (activeProject != null) {
            int pointsGenerated = 1; 
            if (isOperational) {
                if (player.energy >= powerCost) {
                    player.energy -= powerCost;
                    pointsGenerated = 2; 
                } else {
                    isOperational = false; 
                }
            }
            accumulatedPoints += pointsGenerated;
            if (accumulatedPoints >= targetPoints) {
                grid.spawnUnitFromBuilding(activeProject, col, row, ownerIndex);
                activeProject = null;
                accumulatedPoints = 0;
                targetPoints = 0;
            }
        }
    }

    @Override
    public List<String> getProductionInfo(Tile tile) {
        List<String> info = new ArrayList<>();
        if (isOperational) {
            info.add(String.format("-%.1f Energy", powerCost));
            info.add("Rate: 2 Pts/Turn");
        } else {
            info.add("Rate: 1 Pt/Turn");
        }
        info.add("-----------");
        if (activeProject != null) {
            info.add(String.format("Task: %s", activeProject));
            info.add(String.format("Prog: %d/%d Pts", accumulatedPoints, targetPoints));
        } else {
            info.add("Task: IDLE");
        }
        return info;
    }
}
