package com.example.rougelikegame.android.models.core;

import java.util.Random;

/**
 * The GameState class manages the random number generators for various game elements
 * based on a provided seed, ensuring deterministic behavior for a given seed.
 */
public class GameState {
    private final long seed;
    private final Random itemRandom;
    private final Random pickupRandom;
    private final Random enemyRandom;
    private final Random miscRandom;

    /**
     * Constructs a GameState with the specified seed.
     *
     * @param seed the seed used to initialize random number generators
     */
    public GameState(long seed) {
        this.seed = seed;
        this.itemRandom = new Random(seed + 1);
        this.pickupRandom = new Random(seed + 2);
        this.enemyRandom = new Random(seed + 3);
        this.miscRandom = new Random(seed + 4);
    }

    /**
     * @return the random number generator for items
     */
    public Random getItemRandom() {
        return itemRandom;
    }

    /**
     * @return the random number generator for pickups
     */
    public Random getPickupRandom() {
        return pickupRandom;
    }

    /**
     * @return the random number generator for enemies
     */
    public Random getEnemyRandom() {
        return enemyRandom;
    }

    /**
     * @return the random number generator for miscellaneous elements
     */
    public Random getMiscRandom() {
        return miscRandom;
    }

    /**
     * @return the base seed for this game state
     */
    public long getSeed() {
        return seed;
    }
}
