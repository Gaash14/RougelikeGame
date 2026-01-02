package com.example.rougelikegame.android.screens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rougelikegame.R;
import com.example.rougelikegame.android.utils.SharedPreferencesUtil;
import com.example.rougelikegame.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

public class GuildInfoActivity extends AppCompatActivity {

    private TextView txtGuildName;
    private TextView txtMemberCount;
    private TextView txtEnemiesKilled;
    private TextView txtWins;
    private TextView txtAttempts;

    private LinearLayout layoutGuildInfo;
    private LinearLayout layoutNoGuild;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guild_info);

        txtGuildName = findViewById(R.id.txtGuildName);
        txtMemberCount = findViewById(R.id.txtMemberCount);
        txtEnemiesKilled = findViewById(R.id.txtEnemiesKilled);
        txtWins = findViewById(R.id.txtWins);
        txtAttempts = findViewById(R.id.txtAttempts);
        layoutGuildInfo = findViewById(R.id.layoutGuildInfo);
        layoutNoGuild = findViewById(R.id.layoutNoGuild);

        User user = SharedPreferencesUtil.getUser(this);

        if (user == null || user.getGuildId() == null) {
            layoutGuildInfo.setVisibility(View.GONE);
            layoutNoGuild.setVisibility(View.VISIBLE);

            findViewById(R.id.btnCreateGuild).setOnClickListener(v ->
                startActivity(new Intent(this, CreateGuildActivity.class))
            );

            findViewById(R.id.btnJoinGuild).setOnClickListener(v ->
                startActivity(new Intent(this, JoinGuildActivity.class))
            );

            return;
        }


        loadGuild(user.getGuildId());
    }

    private void loadGuild(String guildId) {
        FirebaseDatabase.getInstance()
            .getReference("guilds")
            .child(guildId)
            .addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (!snapshot.exists()) {
                        Toast.makeText(
                            GuildInfoActivity.this,
                            "Guild not found",
                            Toast.LENGTH_SHORT
                        ).show();
                        finish();
                        return;
                    }

                    String name = snapshot.child("name").getValue(String.class);
                    txtGuildName.setText(name != null ? name : "Unknown");

                    Map<String, Boolean> members =
                        (Map<String, Boolean>) snapshot.child("members").getValue();

                    int memberCount = members != null ? members.size() : 0;
                    txtMemberCount.setText("Members: " + memberCount);

                    Integer kills =
                        snapshot.child("totalEnemiesKilled").getValue(Integer.class);
                    Integer wins =
                        snapshot.child("totalWins").getValue(Integer.class);
                    Integer attempts =
                        snapshot.child("totalAttempts").getValue(Integer.class);

                    txtEnemiesKilled.setText(
                        "Enemies Killed: " + (kills != null ? kills : 0)
                    );
                    txtWins.setText(
                        "Wins: " + (wins != null ? wins : 0)
                    );
                    txtAttempts.setText(
                        "Attempts: " + (attempts != null ? attempts : 0)
                    );
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(
                        GuildInfoActivity.this,
                        "Failed to load guild",
                        Toast.LENGTH_SHORT
                    ).show();
                }
            });
    }
}
