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
import com.example.rougelikegame.android.models.meta.User;

import java.util.ArrayList;
import java.util.List;

/**
 * UserListAdapter manages the display and administration of users in a list.
 * It provides functionality for filtering users and performing administrative actions like deleting or resetting stats.
 */
public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.ViewHolder> {

    private final List<User> fullList = new ArrayList<>();
    private final List<User> userList;
    private final OnUserClickListener onUserClickListener;

    /**
     * Interface for handling click events on user list items.
     */
    public interface OnUserClickListener {
        void onUserClick(User user);
        void onLongUserClick(User user);
        void onDeleteClick(User user);
        void onResetStatsClick(User user);
    }

    /**
     * Constructs a new UserListAdapter.
     *
     * @param onUserClickListener the listener for user action events
     */
    public UserListAdapter(@Nullable final OnUserClickListener onUserClickListener) {
        userList = new ArrayList<>();
        this.onUserClickListener = onUserClickListener;
    }

    @NonNull
    @Override
    public UserListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_user_admin, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = userList.get(position);
        if (user == null) return;

        holder.tvName.setText(user.getFullName());
        holder.tvEmail.setText(user.getEmail());

        holder.itemView.setOnClickListener(v -> {
            if (onUserClickListener != null) {
                onUserClickListener.onUserClick(user);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (onUserClickListener != null) {
                onUserClickListener.onLongUserClick(user);
            }
            return true;
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (onUserClickListener != null) {
                onUserClickListener.onDeleteClick(user);
            }
        });

        holder.btnReset.setOnClickListener(v -> {
            if (onUserClickListener != null) {
                onUserClickListener.onResetStatsClick(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    /**
     * Sets the initial list of users and updates the adapter.
     *
     * @param users the list of users to display
     */
    public void setUserList(List<User> users) {
        userList.clear();
        userList.addAll(users);

        fullList.clear();
        fullList.addAll(users);

        notifyDataSetChanged();
    }

    /**
     * Adds a single user to the end of the list.
     *
     * @param user the user to add
     */
    public void addUser(User user) {
        userList.add(user);
        notifyItemInserted(userList.size() - 1);
    }

    /**
     * Updates an existing user entry in the list.
     *
     * @param user the user to update
     */
    public void updateUser(User user) {
        int index = userList.indexOf(user);
        if (index == -1) return;
        userList.set(index, user);
        notifyItemChanged(index);
    }

    /**
     * Removes a user from the displayed list.
     *
     * @param user the user to remove
     */
    public void removeUser(User user) {
        int index = userList.indexOf(user);
        if (index == -1) return;
        userList.remove(index);
        notifyItemRemoved(index);
    }

    /**
     * Filters the user list based on a search query.
     *
     * @param text the query text to filter by
     */
    public void filter(String text) {
        userList.clear();

        if (text == null || text.trim().isEmpty()) {
            userList.addAll(fullList);
        } else {
            String query = text.toLowerCase().trim();
            for (User user : fullList) {
                if (user.getFullName() != null &&
                    user.getFullName().toLowerCase().contains(query)) {
                    userList.add(user);
                }
            }
        }

        notifyDataSetChanged();
    }

    /**
     * ViewHolder class for user list items.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvEmail;
        Button btnDelete;
        Button btnReset;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_item_user_name);
            tvEmail = itemView.findViewById(R.id.tv_item_user_email);
            btnDelete = itemView.findViewById(R.id.btn_item_user_delete);
            btnReset = itemView.findViewById(R.id.btn_item_user_reset);
        }
    }
}
