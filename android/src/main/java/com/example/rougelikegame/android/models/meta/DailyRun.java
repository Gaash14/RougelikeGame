package com.example.rougelikegame.android.models.meta;

/**
 * DailyRun stores the performance of a player in a daily challenge run.
 */
public class DailyRun {

    public String uid;
    public String name;
    public int wave;
    public int time; // seconds
    public String playerClass;

    /**
     * Default constructor for Firebase/JSON serialization.
     */
    public DailyRun() {
    }

    /**
     * Constructs a new DailyRun record.
     * @param uid The unique identifier of the user.
     * @param name The name of the user.
     * @param wave The highest wave reached.
     * @param time The time taken in seconds.
     * @param playerClass The class used by the player.
     */
    public DailyRun(String uid, String name, int wave, int time, String playerClass) {
        this.uid = uid;
        this.name = name;
        this.wave = wave;
        this.time = time;
        this.playerClass = playerClass;
    }
}
