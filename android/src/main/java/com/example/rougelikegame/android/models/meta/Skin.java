package com.example.rougelikegame.android.models.meta;

/**
 * Represents a character skin in the game.
 */
public class Skin {

    /**
     * Defines how a skin can be unlocked.
     */
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

    /**
     * Constructor for DEFAULT or SHOP skins.
     * @param id The unique identifier for the skin.
     * @param name The display name.
     * @param price The shop price (0 if not applicable).
     * @param unlockType The method to unlock the skin.
     * @param texturePath The path to the skin's texture.
     */
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

    /**
     * Constructor for ACHIEVEMENT skins.
     * @param id The unique identifier for the skin.
     * @param name The display name.
     * @param unlockType The method to unlock the skin (ACHIEVEMENT).
     * @param achievementKey The key of the required achievement.
     * @param texturePath The path to the skin's texture.
     */
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

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }

    public UnlockType getUnlockType() {
        return unlockType;
    }

    public String getTexturePath() {
        return texturePath;
    }

    public String getAchievementKey() {
        return achievementKey;
    }
}
