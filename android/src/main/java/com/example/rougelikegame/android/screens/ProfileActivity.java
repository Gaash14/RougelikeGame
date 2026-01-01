package com.example.rougelikegame.android.screens;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rougelikegame.R;
import com.example.rougelikegame.android.utils.SharedPreferencesUtil;
import com.example.rougelikegame.models.User;

import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    // Header
    private ImageView imgAvatar;
    private TextView txtName, txtSubtitle;

    // Stats
    private TextView txtRuns, txtWinsLosses, txtWinRate;
    private TextView txtKills, txtPickups;
    private TextView txtHighestWave, txtBestTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        bindViews();
        loadUserToUI();
    }

    private void bindViews() {
        imgAvatar = findViewById(R.id.imgAvatar);
        txtName = findViewById(R.id.txtName);
        txtSubtitle = findViewById(R.id.txtSubtitle);

        txtRuns = findViewById(R.id.txtRuns);
        txtWinsLosses = findViewById(R.id.txtWinsLosses);
        txtWinRate = findViewById(R.id.txtWinRate);

        txtKills = findViewById(R.id.txtKills);
        txtPickups = findViewById(R.id.txtPickups);

        txtHighestWave = findViewById(R.id.txtHighestWave);
        txtBestTime = findViewById(R.id.txtBestTime);

        // Placeholder avatar (until image upload is added)
        imgAvatar.setImageResource(android.R.drawable.ic_menu_myplaces);
    }

    private void loadUserToUI() {
        User user = SharedPreferencesUtil.getUser(this);

        // ---------- GUEST ----------
        if (user == null) {
            txtName.setText("Guest");
            txtSubtitle.setText("Not logged in");

            txtRuns.setText("Runs: 0");
            txtWinsLosses.setText("Wins: 0 | Losses: 0");
            txtWinRate.setText("Win Rate: 0%");

            txtKills.setText("Enemies Killed: 0 (0.00 / run)");
            txtPickups.setText("Pickups: 0 (0.00 / run)");

            txtHighestWave.setText("Highest Wave: 0");
            txtBestTime.setText("Best Time: —");
            return;
        }

        // ---------- NAME ----------
        txtName.setText(user.getFullName());
        txtSubtitle.setText("Profile");

        // ---------- RUNS / WINS ----------
        int runs = user.getNumOfAttempts();
        int wins = user.getNumOfWins();
        int losses = Math.max(0, runs - wins);

        double winRate = (runs > 0)
            ? (wins * 100.0 / runs)
            : 0.0;

        txtRuns.setText("Runs: " + runs);
        txtWinsLosses.setText("Wins: " + wins + " | Losses: " + losses);
        txtWinRate.setText(String.format(Locale.US, "Win Rate: %.1f%%", winRate));

        // ---------- KILLS / PICKUPS ----------
        int enemiesKilled = user.getEnemiesKilled();
        int pickupsPicked = user.getPickupsPicked();

        double killsPerRun = (runs > 0)
            ? (enemiesKilled * 1.0 / runs)
            : 0.0;

        double pickupsPerRun = (runs > 0)
            ? (pickupsPicked * 1.0 / runs)
            : 0.0;

        txtKills.setText(String.format(
            Locale.US,
            "Enemies Killed: %d (%.2f / run)",
            enemiesKilled,
            killsPerRun
        ));

        txtPickups.setText(String.format(
            Locale.US,
            "Pickups: %d (%.2f / run)",
            pickupsPicked,
            pickupsPerRun
        ));

        // ---------- BESTS ----------
        txtHighestWave.setText("Highest Wave: " + user.getHighestWave());

        int bestTimeSec = user.getBestTime();
        if (bestTimeSec <= 0) {
            txtBestTime.setText("Best Time: —");
        } else {
            txtBestTime.setText("Best Time: " + formatTime(bestTimeSec));
        }
    }

    private static String formatTime(int seconds) {
        int minutes = seconds / 60;
        int secs = seconds % 60;

        if (minutes > 0) {
            return String.format(Locale.US, "%d:%02d", minutes, secs);
        }
        return String.format(Locale.US, "%ds", secs);
    }
}
