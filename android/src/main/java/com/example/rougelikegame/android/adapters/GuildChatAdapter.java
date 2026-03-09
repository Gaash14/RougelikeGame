package com.example.rougelikegame.android.adapters;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rougelikegame.R;
import com.example.rougelikegame.android.models.meta.GuildMessage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GuildChatAdapter extends RecyclerView.Adapter<GuildChatAdapter.ViewHolder> {

    private final List<GuildMessage> messages = new ArrayList<>();
    private final String currentUid;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public GuildChatAdapter(String currentUid) {
        this.currentUid = currentUid;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_guild_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GuildMessage message = messages.get(position);
        if (message == null) return;

        String senderName = message.getSenderName();
        holder.txtSender.setText(senderName != null ? senderName : "Unknown");

        String text = message.getText();
        holder.txtMessage.setText(text != null ? text : "");

        long timestamp = message.getTimestamp();
        holder.txtTimestamp.setText(timestamp > 0 ? timeFormat.format(new Date(timestamp)) : "--:--");

        boolean isMine = currentUid != null && currentUid.equals(message.getSenderId());

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.container.getLayoutParams();
        params.gravity = isMine ? Gravity.END : Gravity.START;
        holder.container.setLayoutParams(params);

        holder.txtMessage.setBackgroundColor(isMine ? 0xFF2E7D32 : 0x33222222);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void setMessages(List<GuildMessage> newMessages) {
        messages.clear();
        messages.addAll(newMessages);
        notifyDataSetChanged();
    }

    public void addMessage(GuildMessage message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    public int getLastPosition() {
        return messages.size() - 1;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout container;
        TextView txtSender;
        TextView txtMessage;
        TextView txtTimestamp;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.layoutMessageContainer);
            txtSender = itemView.findViewById(R.id.txtMessageSender);
            txtMessage = itemView.findViewById(R.id.txtMessageText);
            txtTimestamp = itemView.findViewById(R.id.txtMessageTimestamp);
        }
    }
}
