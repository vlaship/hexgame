package com.hexgame;

class DummyTarget extends Unit {
    public DummyTarget(int col, int row, int ownerIndex) {
        super(col, row, ownerIndex);
        this.typeName = "Dummy Target";
        this.maxMoves = 0;        
        this.movesLeft = 0;
        this.visionRadius = 2; 
        
        this.maxHp = 1000;        
        this.currentHp = 1000;
        this.combatStrength = 0; 
        this.canAttack = false;
        this.isCivilian = false;
        this.attackRange = 0;   
    }
}
