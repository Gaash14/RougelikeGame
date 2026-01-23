package com.example.rougelikegame.android.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.rougelikegame.R;
import com.example.rougelikegame.android.models.meta.User;

import java.util.List;

public class LeaderboardAdapter extends ArrayAdapter<User> {

    private final String currentUserUid;

    public LeaderboardAdapter(
        @NonNull Context context,
        @NonNull List<User> users,
        String currentUserUid
    ) {
        super(context, 0, users);
        this.currentUserUid = currentUserUid;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                .inflate(R.layout.item_leaderboard, parent, false);
        }

        User user = getItem(position);
        if (user == null) return convertView;

        LinearLayout root = convertView.findViewById(R.id.rootItem);
        TextView tvName = convertView.findViewById(R.id.tvName);
        TextView tvStats = convertView.findViewById(R.id.tvStats);

        int rank = position + 1;

        // 🥇🥈🥉 medals
        String medal = "";
        if (rank == 1) medal = "🥇 ";
        else if (rank == 2) medal = "🥈 ";
        else if (rank == 3) medal = "🥉 ";

        String name = user.getFullName();
        if (name == null || name.trim().isEmpty()) {
            name = user.getEmail();
        }

        boolean isCurrentUser =
            currentUserUid != null &&
                currentUserUid.equals(user.getUid());

        if (isCurrentUser) {
            root.setBackgroundColor(0xFF3A3A3A);
            tvName.setText(medal + name + "  • YOU");
        } else {
            root.setBackgroundColor(0xFF2A2A2A);
            tvName.setText(medal + name);
        }

        String stats = "Wave " + user.getHighestWave();

        if (user.getBestTime() > 0 && user.getBestTime() < 999999) {
            stats += "  •  " + formatTime(user.getBestTime());
        }

        tvStats.setText(stats);

        return convertView;
    }

    private String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}
