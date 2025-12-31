package com.example.rougelikegame.android.screens;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rougelikegame.R;
import com.example.rougelikegame.models.Player;

public class ChooseDifficultyActivity extends AppCompatActivity {

    private Switch classSwitch;
    private TextView classLabel;
    private Button startGameButton;

    private Player.PlayerClass selectedClass = Player.PlayerClass.MELEE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_difficulty);

        classSwitch = findViewById(R.id.classSwitch);
        classLabel = findViewById(R.id.classLabel);
        startGameButton = findViewById(R.id.startGameButton);

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

        // Start game
        startGameButton.setOnClickListener(v -> {
            Intent intent = new Intent(ChooseDifficultyActivity.this, AndroidLauncher.class);
            intent.putExtra("PLAYER_CLASS", selectedClass.name());
            startActivity(intent);
        });
    }
}
