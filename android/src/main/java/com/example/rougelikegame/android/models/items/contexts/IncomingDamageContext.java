package com.example.rougelikegame.android.models.items.contexts;

/**
 * Data class for holding incoming damage information.
 */
public class IncomingDamageContext {
    public int damage;
    public int maxDamage;

    /**
     * Default constructor for Firebase/JSON serialization.
     */
    public IncomingDamageContext() {
        this.maxDamage = Integer.MAX_VALUE;
    }

    /**
     * Constructs a new IncomingDamageContext with the specified damage.
     * @param damage The damage value.
     */
    public IncomingDamageContext(int damage) {
        this.damage = damage;
        this.maxDamage = Integer.MAX_VALUE;
    }
}
