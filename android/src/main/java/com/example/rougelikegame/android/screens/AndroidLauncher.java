package com.example.rougelikegame.android.screens;

import android.os.Bundle;
import android.util.Log;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.example.rougelikegame.ScoreReporter;
import com.example.rougelikegame.android.services.DatabaseService;
import com.example.rougelikegame.android.utils.SharedPreferencesUtil;
import com.example.rougelikegame.models.Player;
import com.example.rougelikegame.models.User;
import com.example.rougelikegame.screens.MainActivity;

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

        AndroidApplicationConfiguration configuration = new AndroidApplicationConfiguration();
        configuration.useImmersiveMode = true;

        initialize(new MainActivity(new ScoreReporter() {
            @Override
            public void reportRun(
                int wave,
                int bestTimeSeconds,
                int enemiesKilled,
                int pickupsPicked,
                boolean win )
            {
                User user = SharedPreferencesUtil.getUser(AndroidLauncher.this);
                if (user == null) return;

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

                // attempts & wins
                user.setNumOfAttempts(user.getNumOfAttempts() + 1);

                if (win) {
                    user.setNumOfWins(user.getNumOfWins() + 1);
                }

                // cumulative stats
                user.setEnemiesKilled(
                    user.getEnemiesKilled() + enemiesKilled
                );

                user.setPickupsPicked(
                    user.getPickupsPicked() + pickupsPicked
                );

                SharedPreferencesUtil.saveUser(AndroidLauncher.this, user);

                User u = SharedPreferencesUtil.getUser(AndroidLauncher.this);
                Log.e("SAVE_SOURCE", "Saving user UID=" + u.getUid()
                    + " attempts=" + u.getNumOfAttempts());

                DatabaseService.getInstance().updateUser(user, new DatabaseService.DatabaseCallback<Void>() {
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
                    DatabaseService.getInstance().updateUser(user, null);
                }
            }

        },
        selectedClass, difficulty), configuration);
    }
}
