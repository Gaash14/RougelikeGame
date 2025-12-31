package com.example.rougelikegame;

public interface ScoreReporter {
    void reportRun(
        int wave,
        int time,
        int enemiesKilled,
        int pickupsPicked,
        boolean win
    );
}
