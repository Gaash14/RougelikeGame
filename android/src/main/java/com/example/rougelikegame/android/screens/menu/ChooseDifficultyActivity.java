package com.example.rougelikegame.android.screens.menu;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
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

    private Player.Difficulty selectedDifficulty = Player.Difficulty.NORMAL;
    private Player.PlayerClass selectedClass = Player.PlayerClass.MELEE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_difficulty);

        classSwitch = findViewById(R.id.classSwitch);
        classLabel = findViewById(R.id.classLabel);

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

                // Disable difficulty selection
                for (int i = 0; i < difficultyGroup.getChildCount(); i++) {
                    difficultyGroup.getChildAt(i).setEnabled(false);
                }
            } else {
                // Re-enable difficulty selection
                for (int i = 0; i < difficultyGroup.getChildCount(); i++) {
                    difficultyGroup.getChildAt(i).setEnabled(true);
                }
            }
        });

        startGameButton = findViewById(R.id.startGameButton);
        // Start game
        startGameButton.setOnClickListener(v -> {
            Intent intent = new Intent(ChooseDifficultyActivity.this, AndroidLauncher.class);
            intent.putExtra("PLAYER_CLASS", selectedClass.name());
            intent.putExtra("DIFFICULTY", selectedDifficulty.name());
            intent.putExtra("DAILY_CHALLENGE", dailyChallenge);
            startActivity(intent);
        });
    }
}
