package com.example.rougelikegame.android.models.meta;

public class DailyRun {

    public String uid;
    public String name;
    public int wave;
    public int time; // seconds
    public String playerClass;

    // REQUIRED empty constructor for Firebase
    public DailyRun() {}

    public DailyRun(String uid, String name, int wave, int time, String playerClass) {
        this.uid = uid;
        this.name = name;
        this.wave = wave;
        this.time = time;
        this.playerClass = playerClass;
    }
}
