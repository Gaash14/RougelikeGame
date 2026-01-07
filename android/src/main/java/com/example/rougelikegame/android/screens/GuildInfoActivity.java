package com.example.rougelikegame.android.screens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import android.app.AlertDialog;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rougelikegame.R;
import com.example.rougelikegame.android.utils.SharedPreferencesUtil;
import com.example.rougelikegame.android.models.User;
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

    private Button btnLeaveGuild;
    private Button btnDeleteGuild;

    private Button btnCreateGuild;
    private Button btnJoinGuild;

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

        btnLeaveGuild = findViewById(R.id.btnLeaveGuild);
        btnDeleteGuild = findViewById(R.id.btnDeleteGuild);

        btnCreateGuild = findViewById(R.id.btnCreateGuild);
        btnJoinGuild = findViewById(R.id.btnJoinGuild);

        layoutGuildInfo = findViewById(R.id.layoutGuildInfo);
        layoutNoGuild = findViewById(R.id.layoutNoGuild);

        btnLeaveGuild.setOnClickListener(v -> leaveGuild());
        btnDeleteGuild.setOnClickListener(v -> confirmDeleteGuild());

        btnCreateGuild.setOnClickListener(v ->
            startActivity(new Intent(this, CreateGuildActivity.class))
        );
        btnJoinGuild.setOnClickListener(v ->
            startActivity(new Intent(this, JoinGuildActivity.class))
        );

        refreshGuildState();
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

                    String ownerUid = snapshot.child("ownerUid").getValue(String.class);

                    User user = SharedPreferencesUtil.getUser(GuildInfoActivity.this);
                    if (user != null && ownerUid != null && ownerUid.equals(user.getUid())) {
                        btnDeleteGuild.setVisibility(View.VISIBLE);
                    } else {
                        btnDeleteGuild.setVisibility(View.GONE);
                    }
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

    private void leaveGuild() {
        User user = SharedPreferencesUtil.getUser(this);
        if (user == null || user.getGuildId() == null) return;

        String guildId = user.getGuildId();
        String uid = user.getUid();

        FirebaseDatabase.getInstance()
            .getReference("guilds")
            .child(guildId)
            .addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    String ownerUid = snapshot.child("ownerUid").getValue(String.class);

                    // Remove user from members
                    FirebaseDatabase.getInstance()
                        .getReference("guilds")
                        .child(guildId)
                        .child("members")
                        .child(uid)
                        .removeValue();

                    // Remove guildId from user
                    FirebaseDatabase.getInstance()
                        .getReference("users")
                        .child(uid)
                        .child("guildId")
                        .removeValue();

                    // If owner is leaving --> transfer ownership
                    if (uid.equals(ownerUid)) {
                        boolean transferred = false;

                        for (DataSnapshot member : snapshot.child("members").getChildren()) {
                            String newOwnerUid = member.getKey();

                            if (!newOwnerUid.equals(uid)) {
                                FirebaseDatabase.getInstance()
                                    .getReference("guilds")
                                    .child(guildId)
                                    .child("ownerUid")
                                    .setValue(newOwnerUid);

                                transferred = true;
                                break;
                            }
                        }

                        // if owner is the last member --> delete guild
                        if (!transferred) {
                            FirebaseDatabase.getInstance()
                                .getReference("guilds")
                                .child(guildId)
                                .removeValue();
                        }
                    }


                    // Update local user
                    user.setGuildId(null);
                    SharedPreferencesUtil.saveUser(GuildInfoActivity.this, user);

                    layoutGuildInfo.setVisibility(View.GONE);
                    layoutNoGuild.setVisibility(View.VISIBLE);
                    txtGuildName.setText("Guild");
                }

                @Override
                public void onCancelled(DatabaseError error) {}
            });
    }

    private void confirmDeleteGuild() {
        new AlertDialog.Builder(this)
            .setTitle("Delete Guild")
            .setMessage("This will permanently delete the guild for all members. Continue?")
            .setPositiveButton("Delete", (d, w) -> deleteGuild())
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void deleteGuild() {
        User user = SharedPreferencesUtil.getUser(this);
        if (user == null || user.getGuildId() == null) return;

        String guildId = user.getGuildId();

        FirebaseDatabase.getInstance()
            .getReference("guilds")
            .child(guildId)
            .addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    // Remove guildId from all members
                    for (DataSnapshot member : snapshot.child("members").getChildren()) {
                        FirebaseDatabase.getInstance()
                            .getReference("users")
                            .child(member.getKey())
                            .child("guildId")
                            .removeValue();
                    }

                    // Delete the guild
                    FirebaseDatabase.getInstance()
                        .getReference("guilds")
                        .child(guildId)
                        .removeValue();

                    // Update local user
                    user.setGuildId(null);
                    SharedPreferencesUtil.saveUser(GuildInfoActivity.this, user);

                    layoutGuildInfo.setVisibility(View.GONE);
                    layoutNoGuild.setVisibility(View.VISIBLE);
                }

                @Override
                public void onCancelled(DatabaseError error) {}
            });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshGuildState();
    }

    private void refreshGuildState() {
        User user = SharedPreferencesUtil.getUser(this);

        if (user == null || user.getGuildId() == null) {
            layoutGuildInfo.setVisibility(View.GONE);
            layoutNoGuild.setVisibility(View.VISIBLE);

            txtGuildName.setText("Guild");

            return;
        }

        layoutNoGuild.setVisibility(View.GONE);
        layoutGuildInfo.setVisibility(View.VISIBLE);
        loadGuild(user.getGuildId());
    }
}
