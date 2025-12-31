package com.example.rougelikegame.android.screens;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.example.rougelikegame.ScoreReporter;
import com.example.rougelikegame.android.services.DatabaseService;
import com.example.rougelikegame.android.utils.SharedPreferencesUtil;
import com.example.rougelikegame.models.User;
import com.example.rougelikegame.screens.MainActivity;

/** Launches the Android application. */
public class AndroidLauncher extends AndroidApplication {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidApplicationConfiguration configuration = new AndroidApplicationConfiguration();
        configuration.useImmersiveMode = true;
        initialize(new MainActivity(new ScoreReporter() {
            @Override
            public void saveHighestWave(int wave) {
                User user = SharedPreferencesUtil.getUser(AndroidLauncher.this);
                if (user == null) return;

                // only update if better
                if (wave > user.GetHighestWave()) {
                    user.setHighestWave(wave);
                    SharedPreferencesUtil.saveUser(AndroidLauncher.this, user);
                    DatabaseService.getInstance().updateUser(user, null);
                }
            }

            @Override
            public void saveBestTime(int bestTimeSeconds) {
                User user = SharedPreferencesUtil.getUser(AndroidLauncher.this);
                if (user == null) return;

                int currentBest = user.GetBestTime();

                // 0 means "no record yet"
                if (currentBest == 0 || bestTimeSeconds < currentBest) {
                    user.setBestTime(bestTimeSeconds);
                    SharedPreferencesUtil.saveUser(AndroidLauncher.this, user);
                    DatabaseService.getInstance().updateUser(user, null);
                }
            }

            @Override
            public void addAttempt() {
                User user = SharedPreferencesUtil.getUser(AndroidLauncher.this);
                if (user == null) return;

                user.setNumOfAttempts(user.GetNumOfAttempts() + 1);

                SharedPreferencesUtil.saveUser(AndroidLauncher.this, user);
                DatabaseService.getInstance().updateUser(user, null);
            }

            @Override
            public void addWin() {
                User user = SharedPreferencesUtil.getUser(AndroidLauncher.this);
                if (user == null) return;

                user.setNumOfWins(user.GetNumOfWins() + 1);

                SharedPreferencesUtil.saveUser(AndroidLauncher.this, user);
                DatabaseService.getInstance().updateUser(user, null);
            }
        }), configuration);

    }
}
