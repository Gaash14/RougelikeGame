package com.example.rougelikegame.android.models.items.contexts;

/**
 * Data class for holding damage information.
 */
public class DamageContext {
    public int damage;

    /**
     * Default constructor for Firebase/JSON serialization.
     */
    public DamageContext() {
    }

    /**
     * Constructs a new DamageContext with the specified damage.
     * @param damage The damage value.
     */
    public DamageContext(int damage) {
        this.damage = damage;
    }
}
