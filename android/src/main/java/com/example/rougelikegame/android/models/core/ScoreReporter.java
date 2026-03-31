package com.example.rougelikegame.android.models.core;

/**
 * The ScoreReporter interface defines methods for reporting end-of-run game statistics.
 */
public interface ScoreReporter {
    /**
     * Reports the results of a game run.
     *
     * @param wave          the final wave reached
     * @param time          the total time elapsed in seconds
     * @param enemiesKilled the number of enemies defeated
     * @param itemsPicked   the number of passive items collected
     * @param pickupsPicked the number of pickups collected
     * @param coinsPicked   the number of coins collected
     * @param win           whether the run was completed successfully
     * @param rangedChosen  whether the player played as a ranged character
     */
    void reportRun(
        int wave,
        int time,
        int enemiesKilled,
        int itemsPicked,
        int pickupsPicked,
        int coinsPicked,
        boolean win,
        boolean rangedChosen
    );
}
