package com.example.rougelikegame.android.screens;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rougelikegame.R;
import com.example.rougelikegame.android.adapters.GuildListAdapter;
import com.example.rougelikegame.android.adapters.UserListAdapter;
import com.example.rougelikegame.android.services.DatabaseService;
import com.example.rougelikegame.android.utils.SharedPreferencesUtil;
import com.example.rougelikegame.android.models.Guild;
import com.example.rougelikegame.android.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminActivity extends AppCompatActivity {

    private static final String TAG = "AdminActivity";

    private DatabaseService databaseService;

    private UserListAdapter userAdapter;
    private GuildListAdapter guildAdapter;

    private RecyclerView recyclerView;
    private SearchView searchView;

    private TextView titleText;
    private TextView hintText;
    private Button btnUsers;
    private Button btnGuilds;

    private boolean showingUsers = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin);

        // admin check
        User admin = SharedPreferencesUtil.getUser(this);
        if (admin == null || !admin.isAdmin()) {
            finish();
            return;
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // views
        titleText = findViewById(R.id.titleText);
        hintText = findViewById(R.id.textView2);
        btnUsers = findViewById(R.id.btnUsers);
        btnGuilds = findViewById(R.id.btnGuilds);
        searchView = findViewById(R.id.searchView);

        recyclerView = findViewById(R.id.rv_users_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        databaseService = DatabaseService.getInstance();

        setupAdapters();
        setupSearch();
        setupButtons();

        // Default mode
        showUsers();
    }

    // Setup
    private void setupAdapters() {

        // ---------- USER ADAPTER ----------
        userAdapter = new UserListAdapter(new UserListAdapter.OnUserClickListener() {
            @Override
            public void onUserClick(User user) {
                Intent intent = new Intent(AdminActivity.this, ProfileActivity.class);
                intent.putExtra("USER_UID", user.getUid());
                startActivity(intent);
            }

            @Override
            public void onLongUserClick(User user) {}

            @Override
            public void onDeleteClick(User user) {
                new AlertDialog.Builder(AdminActivity.this)
                    .setTitle("Delete user")
                    .setMessage("Delete this user?\n\n" + user.getFullName())
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Delete", (d, w) -> {

                        FirebaseDatabase.getInstance()
                            .getReference("users")
                            .child(user.getUid())
                            .removeValue()
                            .addOnSuccessListener(v ->
                                userAdapter.removeUser(user)
                            );
                    })
                    .show();
            }

            @Override
            public void onResetStatsClick(User user) {
                new AlertDialog.Builder(AdminActivity.this)
                    .setTitle("Reset user stats")
                    .setMessage("Reset all stats for:\n\n" + user.getFullName())
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Reset", (d, w) -> {

                        Map<String, Object> updates = new HashMap<>();
                        updates.put("highestWave", 0);
                        updates.put("bestTime", 0);
                        updates.put("enemiesKilled", 0);
                        updates.put("pickupsPicked", 0);
                        updates.put("numOfAttempts", 0);
                        updates.put("numOfWins", 0);
                        updates.put("bestStreak", 0);
                        updates.put("currentStreak", 0);
                        updates.put("pickedRanged", 0);
                        updates.put("numOfCoins", 0);

                        FirebaseDatabase.getInstance()
                            .getReference("users")
                            .child(user.getUid())
                            .updateChildren(updates)
                            .addOnSuccessListener(v -> {
                                user.setHighestWave(0);
                                user.setBestTime(0);
                                user.setEnemiesKilled(0);
                                user.setPickupsPicked(0);
                                user.setNumOfAttempts(0);
                                user.setNumOfWins(0);
                                user.setPickedRanged(0);
                                user.setCurrentStreak(0);
                                user.setBestStreak(0);
                                user.setNumOfCoins(0);
                                userAdapter.updateUser(user);
                            });
                    })
                    .show();
            }
        });

        // ---------- GUILD ADAPTER ----------
        guildAdapter = new GuildListAdapter(new GuildListAdapter.OnGuildClickListener() {
            @Override
            public void onDeleteClick(Guild guild) {
                new AlertDialog.Builder(AdminActivity.this)
                    .setTitle("Delete guild")
                    .setMessage("Delete this guild?\n\n" + guild.getName())
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Delete", (d, w) -> deleteGuild(guild))
                    .show();
            }

            @Override
            public void onResetStatsClick(Guild guild) {
                new AlertDialog.Builder(AdminActivity.this)
                    .setTitle("Reset guild stats")
                    .setMessage("Reset all stats for:\n\n" + guild.getName())
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Reset", (d, w) -> resetGuildStats(guild))
                    .show();
            }
        });
    }

    private void setupButtons() {
        btnUsers.setOnClickListener(v -> showUsers());
        btnGuilds.setOnClickListener(v -> showGuilds());
    }

    private void setupSearch() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String q) {
                filter(q);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String q) {
                filter(q);
                return true;
            }
        });
    }

    // mode switching

    private void showUsers() {
        showingUsers = true;
        titleText.setText("User Management");
        hintText.setText("Users will appear here");

        recyclerView.setAdapter(userAdapter);

        databaseService.getUserList(new DatabaseService.DatabaseCallback<List<User>>() {
            @Override
            public void onCompleted(List<User> users) {
                userAdapter.setUserList(users);
            }

            @Override
            public void onFailed(Exception e) {
                Log.e(TAG, "Failed to load users", e);
            }
        });
    }

    private void showGuilds() {
        showingUsers = false;
        titleText.setText("Guild Management");
        hintText.setText("Guilds will appear here");

        recyclerView.setAdapter(guildAdapter);

        databaseService.getGuildList(new DatabaseService.DatabaseCallback<List<Guild>>() {
            @Override
            public void onCompleted(List<Guild> guilds) {
                guildAdapter.setGuildList(guilds);
            }

            @Override
            public void onFailed(Exception e) {
                Log.e(TAG, "Failed to load guilds", e);
            }
        });
    }

    private void filter(String text) {
        if (showingUsers) {
            userAdapter.filter(text);
        } else {
            guildAdapter.filter(text);
        }
    }

    // guild admin actions

    private void resetGuildStats(Guild guild) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("totalEnemiesKilled", 0);
        updates.put("totalWins", 0);
        updates.put("totalAttempts", 0);

        FirebaseDatabase.getInstance()
            .getReference("guilds")
            .child(guild.getGuildId())
            .updateChildren(updates)
            .addOnSuccessListener(v -> {
                guild.setTotalEnemiesKilled(0);
                guild.setTotalWins(0);
                guild.setTotalAttempts(0);
                guildAdapter.updateGuild(guild);
            });
    }

    private void deleteGuild(Guild guild) {
        FirebaseDatabase.getInstance()
            .getReference("guilds")
            .child(guild.getGuildId())
            .addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot snapshot) {

                    for (DataSnapshot member : snapshot.child("members").getChildren()) {
                        FirebaseDatabase.getInstance()
                            .getReference("users")
                            .child(member.getKey())
                            .child("guildId")
                            .removeValue();
                    }

                    FirebaseDatabase.getInstance()
                        .getReference("guilds")
                        .child(guild.getGuildId())
                        .removeValue()
                        .addOnSuccessListener(v ->
                            guildAdapter.removeGuild(guild)
                        );
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Log.e(TAG, "Delete guild cancelled", error.toException());
                }
            });
    }
}
