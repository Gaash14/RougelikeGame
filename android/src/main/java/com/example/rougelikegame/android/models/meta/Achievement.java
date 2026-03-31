package com.example.rougelikegame.android.models.meta;

/**
 * Achievements track player progress and can provide rewards like skins.
 */
public class Achievement {

    private String id;
    private String title;
    private String description;
    private boolean unlocked;
    private String rewardSkinId; // null = no reward

    /**
     * Default constructor for Firebase/JSON serialization.
     */
    public Achievement() {
    }

    /**
     * Constructs a new Achievement with the specified details.
     * @param id The unique identifier for the achievement.
     * @param title The display title.
     * @param description The display description.
     * @param rewardSkinId The ID of the skin rewarded upon unlocking, if any.
     */
    public Achievement(String id, String title, String description, String rewardSkinId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.rewardSkinId = rewardSkinId;
        this.unlocked = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String newId) {
        this.id = newId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String newTitle) {
        this.title = newTitle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String newDescription) {
        this.description = newDescription;
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }

    public String getRewardSkinId() {
        return rewardSkinId;
    }

    public void setRewardSkinId(String rewardSkinId) {
        this.rewardSkinId = rewardSkinId;
    }
}
