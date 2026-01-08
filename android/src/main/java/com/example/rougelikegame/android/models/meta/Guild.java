package com.example.rougelikegame.android.models.meta;

import java.util.Map;

public class Guild {
    private String guildId;
    private String name;
    private String ownerUid;           // creator/admin
    private Map<String, Boolean> members; // uid -> true

    // aggregated stats
    private int totalEnemiesKilled;
    private int totalWins;
    private int totalAttempts;

    public Guild() {} // Firebase requires this

    public String getGuildId() {
        return guildId;
    }

    public void setGuildId(String guildId) {
        this.guildId = guildId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwnerUid() {
        return ownerUid;
    }

    public void setOwnerUid(String ownerUid) {
        this.ownerUid = ownerUid;
    }

    public Map<String, Boolean> getMembers() {
        return members;
    }

    public void setMembers(Map<String, Boolean> members) {
        this.members = members;
    }

    public int getTotalEnemiesKilled() {
        return totalEnemiesKilled;
    }

    public void setTotalEnemiesKilled(int totalEnemiesKilled) {
        this.totalEnemiesKilled = totalEnemiesKilled;
    }

    public int getTotalWins() {
        return totalWins;
    }

    public void setTotalWins(int totalWins) {
        this.totalWins = totalWins;
    }

    public int getTotalAttempts() {
        return totalAttempts;
    }

    public void setTotalAttempts(int totalAttempts) {
        this.totalAttempts = totalAttempts;
    }
}

