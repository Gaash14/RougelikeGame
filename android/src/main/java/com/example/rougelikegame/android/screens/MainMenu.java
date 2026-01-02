package com.example.rougelikegame.android.screens;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.example.rougelikegame.R;
import com.example.rougelikegame.android.utils.SharedPreferencesUtil;
import com.example.rougelikegame.models.User;

public class MainMenu extends AppCompatActivity {
    private static final String TAG = "MainMenu";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        Button startGame = findViewById(R.id.startGameButton);
        Button leaderboard = findViewById(R.id.leaderboardButton);
        Button guilds = findViewById(R.id.guildsButton);
        Button profile = findViewById(R.id.profileButton);
        Button updateUser = findViewById(R.id.updateUserButton);
        Button signOut = findViewById(R.id.signOutButton);
        Button exitButton = findViewById(R.id.exitButton);
        Button adminPanel = findViewById(R.id.adminPanelButton);

        User currentUser = SharedPreferencesUtil.getUser(this);

        if (currentUser != null && currentUser.isAdmin()) {
            adminPanel.setVisibility(View.VISIBLE);

            adminPanel.setOnClickListener(v -> {
                Intent intent = new Intent(MainMenu.this, AdminActivity.class);
                startActivity(intent);
            });
        } else {
            adminPanel.setVisibility(View.GONE);
        }

        startGame.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChooseDifficultyActivity.class);
            startActivity(intent);
        });

        leaderboard.setOnClickListener(v -> {
            Intent intent = new Intent(this, LeaderboardActivity.class);
            startActivity(intent);
        });

        guilds.setOnClickListener(v -> {
            Intent intent = new Intent(this, GuildInfoActivity.class);
            startActivity(intent);
        });

        profile.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
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
