package com.example.rougelikegame.android.models.characters;

public class StatusEffects {

    // --- Poison constants ---
    public static final float POISON_TICK_INTERVAL = 0.5f;
    public static final int POISON_DAMAGE_PER_TICK = 1;

    // --- Poison state ---
    private boolean poisoned = false;
    private float poisonTimeLeft = 0f;
    private float poisonTickTimer = 0f;

    public void applyPoison(float durationSeconds) {
        poisoned = true;
        poisonTimeLeft = durationSeconds; // reset (no stacking)
        poisonTickTimer = 0f;
    }

    public boolean isPoisoned() {
        return poisoned && poisonTimeLeft > 0f;
    }

    public void update(float delta, Enemy enemy) {
        if (!isPoisoned()) return;

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
}
