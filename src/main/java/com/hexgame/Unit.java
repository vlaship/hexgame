package com.hexgame;
import java.util.ArrayList;
import java.util.List;

public abstract class Unit {

    public int col, row;
    public int ownerIndex;          

    public String typeName;
    public int maxMoves;
    public int movesLeft;
    public int visionRadius;

    public int maxHp;
    public int currentHp;
    public int combatStrength;
    public boolean canAttack;
    public boolean isCivilian;
    public int attackRange = 1;

    public Unit(int col, int row, int ownerIndex) {
        this.col = col;
        this.row = row;
        this.ownerIndex = ownerIndex;
    }
    
    public void resetMoves() {
        this.movesLeft = maxMoves;
    }
}

class Scout extends Unit {
    public Scout(int col, int row, int ownerIndex) {
        super(col, row, ownerIndex);
        this.typeName = "Scout";
        this.maxMoves = 3;
        this.movesLeft = 3;
        this.visionRadius = 4;
        
        this.maxHp = 100;
        this.currentHp = 100;
        this.combatStrength = 60;
        this.canAttack = true;
        this.isCivilian = false;
        this.attackRange = 1; 
    }
}

class Builder extends Unit {
    public Builder(int col, int row, int ownerIndex) {
        super(col, row, ownerIndex);
        this.typeName = "Builder";
        this.maxMoves = 2;
        this.movesLeft = 2;
        this.visionRadius = 2;

        this.maxHp = 100;
        this.currentHp = 100;
        this.combatStrength = 0;
        this.canAttack = false;
        this.isCivilian = true;
        this.attackRange = 0; 
    }
}

class Legion extends Unit {
    public Legion(int col, int row, int ownerIndex) {
        super(col, row, ownerIndex);
        this.typeName = "Legion";
        this.maxMoves = 2;
        this.movesLeft = 2;
        this.visionRadius = 2;
        
        this.maxHp = 150;
        this.currentHp = 150;
        this.combatStrength = 85; 
        this.canAttack = true;
        this.isCivilian = false;
        this.attackRange = 1; 
    }
}

class Mortar extends Unit {
    public Mortar(int col, int row, int ownerIndex) {
        super(col, row, ownerIndex);
        this.typeName = "Mortar Group";
        this.maxMoves = 2;
        this.movesLeft = 2;
        this.visionRadius = 3; 
        
        this.maxHp = 80;        
        this.currentHp = 80;
        this.combatStrength = 75; 
        this.canAttack = true;
        this.isCivilian = false;
        this.attackRange = 2;   
    }
}

class Tank extends Unit {
    public Tank(int col, int row, int ownerIndex) {
        super(col, row, ownerIndex);
        this.typeName = "Tank";
        this.maxMoves = 3;
        this.movesLeft = 3;
        this.visionRadius = 3; 
        
        this.maxHp = 300;        
        this.currentHp = 300;
        this.combatStrength = 120; 
        this.canAttack = true;
        this.isCivilian = false;
        this.attackRange = 1;   
    }
}

class SkyShip extends Unit {
    public List<Unit> cargo = new ArrayList<>();
    public int capacity = 6;

    public SkyShip(int col, int row, int ownerIndex) {
        super(col, row, ownerIndex);
        this.typeName = "Sky Ship";
        this.maxMoves = 6;        
        this.movesLeft = 6;
        this.visionRadius = 4; 
        
        this.maxHp = 200;        
        this.currentHp = 200;
        this.combatStrength = 0; 
        this.canAttack = false;
        this.isCivilian = false;
        this.attackRange = 0;   
    }
}

class NukeCarrier extends Unit {
    public NukeCarrier(int col, int row, int ownerIndex) {
        super(col, row, ownerIndex);
        this.typeName = "Nuke Carrier";
        this.maxMoves = 2;
        this.movesLeft = 2;
        this.visionRadius = 2; 
        
        this.maxHp = 20;          
        this.currentHp = 20;
        this.combatStrength = 0;  
        this.canAttack = false;   
        this.isCivilian = false;  
        this.attackRange = 0;   
    }
}

class HQ extends Unit {
    public HQ(int col, int row, int ownerIndex) {
        super(col, row, ownerIndex);
        this.typeName = "Headquarters";
        this.maxMoves = 0;        
        this.movesLeft = 0;
        this.visionRadius = 5;    
        
        this.maxHp = 500;         
        this.currentHp = 500;
        this.combatStrength = 40; 
        this.canAttack = true;    
        this.isCivilian = false;
        this.attackRange = 1;   
    }
}
