package com.example.rougelikegame.android.models.core;

import java.util.Random;

public class GameState {
    private final long seed;
    private final Random itemRandom;
    private final Random pickupRandom;
    private final Random enemyRandom;
    private final Random miscRandom;

    public GameState(long seed) {
        this.seed = seed;
        this.itemRandom = new Random(seed + 1);
        this.pickupRandom = new Random(seed + 2);
        this.enemyRandom = new Random(seed + 3);
        this.miscRandom = new Random(seed + 4);
    }

    public Random getItemRandom() {
        return itemRandom;
    }

    public Random getPickupRandom() {
        return pickupRandom;
    }

    public Random getEnemyRandom() {
        return enemyRandom;
    }

    public Random getMiscRandom() {
        return miscRandom;
    }

    public long getSeed() {
        return seed;
    }
}
