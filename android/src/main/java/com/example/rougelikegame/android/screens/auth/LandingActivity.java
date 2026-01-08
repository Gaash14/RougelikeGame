package com.example.rougelikegame.android.screens.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.example.rougelikegame.R;
import com.example.rougelikegame.android.screens.menu.MainMenu;
import com.example.rougelikegame.android.utils.SharedPreferencesUtil;

public class LandingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // auto login
        if (SharedPreferencesUtil.isUserLoggedIn(this)) {
            Intent mainIntent = new Intent(LandingActivity.this, MainMenu.class);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(mainIntent);
            finish();
            return;
        }

        setContentView(R.layout.activity_landing);

        Button registerButton = findViewById(R.id.registerButton);
        Button loginButton = findViewById(R.id.loginButton);
        Button exitButton = findViewById(R.id.exitButton);

        registerButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
        });

        loginButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        });

        exitButton.setOnClickListener(v -> {
            finishAffinity();
        });
    }
}
