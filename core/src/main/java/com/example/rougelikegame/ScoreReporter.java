package com.example.rougelikegame;

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
