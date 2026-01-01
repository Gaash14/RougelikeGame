package com.example.rougelikegame.android.screens;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.SearchView;

import com.example.rougelikegame.R;
import com.example.rougelikegame.android.adapters.UserListAdapter;
import com.example.rougelikegame.android.services.DatabaseService;
import com.example.rougelikegame.android.utils.SharedPreferencesUtil;
import com.example.rougelikegame.models.User;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminActivity extends AppCompatActivity {

    private static final String TAG = "AdminActivity";

    private UserListAdapter userAdapter;
    private DatabaseService databaseService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin);

        User user = SharedPreferencesUtil.getUser(this);

        if (user == null || !user.isAdmin()) {
            finish();
            return;
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        databaseService = DatabaseService.getInstance();

        // Search
        SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                userAdapter.filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                userAdapter.filter(newText);
                return true;
            }
        });

        // RecyclerView
        RecyclerView usersList = findViewById(R.id.rv_users_list);
        usersList.setLayoutManager(new LinearLayoutManager(this));

        userAdapter = new UserListAdapter(new UserListAdapter.OnUserClickListener() {
            @Override
            public void onUserClick(User user) {
                Log.d(TAG, "User clicked: " + user.getUid());
                Intent intent = new Intent(AdminActivity.this, ProfileActivity.class);
                intent.putExtra("USER_UID", user.getUid());
                startActivity(intent);
            }

            @Override
            public void onLongUserClick(User user) {
                Log.d(TAG, "User long clicked: " + user.getUid());
            }

            @Override
            public void onDeleteClick(User user) {

                new androidx.appcompat.app.AlertDialog.Builder(AdminActivity.this)
                    .setTitle("Delete user")
                    .setMessage("Are you sure you want to delete this user?\n\n" +
                        user.getFullName())
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .setPositiveButton("Delete", (dialog, which) -> {

                        FirebaseDatabase.getInstance()
                            .getReference("users")
                            .child(user.getUid())
                            .removeValue()
                            .addOnSuccessListener(aVoid -> {
                                userAdapter.removeUser(user);
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Delete failed", e);
                            });
                    })
                    .show();
            }

            @Override
            public void onResetStatsClick(User user) {
                new AlertDialog.Builder(AdminActivity.this)
                    .setTitle("Reset user stats")
                    .setMessage(
                        "This will permanently reset all game stats for:\n\n"
                            + user.getFullName()
                    )
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Reset", (dialog, which) -> {

                        Map<String, Object> updates = new HashMap<>();
                        updates.put("highestWave", 0);
                        updates.put("bestTime", 0);
                        updates.put("enemiesKilled", 0);
                        updates.put("pickupsPicked", 0);
                        updates.put("numOfAttempts", 0);
                        updates.put("numOfWins", 0);

                        FirebaseDatabase.getInstance()
                            .getReference("users")
                            .child(user.getUid())
                            .updateChildren(updates)
                            .addOnSuccessListener(aVoid -> {

                                // Update local object
                                user.setHighestWave(0);
                                user.setBestTime(0);
                                user.setEnemiesKilled(0);
                                user.setPickupsPicked(0);
                                user.setNumOfAttempts(0);
                                user.setNumOfWins(0);

                                userAdapter.updateUser(user);
                            })
                            .addOnFailureListener(e ->
                                Log.e(TAG, "Reset stats failed", e));
                    })
                    .show();
            }
        });

        usersList.setAdapter(userAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
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
}
