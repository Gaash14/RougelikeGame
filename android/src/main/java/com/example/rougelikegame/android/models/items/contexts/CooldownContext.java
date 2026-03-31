package com.example.rougelikegame.android.models.items.contexts;

/**
 * Data class for holding cooldown information.
 */
public class CooldownContext {
    public float cooldown;

    /**
     * Default constructor for Firebase/JSON serialization.
     */
    public CooldownContext() {
    }

    /**
     * Constructs a new CooldownContext with the specified cooldown.
     * @param cooldown The cooldown value.
     */
    public CooldownContext(float cooldown) {
        this.cooldown = cooldown;
    }
}
