package com.example.rougelikegame.android.managers;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.example.rougelikegame.android.models.meta.Achievement;
import com.example.rougelikegame.android.services.DatabaseService;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class AchievementManager {

    private static AchievementManager instance;

    private String userUid;
    public void setUserUid(String uid) { this.userUid = uid; }

    private Context context;
    public void setContext(Context context) { this.context = context.getApplicationContext(); }

    private final Map<String, Achievement> achievements = new HashMap<>();

    private AchievementManager() {
        registerAchievements();
    }

    public static AchievementManager getInstance() {
        if (instance == null) {
            instance = new AchievementManager();
        }
        return instance;
    }

    private void registerAchievements() {
        add(new Achievement("wave_5", "Getting Started", "Reach wave 5"));
        add(new Achievement("wave_10", "Survivor", "Reach wave 10"));
        add(new Achievement("kills_100", "Slayer", "Kill 100 enemies"));
        add(new Achievement("coins_200", "Collector", "Collect 200 coins"));
        add(new Achievement("first_win", "Champion", "Defeat the boss"));
    }

    private void add(Achievement achievement) {
        achievements.put(achievement.getId(), achievement);
    }

    public Collection<Achievement> getAllAchievements() {
        return achievements.values();
    }

    public Achievement getAchievement(String id) {
        return achievements.get(id);
    }

    public boolean isUnlocked(String id) {
        Achievement a = achievements.get(id);
        return a != null && a.isUnlocked();
    }

    public void unlock(String id) {
        Achievement achievement = achievements.get(id);

        if (achievement == null) return;
        if (achievement.isUnlocked()) return;

        achievement.setUnlocked(true);

        if (userUid != null) {
            DatabaseService.getInstance()
                .unlockAchievement(userUid, achievement.getId());
        }

        if (context != null) {
            new Handler(Looper.getMainLooper()).post(() -> {
                Toast.makeText(
                    context,
                    "🏆 Achievement unlocked: " + achievement.getTitle(),
                    Toast.LENGTH_SHORT
                ).show();
            });
        }
    }
}
