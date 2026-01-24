package com.example.rougelikegame.android.screens.launcher;

import android.os.Bundle;
import android.util.Log;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.example.rougelikegame.android.models.core.ScoreReporter;
import com.example.rougelikegame.android.models.meta.DailyRun;
import com.example.rougelikegame.android.screens.menu.MainActivity;
import com.example.rougelikegame.android.services.DatabaseService;
import com.example.rougelikegame.android.utils.SharedPreferencesUtil;
import com.example.rougelikegame.android.models.characters.Player;
import com.example.rougelikegame.android.models.meta.User;

/** Launches the Android application. */
public class AndroidLauncher extends AndroidApplication {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String cls = getIntent().getStringExtra("PLAYER_CLASS");
        Player.PlayerClass selectedClass = Player.PlayerClass.MELEE;

        if ("RANGED".equals(cls)) {
            selectedClass = Player.PlayerClass.RANGED;
        }

        String difficultyName = getIntent().getStringExtra("DIFFICULTY");
        Player.Difficulty difficulty = Player.Difficulty.valueOf(difficultyName);

        boolean dailyChallenge =
            getIntent().getBooleanExtra("DAILY_CHALLENGE", false);

        long runSeed;

        if (dailyChallenge) {
            // Same seed for everyone today
            runSeed = java.time.LocalDate.now().toEpochDay();
        } else if (getIntent().hasExtra("CUSTOM_SEED")) {
            // Player-entered seed
            runSeed = getIntent().getLongExtra(
                "CUSTOM_SEED",
                System.currentTimeMillis()
            );
        } else {
            // Normal random run
            runSeed = System.currentTimeMillis();
        }

        User user = SharedPreferencesUtil.getUser(this);

        String equippedSkinId = "default";
        if (user != null) {
            equippedSkinId = user.getEquippedSkinId();
        }

        AndroidApplicationConfiguration configuration = new AndroidApplicationConfiguration();
        configuration.useImmersiveMode = true;

        initialize(new MainActivity(new ScoreReporter() {
            @Override
            public void reportRun(
                int wave,
                int bestTimeSeconds,
                int enemiesKilled,
                int pickupsPicked,
                int coinsPicked,
                boolean win,
                boolean rangedChosen )
            {
                User user = SharedPreferencesUtil.getUser(AndroidLauncher.this);
                if (user == null) return;

                // daily challenge save
                if (dailyChallenge && win) {

                    String today = java.time.LocalDate.now().toString(); // yyyy-MM-dd

                    // save run
                    DailyRun dailyRun = new DailyRun(
                        user.getUid(),
                        user.getFullName(),
                        wave,
                        bestTimeSeconds,
                        rangedChosen ? "RANGED" : "MELEE"
                    );

                    DatabaseService.getInstance().saveDailyRun(
                        today,
                        dailyRun,
                        null
                    );

                    // only count completion & streak once per day
                    if (!today.equals(user.getLastDailyCompletionDate())) {
                        // daily completion count
                        user.setDailyChallengesCompleted(
                            user.getDailyChallengesCompleted() + 1
                        );

                        // daily streak logic
                        String lastDate = user.getLastDailyCompletionDate();

                        if (lastDate != null) {
                            java.time.LocalDate last =
                                java.time.LocalDate.parse(lastDate);

                            if (last.plusDays(1).equals(java.time.LocalDate.now())) {
                                user.setDailyStreak(user.getDailyStreak() + 1);
                            } else {
                                user.setDailyStreak(1);
                            }
                        } else {
                            user.setDailyStreak(1);
                        }

                        user.setBestDailyStreak(
                            Math.max(
                                user.getBestDailyStreak(),
                                user.getDailyStreak()
                            )
                        );

                        user.setLastDailyCompletionDate(today);
                    }

                    SharedPreferencesUtil.saveUser(AndroidLauncher.this, user);
                    DatabaseService.getInstance().updateUser(user, true, null);

                    return;
                }

                // highest wave
                if (wave > user.getHighestWave()) {
                    user.setHighestWave(wave);
                }

                // best time (only when boss is defeated)
                if (win && bestTimeSeconds > 0) {
                    int currentBest = user.getBestTime();
                    if (currentBest == 0 || bestTimeSeconds < currentBest) {
                        user.setBestTime(bestTimeSeconds);
                    }
                }

                // attempts
                user.setNumOfAttempts(user.getNumOfAttempts() + 1);

                if (win) {
                    // wins
                    user.setNumOfWins(user.getNumOfWins() + 1);

                    // streak logic
                    int newStreak = user.getCurrentStreak() + 1;
                    user.setCurrentStreak(newStreak);

                    user.setBestStreak(
                        Math.max(user.getBestStreak(), newStreak)
                    );
                } else {
                    // loss resets streak
                    user.setCurrentStreak(0);
                }

                if (rangedChosen) {
                    user.setPickedRanged(user.getPickedRanged() + 1);
                }

                // cumulative stats
                user.setEnemiesKilled(
                    user.getEnemiesKilled() + enemiesKilled
                );

                user.setPickupsPicked(
                    user.getPickupsPicked() + pickupsPicked
                );

                user.setNumOfCoins(
                    user.getNumOfCoins() + coinsPicked
                );

                SharedPreferencesUtil.saveUser(AndroidLauncher.this, user);

                User u = SharedPreferencesUtil.getUser(AndroidLauncher.this);
                Log.e("SAVE_SOURCE", "Saving user UID=" + u.getUid()
                    + " attempts=" + u.getNumOfAttempts());

                DatabaseService.getInstance().updateUser(user, true, new DatabaseService.DatabaseCallback<Void>() {
                    @Override
                    public void onCompleted(Void ignored) {
                        Log.d("DB", "User saved successfully");
                    }

                    @Override
                    public void onFailed(Exception e) {
                        Log.e("DB", "Save failed", e);
                    }
                });

                if (user.getGuildId() != null) {
                    DatabaseService.getInstance().addGuildRunStats(
                        user,
                        enemiesKilled,
                        win
                    );
                }
            }

            @Override
            public void reportHighestWave(int wave) {
                User user = SharedPreferencesUtil.getUser(AndroidLauncher.this);
                if (user == null) return;

                if (wave > user.getHighestWave()) {
                    user.setHighestWave(wave);
                    SharedPreferencesUtil.saveUser(AndroidLauncher.this, user);
                    DatabaseService.getInstance().updateUser(user, true, null);
                }
            }

        },
        selectedClass, difficulty, equippedSkinId, dailyChallenge, runSeed), configuration);
    }
}
