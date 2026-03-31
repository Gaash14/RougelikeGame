package com.example.rougelikegame.android.models.characters;

/**
 * Manages active status effects on an enemy, such as poison and burning.
 * Handles the logic for periodic damage ticking and effect duration.
 */
public class StatusEffects {

    // Poison and burn constants
    public static final float POISON_TICK_INTERVAL = 0.5f;
    public static final int POISON_DAMAGE_PER_TICK = 1;
    public static final float BURN_TICK_INTERVAL = 1f;
    public static final int BURN_DAMAGE_PER_TICK = 3;

    // Poison state
    private boolean poisoned = false;
    private float poisonTimeLeft = 0f;
    private float poisonTickTimer = 0f;

    // Burn state
    private boolean burning = false;
    private float burnTimeLeft = 0f;
    private float burnTickTimer = 0f;

    /**
     * Applies the poison effect to the enemy.
     *
     * @param durationSeconds how long the poison should last
     */
    public void applyPoison(float durationSeconds) {
        poisoned = true;
        poisonTimeLeft = durationSeconds;
        poisonTickTimer = 0f;
    }

    public boolean isPoisoned() {
        return poisoned && poisonTimeLeft > 0f;
    }

    /**
     * Applies the burn effect to the enemy.
     *
     * @param durationSeconds how long the burn should last
     */
    public void applyBurn(float durationSeconds) {
        burning = true;
        burnTimeLeft = durationSeconds;
        burnTickTimer = 0f;
    }

    public boolean isBurning() {
        return burning && burnTimeLeft > 0f;
    }

    /**
     * Updates the active status effects and applies damage if a tick is reached.
     *
     * @param delta time since last update
     * @param enemy the enemy to apply damage to
     */
    public void update(float delta, Enemy enemy) {
        if (isPoisoned()) {
            poisonTimeLeft -= delta;
            poisonTickTimer += delta;

            while (poisonTickTimer >= POISON_TICK_INTERVAL) {
                poisonTickTimer -= POISON_TICK_INTERVAL;
                enemy.takeDamage(POISON_DAMAGE_PER_TICK);
                if (!enemy.alive) {
                    return;
                }
            }

            if (poisonTimeLeft <= 0f) {
                resetPoison();
            }
        }

        if (isBurning()) {
            burnTimeLeft -= delta;
            burnTickTimer += delta;

            while (burnTickTimer >= BURN_TICK_INTERVAL) {
                burnTickTimer -= BURN_TICK_INTERVAL;
                enemy.takeDamage(BURN_DAMAGE_PER_TICK);
                if (!enemy.alive) {
                    return;
                }
            }

            if (burnTimeLeft <= 0f) {
                resetBurn();
            }
        }
    }

    private void resetPoison() {
        poisoned = false;
        poisonTimeLeft = 0f;
        poisonTickTimer = 0f;
    }

    private void resetBurn() {
        burning = false;
        burnTimeLeft = 0f;
        burnTickTimer = 0f;
    }
}
