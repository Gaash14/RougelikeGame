package com.example.rougelikegame.android.screens;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rougelikegame.R;
import com.example.rougelikegame.android.services.DatabaseService;
import com.example.rougelikegame.android.utils.SharedPreferencesUtil;
import com.example.rougelikegame.android.models.User;

public class CreateGuildActivity extends AppCompatActivity {

    private EditText editGuildName;
    private Button btnCreateGuild;
    private Button btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_guild);

        editGuildName = findViewById(R.id.editGuildName);
        btnCreateGuild = findViewById(R.id.btnCreateGuild);
        btnCancel = findViewById(R.id.btnCancel);

        btnCreateGuild.setOnClickListener(v -> createGuild());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void createGuild() {
        String guildName = editGuildName.getText().toString().trim();

        if (guildName.isEmpty()) {
            Toast.makeText(this, "Guild name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (guildName.length() < 3) {
            Toast.makeText(this, "Guild name too short", Toast.LENGTH_SHORT).show();
            return;
        }

        User currentUser = SharedPreferencesUtil.getUser(this);

        if (currentUser == null) {
            Toast.makeText(this, "User not loaded", Toast.LENGTH_SHORT).show();
            return;
        }

        String guildId = DatabaseService
            .getInstance()
            .createGuild(guildName, currentUser);

        // update local user
        currentUser.setGuildId(guildId);
        SharedPreferencesUtil.saveUser(this, currentUser);

        Toast.makeText(this, "Guild created!", Toast.LENGTH_SHORT).show();
        finish();
    }
}
