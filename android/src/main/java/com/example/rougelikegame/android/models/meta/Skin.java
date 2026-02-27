package com.example.rougelikegame.android.models.meta;

public class Skin {

    public enum UnlockType {
        DEFAULT,
        SHOP,
        ACHIEVEMENT
    }

    private final String id;
    private final String name;
    private final int price; // shop only
    private final UnlockType unlockType;
    private final String texturePath;
    private String achievementKey; // nullable

    // DEFAULT / SHOP constructor
    public Skin(String id,
                String name,
                int price,
                UnlockType unlockType,
                String texturePath) {

        this.id = id;
        this.name = name;
        this.price = price;
        this.unlockType = unlockType;
        this.texturePath = texturePath;
    }

    // ACHIEVEMENT constructor
    public Skin(String id,
                String name,
                UnlockType unlockType,
                String achievementKey,
                String texturePath) {

        this.id = id;
        this.name = name;
        this.unlockType = unlockType;
        this.achievementKey = achievementKey;
        this.texturePath = texturePath;
        this.price = 0;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getPrice() { return price; }
    public UnlockType getUnlockType() { return unlockType; }
    public String getTexturePath() { return texturePath; }
    public String getAchievementKey() { return achievementKey; }
}
