package com.example.rougelikegame.android.models.characters;

public class StatusEffects {

    // --- Poison/burn constants ---
    public static final float POISON_TICK_INTERVAL = 0.5f;
    public static final int POISON_DAMAGE_PER_TICK = 1;
    public static final float BURN_TICK_INTERVAL = 1f;
    public static final int BURN_DAMAGE_PER_TICK = 3;

    // --- Poison state ---
    private boolean poisoned = false;
    private float poisonTimeLeft = 0f;
    private float poisonTickTimer = 0f;

    // --- Burn state ---
    private boolean burning = false;
    private float burnTimeLeft = 0f;
    private float burnTickTimer = 0f;

    public void applyPoison(float durationSeconds) {
        poisoned = true;
        poisonTimeLeft = durationSeconds; // reset (no stacking)
        poisonTickTimer = 0f;
    }

    public boolean isPoisoned() {
        return poisoned && poisonTimeLeft > 0f;
    }

    public void applyBurn(float durationSeconds) {
        burning = true;
        burnTimeLeft = durationSeconds; // refresh duration (no stacking)
        burnTickTimer = 0f;
    }

    public boolean isBurning() {
        return burning && burnTimeLeft > 0f;
    }

    public void update(float delta, Enemy enemy) {
        if (isPoisoned()) {
            poisonTimeLeft -= delta;
            poisonTickTimer += delta;

            while (poisonTickTimer >= POISON_TICK_INTERVAL) {
                poisonTickTimer -= POISON_TICK_INTERVAL;

                enemy.takeDamage(POISON_DAMAGE_PER_TICK);
                if (!enemy.alive) return;
            }

            if (poisonTimeLeft <= 0f) {
                poisoned = false;
                poisonTimeLeft = 0f;
                poisonTickTimer = 0f;
            }
        }

        if (isBurning()) {
            burnTimeLeft -= delta;
            burnTickTimer += delta;

            while (burnTickTimer >= BURN_TICK_INTERVAL) {
                burnTickTimer -= BURN_TICK_INTERVAL;

                enemy.takeDamage(BURN_DAMAGE_PER_TICK);
                if (!enemy.alive) return;
            }

            if (burnTimeLeft <= 0f) {
                burning = false;
                burnTimeLeft = 0f;
                burnTickTimer = 0f;
            }
        }
    }
}
