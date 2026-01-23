package com.example.rougelikegame.android.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.rougelikegame.R;
import com.example.rougelikegame.android.models.meta.DailyRun;

import java.util.List;
import java.util.Locale;

public class DailyLeaderboardAdapter extends BaseAdapter {

    private final Context context;
    private final List<DailyRun> runs;
    private final String currentUserUid;

    public DailyLeaderboardAdapter(
        Context context,
        List<DailyRun> runs,
        String currentUserUid
    ) {
        this.context = context;
        this.runs = runs;
        this.currentUserUid = currentUserUid;
    }

    @Override
    public int getCount() {
        return runs.size();
    }

    @Override
    public Object getItem(int position) {
        return runs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                .inflate(R.layout.item_daily_leaderboard, parent, false);
        }

        DailyRun run = runs.get(position);

        // ---------- HIGHLIGHTING ----------
        int rank = position + 1;


        // 🥇🥈🥉 medals (emoji only)
        String medal = "";
        if (rank == 1) medal = "🥇 ";
        else if (rank == 2) medal = "🥈 ";
        else if (rank == 3) medal = "🥉 ";

        TextView txtRank = convertView.findViewById(R.id.txtRank);
        TextView txtName = convertView.findViewById(R.id.txtName);
        TextView txtStats = convertView.findViewById(R.id.txtStats);

        txtRank.setText("#" + (position + 1));
        txtName.setText(medal + run.name);

        txtStats.setText(
            "Wave " + run.wave +
                " • " + formatTime(run.time) +
                " • " + run.playerClass
        );

        // reset background
        convertView.setBackgroundColor(0xFF2A2A2A);

        // ⭐ current user outline
        if (currentUserUid != null && currentUserUid.equals(run.uid)) {
            convertView.setBackgroundResource(R.drawable.bg_you_outline);
            txtName.setText(txtName.getText() + "  • YOU");
        }

        return convertView;
    }

    private String formatTime(int seconds) {
        int m = seconds / 60;
        int s = seconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", m, s);
    }
}
