package com.example.rougelikegame;

public interface ScoreReporter {
    void saveHighestWave(int wave);
    void saveBestTime(int bestTimeSeconds);
    void addAttempt();
    void addWin();
}
