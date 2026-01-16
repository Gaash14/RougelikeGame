package com.example.rougelikegame.android.managers;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.example.rougelikegame.android.models.meta.Achievement;
import com.example.rougelikegame.android.models.meta.User;
import com.example.rougelikegame.android.services.DatabaseService;
import com.example.rougelikegame.android.utils.SharedPreferencesUtil;

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
        add(new Achievement("wave_5","Getting Started",
            "Reach wave 5","skin_wave_5"));
        add(new Achievement("wave_10","Survivor",
            "Reach wave 10","skin_wave_10"));
        add(new Achievement("kills_100","Slayer",
            "Kill 100 enemies","skin_100_wins"));
        add(new Achievement("coins_200","Collector",
            "Collect 200 coins","skin_200_coins"));
        add(new Achievement("first_win","Champion",
            "Defeat the boss","skin_first_win"));
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

        // grant skin reward
        if (achievement.getRewardSkinId() != null && context != null) {
            User user = SharedPreferencesUtil.getUser(context);
            if (user != null) {
                if (user.getOwnedSkins() == null) {
                    user.setOwnedSkins(new HashMap<>());}
                user.getOwnedSkins()
                    .put(achievement.getRewardSkinId(), true);


                SharedPreferencesUtil.saveUser(context, user);

                DatabaseService.getInstance()
                    .unlockOwnedSkin(userUid, achievement.getRewardSkinId());
            }
        }

        // save achievement to Firebase
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
    public void markUnlockedFromDatabase(String id) {
        Achievement achievement = achievements.get(id);
        if (achievement == null) return;

        // mark unlocked WITHOUT triggering unlock logic
        achievement.setUnlocked(true);
    }

    public void reset() {
        userUid = null;
        context = null;

        for (Achievement a : achievements.values()) {
            a.setUnlocked(false);
        }
    }
}
