package com.example.rougelikegame.android.models;

public interface ScoreReporter {
    void reportRun(
        int wave,
        int time,
        int enemiesKilled,
        int pickupsPicked,
        int coinsPicked,
        boolean win,
        boolean rangedChosen
    );

    void reportHighestWave(int wave);
}
