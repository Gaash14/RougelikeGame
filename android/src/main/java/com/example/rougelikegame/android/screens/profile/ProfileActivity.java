package com.example.rougelikegame.android.screens.profile;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rougelikegame.R;
import com.example.rougelikegame.android.models.meta.Guild;
import com.example.rougelikegame.android.models.meta.User;
import com.example.rougelikegame.android.screens.menu.ItemsActivity;
import com.example.rougelikegame.android.screens.shop.ShopActivity;
import com.example.rougelikegame.android.utils.ImageUtil;
import com.example.rougelikegame.android.utils.SharedPreferencesUtil;

import java.util.Locale;

/**
 * Activity for displaying and managing the user profile and statistics.
 */
public class ProfileActivity extends AppCompatActivity {

    // Header
    private ImageView imgAvatar;
    private TextView txtName;
    private TextView txtSubtitle;
    private TextView txtGuildName;
    private ImageButton btnUpdateUser;

    // Stats
    private TextView txtRuns;
    private TextView txtWinsLosses;
    private TextView txtWinRate;
    private TextView txtKills;
    private TextView txtPickups;
    private TextView txtItemsPicked;
    private TextView txtCoins;
    private TextView txtHighestWave;
    private TextView txtBestTime;
    private TextView txtRangedPicks;
    private TextView txtMeleePicks;
    private TextView txtCurrentStreak;
    private TextView txtBestStreak;
    private TextView txtDailyStats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        bindViews();
        loadUserToUI();
    }

    /**
     * Binds UI components to class fields.
     */
    private void bindViews() {
        imgAvatar = findViewById(R.id.imgAvatar);
        txtName = findViewById(R.id.txtName);
        txtSubtitle = findViewById(R.id.txtSubtitle);
        txtGuildName = findViewById(R.id.txtGuildName);
        btnUpdateUser = findViewById(R.id.btnUpdateUser);

        txtRuns = findViewById(R.id.txtRuns);
        txtWinsLosses = findViewById(R.id.txtWinsLosses);
        txtWinRate = findViewById(R.id.txtWinRate);

        txtKills = findViewById(R.id.txtKills);
        txtPickups = findViewById(R.id.txtPickups);
        txtCoins = findViewById(R.id.txtCoins);
        txtItemsPicked = findViewById(R.id.txtItemsPicked);

        txtHighestWave = findViewById(R.id.txtHighestWave);
        txtBestTime = findViewById(R.id.txtBestTime);

        txtRangedPicks = findViewById(R.id.txtRangedPicks);
        txtMeleePicks = findViewById(R.id.txtMeleePicks);

        txtCurrentStreak = findViewById(R.id.txtCurrentStreak);
        txtBestStreak = findViewById(R.id.txtBestStreak);

        txtDailyStats = findViewById(R.id.txtDailyStats);

        // Placeholder avatar
        imgAvatar.setImageResource(android.R.drawable.ic_menu_myplaces);

        btnUpdateUser.setOnClickListener(v -> {
            Intent intent = new Intent(this, UpdateUserActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.btnOpenShop).setOnClickListener(v -> {
            startActivity(new Intent(this, ShopActivity.class));
        });

        findViewById(R.id.btnSkins).setOnClickListener(v -> {
            startActivity(new Intent(this, SkinsActivity.class));
        });

        findViewById(R.id.btnOpenItems).setOnClickListener(v -> {
            startActivity(new Intent(this, ItemsActivity.class));
        });

    }

    /**
     * Loads user data into the UI, either from local storage or database.
     */
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

    /**
     * Fetches user data from Firebase Database.
     */
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

    /**
     * Updates the UI with the provided user data.
     */
    private void showUser(User user) {
        // ---------- GUEST ----------
        if (user == null) {
            txtName.setText("Guest");
            txtSubtitle.setText("Not logged in");
            txtGuildName.setText("No Guild");

            txtRuns.setText("Runs: 0");
            txtWinsLosses.setText("Wins: 0 | Losses: 0");
            txtWinRate.setText("Win Rate: 0%");

            txtKills.setText("Enemies Killed: 0 (0.00 / run)");
            txtPickups.setText("Pickups: 0 (0.00 / run)");
            txtCoins.setText("Coins 0");
            txtItemsPicked.setText("Items Picked: 0 (0.00 / run)");

            txtHighestWave.setText("Highest Wave: 0");
            txtBestTime.setText("Best Time: —");

            txtRangedPicks.setText("Ranged: 0 (0%)");
            txtMeleePicks.setText("Melee: 0 (0%)");

            txtCurrentStreak.setText("Current Streak: 0");
            txtBestStreak.setText("Best Streak: 0");

            txtDailyStats.setText("Completed: 0 • 🔥 Streak: 0");
            return;
        }

        // ---------- NAME ----------
        txtName.setText(user.getFullName());
        txtSubtitle.setText("Profile");
        bindGuildName(user.getGuildId());

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

        txtDailyStats.setText(
                "Completed: " + user.getDailyChallengesCompleted()
                        + " • 🔥 Streak: " + user.getDailyStreak()
                        + " (Best: " + user.getBestDailyStreak() + ")"
        );

        // ---------- CLASS PICKS ----------
        int rangedPicks = user.getPickedRanged();

        // Safety clamp (in case of bad data)
        rangedPicks = Math.max(0, Math.min(rangedPicks, runs));
        int meleePicks = Math.max(0, runs - rangedPicks);

        double rangedPercent = (runs > 0)
                ? (rangedPicks * 100.0 / runs)
                : 0.0;

        double meleePercent = (runs > 0)
                ? (meleePicks * 100.0 / runs)
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
        int itemsPicked = user.getItemsPicked();

        double killsPerRun = (runs > 0)
                ? (enemiesKilled * 1.0 / runs)
                : 0.0;

        double pickupsPerRun = (runs > 0)
                ? (pickupsPicked * 1.0 / runs)
                : 0.0;

        double itemsPickedPerRun = (runs > 0)
                ? (itemsPicked * 1.0 / runs)
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

        txtItemsPicked.setText(String.format(
                Locale.US,
                "Items Picked: %d (%.2f / run)",
                itemsPicked,
                itemsPickedPerRun
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

    /**
     * Binds the guild name to the UI based on guildId.
     */
    private void bindGuildName(String guildId) {
        if (guildId == null || guildId.trim().isEmpty()) {
            txtGuildName.setText("No Guild");
            return;
        }

        txtGuildName.setText("Guild...");

        com.google.firebase.database.FirebaseDatabase.getInstance()
                .getReference("guilds")
                .child(guildId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    Guild guild = snapshot.getValue(Guild.class);
                    String guildName = guild != null ? guild.getName() : null;

                    if (guildName == null || guildName.trim().isEmpty()) {
                        txtGuildName.setText("Unknown Guild");
                        return;
                    }

                    txtGuildName.setText(guildName.trim());
                })
                .addOnFailureListener(e -> txtGuildName.setText("Unknown Guild"));
    }

    /**
     * Formats seconds into a human-readable time string.
     */
    private static String formatTime(int seconds) {
        int minutes = seconds / 60;
        int secs = seconds % 60;

        if (minutes > 0) {
            return String.format(Locale.US, "%d:%02d", minutes, secs);
        }
        return String.format(Locale.US, "%ds", secs);
    }
}

