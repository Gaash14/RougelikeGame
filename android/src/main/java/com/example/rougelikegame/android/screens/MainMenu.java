package com.example.rougelikegame.android.screens;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.example.rougelikegame.R;
import com.example.rougelikegame.android.utils.SharedPreferencesUtil;

public class MainMenu extends AppCompatActivity {
    private static final String TAG = "MainMenu";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        Button startGame = findViewById(R.id.startGameButton);
        Button leaderboard = findViewById(R.id.leaderboardButton);
        Button updateUser = findViewById(R.id.updateUserButton);
        Button signOut = findViewById(R.id.signOutButton);
        Button exitButton = findViewById(R.id.exitButton);

        startGame.setOnClickListener(v -> {
            Intent intent = new Intent(this, AndroidLauncher.class);
            startActivity(intent);
        });

        leaderboard.setOnClickListener(v -> {
            Intent intent = new Intent(this, LeaderboardActivity.class);
            startActivity(intent);
        });

        updateUser.setOnClickListener(v -> {
            Intent intent = new Intent(this, UpdateUserActivity.class);
            startActivity(intent);
        });

        signOut.setOnClickListener(v -> {
            Log.d(TAG, "Sign out button clicked");
            SharedPreferencesUtil.signOutUser(MainMenu.this);

            Log.d(TAG, "User signed out, redirecting to LandingActivity");
            Intent landingIntent = new Intent(MainMenu.this, LandingActivity.class);
            landingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(landingIntent);
        });

        exitButton.setOnClickListener(v -> finishAffinity());
    }
}
