package com.example.rougelikegame.android.screens.leaderboard;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rougelikegame.R;
import com.example.rougelikegame.android.adapters.DailyLeaderboardAdapter;
import com.example.rougelikegame.android.adapters.LeaderboardAdapter;
import com.example.rougelikegame.android.models.meta.DailyRun;
import com.example.rougelikegame.android.models.meta.User;
import com.example.rougelikegame.android.services.DatabaseService;
import com.example.rougelikegame.android.utils.SharedPreferencesUtil;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LeaderboardActivity extends AppCompatActivity {
    private enum Mode {
        NORMAL,
        DAILY
    }
    private Mode currentMode = Mode.NORMAL;
    private ListView listLeaderboard;
    private DatabaseService databaseService;

    private TextView txtDailyReset;
    private TextView txtEmptyDaily;
    private final android.os.Handler handler = new android.os.Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        listLeaderboard = findViewById(R.id.listLeaderboard);
        databaseService = DatabaseService.getInstance();

        Button btnNormal = findViewById(R.id.btnNormal);
        Button btnDaily = findViewById(R.id.btnDaily);

        txtDailyReset = findViewById(R.id.txtDailyReset);
        txtEmptyDaily = findViewById(R.id.txtEmptyDaily);

        // Default: NORMAL
        currentMode = Mode.NORMAL;
        loadCurrentLeaderboard();

        btnNormal.setOnClickListener(v -> {
            currentMode = Mode.NORMAL;
            btnNormal.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(0xFFFF9800));
            btnDaily.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(0xFF333333));
            loadCurrentLeaderboard();
            txtDailyReset.setVisibility(View.GONE);
            handler.removeCallbacksAndMessages(null);
        });

        btnDaily.setOnClickListener(v -> {
            currentMode = Mode.DAILY;
            btnDaily.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(0xFFFF9800));
            btnNormal.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(0xFF333333));
            loadCurrentLeaderboard();
            startDailyCountdown();
        });
    }

    private void loadCurrentLeaderboard() {
        if (currentMode == Mode.DAILY) {
            loadDailyLeaderboard();
        } else {
            loadLeaderboard();
        }
    }

    private void loadLeaderboard() {
        txtEmptyDaily.setVisibility(View.GONE);
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

    private void loadDailyLeaderboard() {
        String todayKey = java.time.LocalDate.now().toString();

        databaseService.getDailyRuns(todayKey, new DatabaseService.DatabaseCallback<List<DailyRun>>() {
            @Override
            public void onCompleted(List<DailyRun> runs) {

                if (runs == null || runs.isEmpty()) {
                    txtEmptyDaily.setVisibility(View.VISIBLE);
                    listLeaderboard.setAdapter(null);
                    return;
                }

                txtEmptyDaily.setVisibility(View.GONE);

                runs.sort((a, b) -> {
                    int waveCompare = Integer.compare(b.wave, a.wave);
                    if (waveCompare != 0) return waveCompare;
                    return Integer.compare(a.time, b.time);
                });

                String currentUid =
                    SharedPreferencesUtil.getUserId(LeaderboardActivity.this);

                DailyLeaderboardAdapter adapter =
                    new DailyLeaderboardAdapter(
                        LeaderboardActivity.this,
                        runs,
                        currentUid
                    );

                listLeaderboard.setAdapter(adapter);
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(
                    LeaderboardActivity.this,
                    "Failed to load daily leaderboard",
                    Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private void startDailyCountdown() {
        txtDailyReset.setVisibility(android.view.View.VISIBLE);

        handler.post(new Runnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();

                java.time.LocalDateTime tomorrow =
                    java.time.LocalDate.now().plusDays(1).atStartOfDay();

                long millisUntilReset =
                    java.time.ZoneId.systemDefault()
                        .getRules()
                        .getOffset(java.time.Instant.now())
                        .getTotalSeconds() * 1000L
                        + tomorrow.atZone(java.time.ZoneId.systemDefault())
                        .toInstant().toEpochMilli()
                        - now;

                long seconds = millisUntilReset / 1000;
                long h = seconds / 3600;
                long m = (seconds % 3600) / 60;
                long s = seconds % 60;

                txtDailyReset.setText(
                    String.format(
                        "Daily resets in %02d:%02d:%02d", h, m, s
                    )
                );

                handler.postDelayed(this, 1000);
            }
        });
    }
}
