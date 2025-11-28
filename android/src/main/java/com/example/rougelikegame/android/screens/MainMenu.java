package com.example.rougelikegame.android.screens;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.example.rougelikegame.R;

public class MainMenu extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        Button startGame = findViewById(R.id.startGameButton);
        Button leaderboard = findViewById(R.id.leaderboardButton);
        Button exitButton = findViewById(R.id.exitButton);

        startGame.setOnClickListener(v -> {
            Intent intent = new Intent(this, AndroidLauncher.class);
            startActivity(intent);
        });

        leaderboard.setOnClickListener(v -> {
            Intent intent = new Intent(this, AndroidLauncher.class);
            startActivity(intent);
        });

        exitButton.setOnClickListener(v -> finishAffinity());
    }
}
