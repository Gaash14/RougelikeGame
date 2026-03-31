package com.example.rougelikegame.android.models.items.contexts;

/**
 * Data class for holding blocking chance information.
 */
public class BlockChanceContext {
    public float chance;

    /**
     * Default constructor for Firebase/JSON serialization.
     */
    public BlockChanceContext() {
    }

    /**
     * Constructs a new BlockChanceContext with the specified chance.
     * @param chance The block chance.
     */
    public BlockChanceContext(float chance) {
        this.chance = chance;
    }
}
