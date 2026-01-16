package com.example.rougelikegame.android.screens.menu;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rougelikegame.R;
import com.example.rougelikegame.android.managers.AchievementManager;
import com.example.rougelikegame.android.models.meta.Achievement;

public class AchievementsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievements);

        LinearLayout container = findViewById(R.id.achievementsContainer);

        for (Achievement achievement :
            AchievementManager.getInstance().getAllAchievements()) {

            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(24, 24, 24, 24);

            LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                );
            cardParams.setMargins(0, 0, 0, 16);
            card.setLayoutParams(cardParams);

        // Background
            card.setBackgroundColor(
                achievement.isUnlocked() ? 0xFF1E1E1E : 0xFF161616
            );

            // Title
            TextView title = new TextView(this);
            title.setText(
                (achievement.isUnlocked() ? "🏆 " : "🔒 ") +
                    achievement.getTitle()
            );
            title.setTextSize(18);
            title.setTextColor(0xFFFFFFFF);
            title.setPadding(0, 0, 0, 8);

            // Description
            TextView desc = new TextView(this);
            desc.setText(achievement.getDescription());
            desc.setTextSize(14);
            desc.setTextColor(0xFFBBBBBB);

            // Locked = dim
            if (!achievement.isUnlocked()) {
                card.setAlpha(0.5f);
            }

            card.addView(title);
            card.addView(desc);
            container.addView(card);
        }
    }
}
