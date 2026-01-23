package com.example.rougelikegame.android.screens.menu;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rougelikegame.R;
import com.example.rougelikegame.android.models.characters.Player;
import com.example.rougelikegame.android.screens.launcher.AndroidLauncher;

public class ChooseDifficultyActivity extends AppCompatActivity {

    private Switch classSwitch;
    private TextView classLabel;
    private Button startGameButton;
    private RadioGroup difficultyGroup;

    private Switch dailyChallengeSwitch;
    private boolean dailyChallenge = false;
    private TextView txtDailyReset;
    private final Handler handler = new Handler();
    private EditText seedInput;

    private Player.Difficulty selectedDifficulty = Player.Difficulty.NORMAL;
    private Player.PlayerClass selectedClass = Player.PlayerClass.MELEE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_difficulty);

        classSwitch = findViewById(R.id.classSwitch);
        classLabel = findViewById(R.id.classLabel);

        txtDailyReset = findViewById(R.id.txtDailyReset);
        seedInput = findViewById(R.id.seedInput);

        // Toggle between MELEE / RANGED
        classSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedClass = Player.PlayerClass.RANGED;
                classLabel.setText("Class: RANGED");
            } else {
                selectedClass = Player.PlayerClass.MELEE;
                classLabel.setText("Class: MELEE");
            }
        });

        difficultyGroup = findViewById(R.id.difficultyGroup);
        difficultyGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.diffEasy) {
                selectedDifficulty = Player.Difficulty.EASY;
            } else if (checkedId == R.id.diffNormal) {
                selectedDifficulty = Player.Difficulty.NORMAL;
            } else if (checkedId == R.id.diffHard) {
                selectedDifficulty = Player.Difficulty.HARD;
            }
        });

        dailyChallengeSwitch = findViewById(R.id.dailyChallengeSwitch);

        dailyChallengeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            dailyChallenge = isChecked;

            if (isChecked) {
                // Lock difficulty to NORMAL
                selectedDifficulty = Player.Difficulty.NORMAL;
                difficultyGroup.check(R.id.diffNormal);

                for (int i = 0; i < difficultyGroup.getChildCount(); i++) {
                    difficultyGroup.getChildAt(i).setEnabled(false);
                }

                seedInput.setEnabled(false);
                seedInput.setText(""); // daily ignores custom seed

                startDailyCountdown();

            } else {
                // Re-enable difficulty
                for (int i = 0; i < difficultyGroup.getChildCount(); i++) {
                    difficultyGroup.getChildAt(i).setEnabled(true);
                }

                txtDailyReset.setVisibility(View.GONE);
                handler.removeCallbacksAndMessages(null);

                seedInput.setEnabled(true);
            }
        });

        startGameButton = findViewById(R.id.startGameButton);
        // Start game
        startGameButton.setOnClickListener(v -> {
            Intent intent = new Intent(ChooseDifficultyActivity.this, AndroidLauncher.class);
            intent.putExtra("PLAYER_CLASS", selectedClass.name());
            intent.putExtra("DIFFICULTY", selectedDifficulty.name());
            intent.putExtra("DAILY_CHALLENGE", dailyChallenge);

            String seedText = seedInput.getText().toString().trim();
            if (!seedText.isEmpty()) {
                try {
                    long customSeed = Long.parseLong(seedText);
                    intent.putExtra("CUSTOM_SEED", customSeed);
                } catch (NumberFormatException ignored) {
                    // invalid seed → ignore, fallback handled later
                }
            }

            startActivity(intent);
        });
    }

    private void startDailyCountdown() {
        txtDailyReset.setVisibility(View.VISIBLE);

        handler.post(new Runnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();

                java.time.LocalDateTime tomorrow =
                    java.time.LocalDate.now().plusDays(1).atStartOfDay();

                long millisUntilReset =
                    tomorrow.atZone(java.time.ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli()
                        - now;

                long seconds = millisUntilReset / 1000;
                long h = seconds / 3600;
                long m = (seconds % 3600) / 60;
                long s = seconds % 60;

                txtDailyReset.setText(
                    String.format(
                        "⏱ Daily resets in %02d:%02d:%02d", h, m, s
                    )
                );

                handler.postDelayed(this, 1000);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
