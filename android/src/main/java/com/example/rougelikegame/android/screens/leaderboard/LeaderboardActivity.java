package com.example.rougelikegame.android.screens.leaderboard;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rougelikegame.R;
import com.example.rougelikegame.android.adapters.LeaderboardAdapter;
import com.example.rougelikegame.android.models.meta.User;
import com.example.rougelikegame.android.services.DatabaseService;
import com.example.rougelikegame.android.utils.SharedPreferencesUtil;

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

                if (users == null) users = new ArrayList<>();

                List<User> scoredUsers = new ArrayList<>();
                for (User u : users) {
                    if (u != null && u.getHighestWave() > 0) {
                        scoredUsers.add(u);
                    }
                }

                // Sort: wave DESC, time ASC
                Collections.sort(scoredUsers, new Comparator<User>() {
                    @Override
                    public int compare(User u1, User u2) {
                        int waveCompare = Integer.compare(
                            u2.getHighestWave(),
                            u1.getHighestWave()
                        );
                        if (waveCompare != 0) return waveCompare;

                        return Integer.compare(
                            u1.getBestTime(),
                            u2.getBestTime()
                        );
                    }
                });

                String currentUid =
                    SharedPreferencesUtil.getUserId(LeaderboardActivity.this);

                LeaderboardAdapter adapter = new LeaderboardAdapter(
                    LeaderboardActivity.this,
                    scoredUsers,
                    currentUid
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
}
