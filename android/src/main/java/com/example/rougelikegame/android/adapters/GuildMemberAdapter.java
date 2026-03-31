package com.example.rougelikegame.android.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rougelikegame.R;
import com.example.rougelikegame.android.models.meta.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying a list of guild members.
 */
public class GuildMemberAdapter extends RecyclerView.Adapter<GuildMemberAdapter.ViewHolder> {

    private final List<User> memberList = new ArrayList<>();
    private String ownerUid;

    /**
     * Sets the members to display in the list.
     *
     * @param members the list of members to display
     */
    public void setMembers(List<User> members) {
        memberList.clear();
        if (members != null) {
            memberList.addAll(members);
        }
        notifyDataSetChanged();
    }

    /**
     * Sets the UID of the guild owner for highlighting.
     *
     * @param ownerUid the UID of the guild owner
     */
    public void setOwnerUid(String ownerUid) {
        this.ownerUid = ownerUid;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_guild_member, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = memberList.get(position);
        if (user == null) return;

        String name = user.getFullName();
        if (name == null || name.trim().isEmpty()) {
            name = user.getEmail();
        }
        holder.tvMemberName.setText(name);

        boolean isOwner = ownerUid != null && ownerUid.equals(user.getUid());
        holder.tvMemberRole.setText(isOwner ? "Owner" : "Member");
        holder.tvMemberRole.setBackgroundColor(isOwner ? 0xFFFF8C00 : 0xFF555555); // Dark Orange for owner, Dark Gray for member

        String stats = "Kills: " + user.getEnemiesKilled() + " • Wins: " + user.getNumOfWins();
        holder.tvMemberStats.setText(stats);
    }

    @Override
    public int getItemCount() {
        return memberList.size();
    }

    /**
     * ViewHolder for guild member list items.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMemberName;
        TextView tvMemberRole;
        TextView tvMemberStats;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMemberName = itemView.findViewById(R.id.tvMemberName);
            tvMemberRole = itemView.findViewById(R.id.tvMemberRole);
            tvMemberStats = itemView.findViewById(R.id.tvMemberStats);
        }
    }
}
