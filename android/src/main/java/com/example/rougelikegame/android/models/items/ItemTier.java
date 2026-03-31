package com.example.rougelikegame.android.models.items;

/**
 * The ItemTier enum defines the possible rarity tiers for passive items and their associated generation weights.
 */
public enum ItemTier {
    S(3),
    A(7),
    B(15),
    C(25),
    D(30);

    private final int weight;

    /**
     * Constructs an ItemTier with the specified weight.
     *
     * @param weight the weight used for item generation probability
     */
    ItemTier(int weight) {
        this.weight = weight;
    }

    /**
     * @return the weight associated with this tier
     */
    public int getWeight() {
        return weight;
    }
}
