package com.hexgame;
import java.awt.Color;
import java.util.Random;

public class Tile {

    public enum Terrain {
        OCEAN, PLAINS, FOREST, MOUNTAIN, DESERT, TUNDRA, BASE
    }

    public Terrain terrain;
    public boolean selected = false;
    public boolean hovered  = false;
    public boolean isPlayer = false;
    
    // --- Fog of War States ---
    public boolean isExplored = false; 
    public boolean isVisible  = false; 

    // --- Economy Foundation (Yields & Efficiency) ---
    public double yieldCoal = 0;
    public double yieldGold = 0;
    public double yieldIron = 0;
    public double yieldWood = 0;
    public double yieldUranium = 0;
    public double yieldEnergy = 0;
    
    public double buildingEfficiency = 1.0; 

    // --- Radiation State ---
    public int deadZoneTimer = 0;

    public Tile(Terrain terrain) {
        this.terrain = terrain;
    }

    public void generateYields(Random rng) {
        switch (terrain) {
            case PLAINS:
                buildingEfficiency = 1.0; 
                break;
            case MOUNTAIN:
                yieldIron = 0.7 + rng.nextDouble() * 0.3; 
                yieldCoal = 1.0 + rng.nextDouble() * 1.5; 
                buildingEfficiency = 0.50; 
                break;
            case FOREST:
                yieldWood = 3.0 + rng.nextDouble() * 2.5; 
                buildingEfficiency = 0.75; 
                break;
            case TUNDRA:
                yieldGold = 9.0 + rng.nextDouble() * 3.0; 
                yieldUranium = 0.15 + rng.nextDouble() * 0.35; 
                buildingEfficiency = 0.60; 
                break;
            case DESERT:
                yieldEnergy = 1.0; 
                buildingEfficiency = 0.70; 
                break;
            case OCEAN:
                buildingEfficiency = 0.0;
                break;
            case BASE:
                buildingEfficiency = 1.0;
                break;
        }
    }

    public Color getColor() {
        if (deadZoneTimer > 0) return new Color(20, 60, 30); // Solid dark radioactive green
        switch (terrain) {
            case OCEAN:    return new Color(58,  148, 210);
            case PLAINS:   return new Color(138, 195, 88);
            case FOREST:   return new Color(34,  110, 54);
            case MOUNTAIN: return new Color(140, 130, 120);
            case DESERT:   return new Color(224, 195, 110);
            case TUNDRA:   return new Color(190, 215, 225);
            case BASE: return isPlayer ? new Color(245, 195, 35) : new Color(200, 40, 40);
            default:       return Color.GRAY;
        }
    }

    public Color getHoverColor() {
        return getColor().brighter(); 
    }
    
    public Color getRenderColor() {
        if (!isExplored) return new Color(25, 25, 30); 
        Color base = (hovered && isVisible) ? getHoverColor() : getColor();
        if (!isVisible) {
            return new Color((int)(base.getRed()*0.4), (int)(base.getGreen()*0.4), (int)(base.getBlue()*0.4));
        }
        return base;
    }

    public Color getRenderBorderColor() {
        if (!isExplored) return new Color(15, 15, 20); 
        if (selected && isVisible) return new Color(255, 215, 0);
        if (!isVisible) return new Color(0, 0, 0, 40); 
        return new Color(0, 0, 0, 80);
    }
}
