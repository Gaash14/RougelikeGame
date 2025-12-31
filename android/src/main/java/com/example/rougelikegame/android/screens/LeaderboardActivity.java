package com.example.rougelikegame.android.screens;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rougelikegame.R;
import com.example.rougelikegame.android.services.DatabaseService;
import com.example.rougelikegame.models.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LeaderboardActivity extends AppCompatActivity {

    private ListView listLeaderboard;
    private DatabaseService databaseService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        listLeaderboard = findViewById(R.id.listLeaderboard);
        databaseService = DatabaseService.getInstance();

        loadLeaderboard();
    }

    private void loadLeaderboard() {
        databaseService.getUserList(new DatabaseService.DatabaseCallback<List<User>>() {
            @Override
            public void onCompleted(List<User> users) {
                if (users == null) {
                    users = new ArrayList<>();
                }

                // Remove null users just in case
                List<User> cleanList = new ArrayList<>();
                for (User u : users) {
                    if (u != null) {
                        cleanList.add(u);
                    }
                }

                // Optional: filter users with no score
                // (if you consider "0" wave as "never played")
                List<User> scoredUsers = new ArrayList<>();
                for (User u : cleanList) {
                    if (u.getHighestWave() > 0) {
                        scoredUsers.add(u);
                    }
                }

                // Sort:
                // 1) highestWave DESC (higher first)
                // 2) if same wave -> bestTime ASC (lower time is better)
                Collections.sort(scoredUsers, new Comparator<User>() {
                    @Override
                    public int compare(User u1, User u2) {
                        int waveCompare = Integer.compare(
                            u2.getHighestWave(),
                            u1.getHighestWave()
                        );
                        if (waveCompare != 0) return waveCompare;

                        // tie-breaker by best time (smaller is better)
                        return Integer.compare(
                            u1.getBestTime(),
                            u2.getBestTime()
                        );
                    }
                });

                // Build strings for ListView
                List<String> lines = new ArrayList<>();
                int rank = 1;
                for (User u : scoredUsers) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(rank).append(". ");

                    // show name if exists, otherwise email
                    String name = u.getFullName();
                    if (name == null || name.trim().isEmpty()) {
                        name = u.getEmail();
                    }
                    sb.append(name);

                    sb.append("  |  Wave: ").append(u.getHighestWave());

                    int bestTime = u.getBestTime();
                    if (bestTime > 0 && bestTime < 999999) { // only show if actually set
                        sb.append("  |  Time: ").append(formatTime(bestTime));
                    }

                    lines.add(sb.toString());
                    rank++;
                }

                // If still nothing, show a placeholder line
                if (lines.isEmpty()) {
                    lines.add("No scores yet. Play a run to set the first record!");
                }

                // Put in ListView
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    LeaderboardActivity.this,
                    android.R.layout.simple_list_item_1,
                    lines
                );
                listLeaderboard.setAdapter(adapter);
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(
                    LeaderboardActivity.this,
                    "Failed to load leaderboard",
                    Toast.LENGTH_SHORT
                ).show();
                e.printStackTrace();
            }
        });
    }

    // helper: from seconds → mm:ss
    private String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}
