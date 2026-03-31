package com.example.rougelikegame.android.models.meta;

/**
 * Tracks statistics for a single game run.
 */
public class RunStats {

    private int enemiesKilled;
    private int pickupsPicked;
    private int itemsPicked;
    private float runTime;

    /**
     * Increments the count of enemies killed.
     */
    public void addKill() {
        enemiesKilled++;
    }

    /**
     * Increments the count of pickups collected.
     */
    public void addPickup() {
        pickupsPicked++;
    }

    /**
     * Increments the count of items picked up.
     */
    public void addItemPicked() {
        itemsPicked++;
    }

    /**
     * Adds the specified time to the total run time.
     * @param delta The time to add in seconds.
     */
    public void addTime(float delta) {
        runTime += delta;
    }

    public int getEnemiesKilled() {
        return enemiesKilled;
    }

    public int getPickupsPicked() {
        return pickupsPicked;
    }

    public int getItemsPicked() {
        return itemsPicked;
    }

    public float getRunTime() {
        return runTime;
    }
}
