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

    // --- NEW: Economy Foundation (Yields & Efficiency) ---
    public double yieldCoal = 0;
    public double yieldGold = 0;
    public double yieldIron = 0;
    public double yieldWood = 0;
    public double yieldUranium = 0;
    public double yieldEnergy = 0;

    // 1.0 = 100% normal efficiency for general buildings. Lower means penalized.
    public double buildingEfficiency = 1.0;

    public Tile(Terrain terrain) {
        this.terrain = terrain;
    }

    // Call this right after creating the tile to set its resource potentials
    public void generateYields(Random rng) {
        switch (terrain) {
            case PLAINS:
                buildingEfficiency = 1.0; // Normal building ground
                break;
            case MOUNTAIN:
                yieldIron = 0.7 + rng.nextDouble() * 0.3; // 0.7 to 1.0
                yieldCoal = 1.0 + rng.nextDouble() * 1.5; // 1.0 to 2.5
                buildingEfficiency = 0.50; // -50% efficiency for general buildings
                break;
            case FOREST:
                yieldWood = 3.0 + rng.nextDouble() * 2.5; // 3.0 to 5.5
                buildingEfficiency = 0.75; // -25% efficiency
                break;
            case TUNDRA:
                yieldGold = 9.0 + rng.nextDouble() * 3.0; // 9.0 to 12.0
                yieldUranium = 0.15 + rng.nextDouble() * 0.35; // 0.15 to 0.50
                buildingEfficiency = 0.60; // -40% efficiency
                break;
            case DESERT:
                yieldEnergy = 1.0; // 100% solar efficiency placeholder
                buildingEfficiency = 0.70; // -30% efficiency
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
