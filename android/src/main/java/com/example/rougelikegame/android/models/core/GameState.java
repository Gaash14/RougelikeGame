package com.example.rougelikegame.android.models.core;

import java.util.Random;

public class GameState {
    private long seed;
    private Random random;

    public GameState(long seed) {
        this.seed = seed;
        this.random = new Random(seed);
    }

    public Random getRandom() {
        return random;
    }

    public long getSeed() {
        return seed;
    }
}

