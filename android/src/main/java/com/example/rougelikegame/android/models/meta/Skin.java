package com.example.rougelikegame.android.models.meta;

public class Skin {
    public enum UnlockType {
        DEFAULT,
        SHOP,
        ACHIEVEMENT
    }

    private String id;
    private String name;
    private int price; // shop only
    private UnlockType unlockType;
    private String achievementKey; // nullable

    public Skin(String id, String name, int price, UnlockType unlockType) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.unlockType = unlockType;
    }

    // achievement skins
    public Skin(String id, String name, UnlockType unlockType, String achievementKey) {
        this.id = id;
        this.name = name;
        this.unlockType = unlockType;
        this.achievementKey = achievementKey;
        this.price = 0;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getPrice() { return price; }
    public UnlockType getUnlockType() { return unlockType; }
}

