package com.example.rougelikegame.android.screens.menu;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.rougelikegame.R;
import com.example.rougelikegame.android.managers.AchievementManager;
import com.example.rougelikegame.android.models.meta.User;
import com.example.rougelikegame.android.screens.admin.AdminActivity;
import com.example.rougelikegame.android.screens.guild.GuildInfoActivity;
import com.example.rougelikegame.android.screens.leaderboard.LeaderboardActivity;
import com.example.rougelikegame.android.screens.profile.ProfileActivity;
import com.example.rougelikegame.android.services.ReminderReceiver;
import com.example.rougelikegame.android.utils.SharedPreferencesUtil;

import java.util.Calendar;

/**
 * Main menu activity providing access to all game features.
 */
public class MainMenuActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        checkPermissionsAndScheduleAlarm();

        Button startGame = findViewById(R.id.startGameButton);
        Button leaderboard = findViewById(R.id.leaderboardButton);
        Button guilds = findViewById(R.id.guildsButton);
        Button profile = findViewById(R.id.profileButton);
        Button itemCodex = findViewById(R.id.itemsCodexButton);
        Button adminPanel = findViewById(R.id.adminPanelButton);

        User currentUser = SharedPreferencesUtil.getUser(this);

        if (currentUser != null && currentUser.isAdmin()) {
            adminPanel.setVisibility(View.VISIBLE);

            adminPanel.setOnClickListener(v -> {
                Intent intent = new Intent(MainMenuActivity.this, AdminActivity.class);
                startActivity(intent);
            });
        } else {
            adminPanel.setVisibility(View.GONE);
        }

        if (currentUser != null) {
            AchievementManager manager = AchievementManager.getInstance();

            // clear previous user state (singleton!)
            manager.reset();

            // set active user
            manager.setUserUid(currentUser.getUid());
            manager.setContext(MainMenuActivity.this);

            // load achievements from Firebase
            if (currentUser.getAchievements() != null) {
                currentUser.getAchievements().forEach((id, unlocked) -> {
                    if (Boolean.TRUE.equals(unlocked)) {
                        manager.markUnlockedFromDatabase(id);
                    }
                });
            }
        }

        startGame.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChooseDifficultyActivity.class);
            startActivity(intent);
        });

        leaderboard.setOnClickListener(v -> {
            Intent intent = new Intent(this, LeaderboardActivity.class);
            startActivity(intent);
        });

        guilds.setOnClickListener(v -> {
            Intent intent = new Intent(this, GuildInfoActivity.class);
            startActivity(intent);
        });

        profile.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
        });

        itemCodex.setOnClickListener(v -> {
            Intent intent = new Intent(this, ItemsActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Checks for notification permissions and schedules a daily alarm if granted.
     */
    private void checkPermissionsAndScheduleAlarm() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        PERMISSION_REQUEST_CODE
                );
            } else {
                scheduleDailyReminder(this);
            }
        } else {
            scheduleDailyReminder(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                scheduleDailyReminder(this);
            }
        }
    }

    /**
     * Schedules a daily notification reminder.
     *
     * @param context Application context.
     */
    public static void scheduleDailyReminder(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }

        Intent intent = new Intent(context, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 14);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                pendingIntent
        );
    }
}

