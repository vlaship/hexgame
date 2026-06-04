package com.hexgame;

public class Scout {

    // ── position ──────────────────────────────────────────────────────────────
    public int col, row;

    // ── ownership ─────────────────────────────────────────────────────────────
    public int ownerIndex;          // index into players[] / playerColors[]

    // ── unit identity ─────────────────────────────────────────────────────────
    public static final String TYPE = "Scout";

    // ── movement & vision ─────────────────────────────────────────────────────
    public int maxMoves = 3;
    public int movesLeft = 3;
    public int visionRadius = 4;
    public int dirIndex = 0;     // current direction for circular AI movement (cycles 0–5)

    // ─────────────────────────────────────────────────────────────────────────

    public Scout(int col, int row, int ownerIndex) {
        this.col        = col;
        this.row        = row;
        this.ownerIndex = ownerIndex;
    }

    public void resetMoves() {
        this.movesLeft = maxMoves;
    }
}
