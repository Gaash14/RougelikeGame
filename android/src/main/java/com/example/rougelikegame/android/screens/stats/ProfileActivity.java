package com.example.rougelikegame.android.screens.stats;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rougelikegame.R;
import com.example.rougelikegame.android.screens.ShopActivity;
import com.example.rougelikegame.android.utils.ImageUtil;
import com.example.rougelikegame.android.utils.SharedPreferencesUtil;
import com.example.rougelikegame.android.models.User;

import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    // Header
    private ImageView imgAvatar;
    private TextView txtName, txtSubtitle;

    // Stats
    private TextView txtRuns, txtWinsLosses, txtWinRate;
    private TextView txtKills, txtPickups, txtCoins;
    private TextView txtHighestWave, txtBestTime;
    private TextView txtRangedPicks, txtMeleePicks;
    private TextView txtCurrentStreak, txtBestStreak;

    private Button btnOpenShop;

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
        txtCoins = findViewById(R.id.txtCoins);

        txtHighestWave = findViewById(R.id.txtHighestWave);
        txtBestTime = findViewById(R.id.txtBestTime);

        txtRangedPicks = findViewById(R.id.txtRangedPicks);
        txtMeleePicks = findViewById(R.id.txtMeleePicks);

        txtCurrentStreak = findViewById(R.id.txtCurrentStreak);
        txtBestStreak = findViewById(R.id.txtBestStreak);

        // Placeholder avatar (until image upload is added)
        imgAvatar.setImageResource(android.R.drawable.ic_menu_myplaces);

        findViewById(R.id.btnOpenShop).setOnClickListener(v -> {
            startActivity(new Intent(this, ShopActivity.class));
        });
    }

    private void loadUserToUI() {
        // Check if admin passed a user UID
        String userUid = getIntent().getStringExtra("USER_UID");

        if (userUid != null && !userUid.isEmpty()) {
            // ---------- ADMIN VIEW ----------
            loadUserFromDatabase(userUid);
            return;
        }

        // ---------- NORMAL USER ----------
        User local = SharedPreferencesUtil.getUser(this);
        if (local == null || local.getUid() == null) {
            showUser(null);
            return;
        }

        loadUserFromDatabase(local.getUid());
    }

    private void loadUserFromDatabase(String uid) {
        com.google.firebase.database.FirebaseDatabase.getInstance()
            .getReference("users")
            .child(uid)
            .get()
            .addOnSuccessListener(snapshot -> {
                User user = snapshot.getValue(User.class);
                showUser(user);
            })
            .addOnFailureListener(e -> {
                showUser(null);
            });
    }

    private void showUser(User user) {
        // ---------- GUEST ----------
        if (user == null) {
            txtName.setText("Guest");
            txtSubtitle.setText("Not logged in");

            txtRuns.setText("Runs: 0");
            txtWinsLosses.setText("Wins: 0 | Losses: 0");
            txtWinRate.setText("Win Rate: 0%");

            txtKills.setText("Enemies Killed: 0 (0.00 / run)");
            txtPickups.setText("Pickups: 0 (0.00 / run)");
            txtCoins.setText("Coins 0");

            txtHighestWave.setText("Highest Wave: 0");
            txtBestTime.setText("Best Time: —");

            txtRangedPicks.setText("Ranged: 0 (0%)");
            txtMeleePicks.setText("Melee: 0 (0%)");

            txtCurrentStreak.setText("Current Streak: 0");
            txtBestStreak.setText("Best Streak: 0");
            return;
        }

        // ---------- NAME ----------
        txtName.setText(user.getFullName());
        txtSubtitle.setText("Profile");

        // ---------- PROFILE IMAGE ----------
        if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
            Bitmap bitmap = ImageUtil.convertFrom64base(user.getProfileImage());
            if (bitmap != null) {
                imgAvatar.setImageBitmap(bitmap);
            }
        } else {
            // fallback icon
            imgAvatar.setImageResource(android.R.drawable.ic_menu_myplaces);
        }

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

        // ---------- STREAKS ----------
        int currentStreak = user.getCurrentStreak();
        int bestStreak = user.getBestStreak();

        txtCurrentStreak.setText("Current Streak: " + currentStreak);
        txtBestStreak.setText("Best Streak: " + bestStreak);

        // ---------- CLASS PICKS ----------
        int rangedPicks = user.getPickedRanged();
        int totalRuns = runs;

        // Safety clamp (in case of bad data)
        rangedPicks = Math.max(0, Math.min(rangedPicks, totalRuns));
        int meleePicks = Math.max(0, totalRuns - rangedPicks);

        double rangedPercent = (totalRuns > 0)
            ? (rangedPicks * 100.0 / totalRuns)
            : 0.0;

        double meleePercent = (totalRuns > 0)
            ? (meleePicks * 100.0 / totalRuns)
            : 0.0;

        txtRangedPicks.setText(String.format(
            Locale.US,
            "Ranged: %d (%.1f%%)",
            rangedPicks,
            rangedPercent
        ));

        txtMeleePicks.setText(String.format(
            Locale.US,
            "Melee: %d (%.1f%%)",
            meleePicks,
            meleePercent
        ));

        // ---------- KILLS / PICKUPS ----------
        int enemiesKilled = user.getEnemiesKilled();
        int pickupsPicked = user.getPickupsPicked();
        int numOfCoins = user.getNumOfCoins();

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

        txtCoins.setText("Coins: " + numOfCoins);

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
