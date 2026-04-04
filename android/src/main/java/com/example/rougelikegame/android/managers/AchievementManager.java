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

/**
 * Manager for handling game achievements.
 */
public class AchievementManager {

    private static AchievementManager instance;

    private String userUid;
    private Context context;
    private final Map<String, Achievement> achievements = new HashMap<>();

    private AchievementManager() {
        registerAchievements();
    }

    /**
     * Gets the instance of AchievementManager.
     *
     * @return the singleton instance
     */
    public static AchievementManager getInstance() {
        if (instance == null) {
            instance = new AchievementManager();
        }
        return instance;
    }

    /**
     * Sets the current user's UID for database operations.
     *
     * @param uid the user's UID
     */
    public void setUserUid(String uid) {
        this.userUid = uid;
    }

    /**
     * Sets the application context for UI operations like Toasts.
     *
     * @param context the context to use
     */
    public void setContext(Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * Registers all available achievements in the game.
     */
    private void registerAchievements() {
        add(new Achievement("wave_5", "Getting Started",
            "Reach wave 5", "skin_wave_5"));
        add(new Achievement("wave_10", "Survivor",
            "Reach wave 10", "skin_wave_10"));
        add(new Achievement("kills_100", "Slayer",
            "Kill 100 enemies in one run", "skin_100_kills"));
        add(new Achievement("coins_25", "Collector",
            "Collect 25 coins in one run", "skin_25_coins"));
        add(new Achievement("first_win", "Champion",
            "Defeat the boss", "skin_first_win"));
        add(new Achievement("pickup_s_tier", "Treasure Hunter",
            "Pick up an S tier item", null));
    }

    /**
     * Adds an achievement to the manager.
     *
     * @param achievement the achievement to add
     */
    private void add(Achievement achievement) {
        achievements.put(achievement.getId(), achievement);
    }

    /**
     * Returns all registered achievements.
     *
     * @return a collection of all achievements
     */
    public Collection<Achievement> getAllAchievements() {
        return achievements.values();
    }

    /**
     * Gets a specific achievement by its ID.
     *
     * @param id the achievement ID
     * @return the achievement, or null if not found
     */
    public Achievement getAchievement(String id) {
        return achievements.get(id);
    }

    /**
     * Checks if a specific achievement is unlocked.
     *
     * @param id the achievement ID
     * @return true if unlocked, false otherwise
     */
    public boolean isUnlocked(String id) {
        Achievement a = achievements.get(id);
        return a != null && a.isUnlocked();
    }

    /**
     * Unlocks an achievement, grants rewards, and persists the state.
     *
     * @param id the ID of the achievement to unlock
     */
    public void unlock(String id) {
        Achievement achievement = achievements.get(id);
        if (achievement == null || achievement.isUnlocked()) return;

        achievement.setUnlocked(true);
        persistUnlockedAchievementLocally(achievement.getId());

        // grant skin reward
        if (achievement.getRewardSkinId() != null && context != null) {
            User user = SharedPreferencesUtil.getUser(context);
            if (user != null) {
                if (user.getOwnedSkins() == null) {
                    user.setOwnedSkins(new HashMap<>());
                }
                user.getOwnedSkins().put(achievement.getRewardSkinId(), true);

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

        SoundManager.play("achievement");

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

    /**
     * Marks an achievement as unlocked from an external source (e.g., database)
     * without triggering reward logic or notifications.
     *
     * @param id the ID of the achievement
     */
    public void markUnlockedFromDatabase(String id) {
        Achievement achievement = achievements.get(id);
        if (achievement == null) return;

        achievement.setUnlocked(true);
        persistUnlockedAchievementLocally(id);
    }

    /**
     * Persists an unlocked achievement to local shared preferences.
     *
     * @param achievementId the ID of the achievement to persist
     */
    private void persistUnlockedAchievementLocally(String achievementId) {
        if (context == null) return;

        User user = SharedPreferencesUtil.getUser(context);
        if (user == null) return;

        if (user.getAchievements() == null) {
            user.setAchievements(new HashMap<>());
        }

        user.getAchievements().put(achievementId, true);
        SharedPreferencesUtil.saveUser(context, user);
    }

    /**
     * Resets the manager state, including user UID and achievement status.
     */
    public void reset() {
        userUid = null;
        context = null;

        for (Achievement a : achievements.values()) {
            a.setUnlocked(false);
        }
    }
}
