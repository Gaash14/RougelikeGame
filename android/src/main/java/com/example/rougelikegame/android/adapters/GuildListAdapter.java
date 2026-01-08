package com.example.rougelikegame.android.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rougelikegame.R;
import com.example.rougelikegame.android.models.meta.Guild;

import java.util.ArrayList;
import java.util.List;

public class GuildListAdapter extends RecyclerView.Adapter<GuildListAdapter.ViewHolder> {

    private final List<Guild> fullList = new ArrayList<>();
    private final List<Guild> guildList;

    public interface OnGuildClickListener {
        void onDeleteClick(Guild guild);
        void onResetStatsClick(Guild guild);
    }

    private final OnGuildClickListener onGuildClickListener;

    public GuildListAdapter(@Nullable OnGuildClickListener listener) {
        guildList = new ArrayList<>();
        this.onGuildClickListener = listener;
    }

    @NonNull
    @Override
    public GuildListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_guild_admin, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Guild guild = guildList.get(position);
        if (guild == null) return;

        holder.tvName.setText(guild.getName());

        int memberCount = guild.getMembers() != null
            ? guild.getMembers().size()
            : 0;

        holder.tvMembers.setText("Members: " + memberCount);

        holder.btnDelete.setOnClickListener(v -> {
            if (onGuildClickListener != null) {
                onGuildClickListener.onDeleteClick(guild);
            }
        });

        holder.btnReset.setOnClickListener(v -> {
            if (onGuildClickListener != null) {
                onGuildClickListener.onResetStatsClick(guild);
            }
        });
    }

    @Override
    public int getItemCount() {
        return guildList.size();
    }

    /* ---------- DATA MANAGEMENT (same pattern as UserListAdapter) ---------- */

    public void setGuildList(List<Guild> guilds) {
        guildList.clear();
        guildList.addAll(guilds);

        fullList.clear();
        fullList.addAll(guilds);

        notifyDataSetChanged();
    }

    public void removeGuild(Guild guild) {
        int index = guildList.indexOf(guild);
        if (index == -1) return;

        guildList.remove(index);
        notifyItemRemoved(index);
    }

    public void updateGuild(Guild guild) {
        int index = guildList.indexOf(guild);
        if (index == -1) return;

        guildList.set(index, guild);
        notifyItemChanged(index);
    }

    public void filter(String text) {
        guildList.clear();

        if (text == null || text.trim().isEmpty()) {
            guildList.addAll(fullList);
        } else {
            String query = text.toLowerCase().trim();
            for (Guild guild : fullList) {
                if (guild.getName() != null &&
                    guild.getName().toLowerCase().contains(query)) {
                    guildList.add(guild);
                }
            }
        }

        notifyDataSetChanged();
    }

    // view holder

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvName;
        TextView tvMembers;
        Button btnDelete;
        Button btnReset;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tv_item_guild_name);
            tvMembers = itemView.findViewById(R.id.tv_item_guild_members);
            btnDelete = itemView.findViewById(R.id.btn_item_guild_delete);
            btnReset = itemView.findViewById(R.id.btn_item_guild_reset);
        }
    }
}
