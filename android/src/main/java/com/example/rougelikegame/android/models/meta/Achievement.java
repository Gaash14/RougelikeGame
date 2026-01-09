package com.example.rougelikegame.android.models.meta;

public class Achievement {

    private String id;
    private String title;
    private String description;
    private boolean unlocked;

    public Achievement() {} // required for firebase

    public Achievement(String id, String title, String description) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.unlocked = false;
    }

    public String getId() { return id; }
    public void setId(String newId) { this.id = newId; }

    public String getTitle() { return title; }
    public void setTitle(String newTitle) { this.title = newTitle; }

    public String getDescription() { return description; }
    public void setDescription(String newDescription) { this.id = newDescription; }

    public boolean isUnlocked() { return unlocked; }
    public void setUnlocked(boolean unlocked) { this.unlocked = unlocked; }
}
