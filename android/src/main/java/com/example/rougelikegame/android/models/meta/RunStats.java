package com.example.rougelikegame.android.models.meta;

public class RunStats {

    private int enemiesKilled;
    private int pickupsPicked;
    private float runTime;

    public void addKill() {
        enemiesKilled++;
    }

    public void addPickup() {
        pickupsPicked++;
    }

    public void addTime(float delta) {
        runTime += delta;
    }

    public int getEnemiesKilled() {
        return enemiesKilled;
    }

    public int getPickupsPicked() {
        return pickupsPicked;
    }

    public float getRunTime() {
        return runTime;
    }
}
