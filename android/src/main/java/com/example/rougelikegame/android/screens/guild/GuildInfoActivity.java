package com.example.rougelikegame.android.screens.guild;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rougelikegame.R;
import com.example.rougelikegame.android.adapters.GuildChatAdapter;
import com.example.rougelikegame.android.models.meta.GuildMessage;
import com.example.rougelikegame.android.models.meta.User;
import com.example.rougelikegame.android.utils.SharedPreferencesUtil;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

/**
 * Activity that displays information about a user's guild, including member count,
 * statistics (kills, wins, attempts), and a real-time guild chat.
 * It also allows users to leave or delete a guild, or join/create one if they are not in a guild.
 */
public class GuildInfoActivity extends AppCompatActivity {

    private static final int MAX_GUILD_MESSAGE_LENGTH = 300;

    private TextView txtGuildName;
    private TextView txtMemberCount;
    private TextView txtEnemiesKilled;
    private TextView txtWins;
    private TextView txtAttempts;

    private Button btnLeaveGuild;
    private Button btnDeleteGuild;

    private Button btnCreateGuild;
    private Button btnJoinGuild;

    private LinearLayout layoutGuildInfo;
    private LinearLayout layoutNoGuild;

    private RecyclerView recyclerGuildChat;
    private EditText edtGuildMessage;
    private Button btnSendGuildMessage;
    private TextView txtGuildChatStatus;
    private LinearLayout layoutGuildChatInput;

    private GuildChatAdapter guildChatAdapter;
    private ChildEventListener chatListener;
    private Query chatQuery;
    private String activeGuildId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guild_info);

        txtGuildName = findViewById(R.id.txtGuildName);
        txtMemberCount = findViewById(R.id.txtMemberCount);
        txtEnemiesKilled = findViewById(R.id.txtEnemiesKilled);
        txtWins = findViewById(R.id.txtWins);
        txtAttempts = findViewById(R.id.txtAttempts);

        btnLeaveGuild = findViewById(R.id.btnLeaveGuild);
        btnDeleteGuild = findViewById(R.id.btnDeleteGuild);

        btnCreateGuild = findViewById(R.id.btnCreateGuild);
        btnJoinGuild = findViewById(R.id.btnJoinGuild);

        layoutGuildInfo = findViewById(R.id.layoutGuildInfo);
        layoutNoGuild = findViewById(R.id.layoutNoGuild);

        recyclerGuildChat = findViewById(R.id.recyclerGuildChat);
        edtGuildMessage = findViewById(R.id.edtGuildMessage);
        btnSendGuildMessage = findViewById(R.id.btnSendGuildMessage);
        txtGuildChatStatus = findViewById(R.id.txtGuildChatStatus);
        layoutGuildChatInput = findViewById(R.id.layoutGuildChatInput);

        User user = SharedPreferencesUtil.getUser(this);
        String currentUid = user != null ? user.getUid() : null;

        guildChatAdapter = new GuildChatAdapter(currentUid);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerGuildChat.setLayoutManager(layoutManager);
        recyclerGuildChat.setAdapter(guildChatAdapter);

        btnLeaveGuild.setOnClickListener(v -> confirmLeaveGuild());
        btnDeleteGuild.setOnClickListener(v -> confirmDeleteGuild());
        btnSendGuildMessage.setOnClickListener(v -> sendGuildMessage());

        btnCreateGuild.setOnClickListener(v ->
            startActivity(new Intent(this, CreateGuildActivity.class))
        );
        btnJoinGuild.setOnClickListener(v ->
            startActivity(new Intent(this, JoinGuildActivity.class))
        );

        refreshGuildState();
    }

    /**
     * Loads guild details from Firebase and updates the UI.
     *
     * @param guildId The ID of the guild to load.
     */
    private void loadGuild(String guildId) {
        FirebaseDatabase.getInstance()
            .getReference("guilds")
            .child(guildId)
            .addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                @SuppressWarnings("unchecked")
                public void onDataChange(DataSnapshot snapshot) {
                    if (!snapshot.exists()) {
                        Toast.makeText(
                            GuildInfoActivity.this,
                            "Guild not found",
                            Toast.LENGTH_SHORT
                        ).show();
                        finish();
                        return;
                    }

                    String name = snapshot.child("name").getValue(String.class);
                    txtGuildName.setText(name != null ? name : "Unknown");

                    Map<String, Boolean> members =
                        (Map<String, Boolean>) snapshot.child("members").getValue();

                    int memberCount = members != null ? members.size() : 0;
                    txtMemberCount.setText("Members: " + memberCount);

                    Integer kills =
                        snapshot.child("totalEnemiesKilled").getValue(Integer.class);
                    Integer wins =
                        snapshot.child("totalWins").getValue(Integer.class);
                    Integer attempts =
                        snapshot.child("totalAttempts").getValue(Integer.class);

                    txtEnemiesKilled.setText(
                        "Enemies Killed: " + (kills != null ? kills : 0)
                    );
                    txtWins.setText(
                        "Wins: " + (wins != null ? wins : 0)
                    );
                    txtAttempts.setText(
                        "Attempts: " + (attempts != null ? attempts : 0)
                    );

                    String ownerUid = snapshot.child("ownerUid").getValue(String.class);

                    User user = SharedPreferencesUtil.getUser(GuildInfoActivity.this);
                    if (user != null && ownerUid != null && ownerUid.equals(user.getUid())) {
                        btnDeleteGuild.setVisibility(View.VISIBLE);
                    } else {
                        btnDeleteGuild.setVisibility(View.GONE);
                    }

                    verifyMembershipAndInitChat(guildId);
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(
                        GuildInfoActivity.this,
                        "Failed to load guild",
                        Toast.LENGTH_SHORT
                    ).show();
                    disableGuildChat("Chat unavailable right now");
                }
            });
    }

    /**
     * Verifies if the current user is still a member of the guild before enabling chat.
     *
     * @param guildId The ID of the guild to verify membership for.
     */
    private void verifyMembershipAndInitChat(String guildId) {
        User user = SharedPreferencesUtil.getUser(this);
        if (user == null || TextUtils.isEmpty(user.getUid())) {
            disableGuildChat("Login required for guild chat");
            return;
        }

        String uid = user.getUid();
        FirebaseDatabase.getInstance()
            .getReference("guilds")
            .child(guildId)
            .child("members")
            .child(uid)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Boolean isMember = snapshot.getValue(Boolean.class);
                    if (!Boolean.TRUE.equals(isMember)) {
                        disableGuildChat("Guild chat is only available to members");
                        return;
                    }

                    enableGuildChat();
                    startGuildChatListener(guildId);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    disableGuildChat("Failed to verify chat access");
                }
            });
    }

    /**
     * Enables the guild chat UI elements.
     */
    private void enableGuildChat() {
        txtGuildChatStatus.setVisibility(View.GONE);
        layoutGuildChatInput.setVisibility(View.VISIBLE);
        edtGuildMessage.setEnabled(true);
        btnSendGuildMessage.setEnabled(true);
    }

    /**
     * Disables the guild chat UI elements and displays a reason.
     *
     * @param reason The reason why the chat is disabled.
     */
    private void disableGuildChat(String reason) {
        stopGuildChatListener();
        guildChatAdapter.setMessages(new java.util.ArrayList<>());
        txtGuildChatStatus.setText(reason);
        txtGuildChatStatus.setVisibility(View.VISIBLE);
        layoutGuildChatInput.setVisibility(View.GONE);
        edtGuildMessage.setText("");
        edtGuildMessage.setEnabled(false);
        btnSendGuildMessage.setEnabled(false);
    }

    /**
     * Starts listening for new messages in the guild chat.
     *
     * @param guildId The ID of the guild whose chat to listen to.
     */
    private void startGuildChatListener(String guildId) {
        stopGuildChatListener();
        activeGuildId = guildId;

        DatabaseReference chatRef = FirebaseDatabase.getInstance()
            .getReference("guilds")
            .child(guildId)
            .child("chat");

        chatQuery = chatRef.orderByChild("timestamp").limitToLast(200);
        chatListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                GuildMessage message = snapshot.getValue(GuildMessage.class);
                if (message == null || TextUtils.isEmpty(message.getText())) {
                    return;
                }

                boolean shouldAutoScroll = isNearBottom();

                guildChatAdapter.addMessage(message);

                if (shouldAutoScroll) {
                    recyclerGuildChat.scrollToPosition(guildChatAdapter.getLastPosition());
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {}

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {}

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {}

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(GuildInfoActivity.this, "Chat listener disconnected", Toast.LENGTH_SHORT).show();
            }
        };

        chatQuery.addChildEventListener(chatListener);
    }

    /**
     * Checks if the chat RecyclerView is scrolled near the bottom.
     *
     * @return True if scrolled near the bottom, false otherwise.
     */
    private boolean isNearBottom() {
        RecyclerView.LayoutManager manager = recyclerGuildChat.getLayoutManager();
        if (!(manager instanceof LinearLayoutManager)) {
            return true;
        }

        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) manager;
        int lastVisible = linearLayoutManager.findLastCompletelyVisibleItemPosition();
        int itemCount = guildChatAdapter.getItemCount();

        return itemCount <= 1 || lastVisible >= itemCount - 3;
    }

    /**
     * Sends a message to the guild chat.
     */
    private void sendGuildMessage() {
        User user = SharedPreferencesUtil.getUser(this);
        if (user == null || TextUtils.isEmpty(user.getUid()) || TextUtils.isEmpty(activeGuildId)) {
            Toast.makeText(this, "Unable to send message", Toast.LENGTH_SHORT).show();
            return;
        }

        String rawMessage = edtGuildMessage.getText() != null
            ? edtGuildMessage.getText().toString()
            : "";

        String trimmedMessage = rawMessage.trim();
        if (trimmedMessage.isEmpty()) {
            return;
        }

        if (trimmedMessage.length() > MAX_GUILD_MESSAGE_LENGTH) {
            trimmedMessage = trimmedMessage.substring(0, MAX_GUILD_MESSAGE_LENGTH);
        }

        String senderName = user.getFullName();
        if (TextUtils.isEmpty(senderName) || "null null".equalsIgnoreCase(senderName)) {
            senderName = user.getEmail();
        }
        if (TextUtils.isEmpty(senderName)) {
            senderName = "Unknown";
        }

        GuildMessage newMessage = new GuildMessage(
            user.getUid(),
            senderName,
            trimmedMessage,
            System.currentTimeMillis()
        );

        DatabaseReference messageRef = FirebaseDatabase.getInstance()
            .getReference("guilds")
            .child(activeGuildId)
            .child("chat")
            .push();

        String finalTrimmedMessage = trimmedMessage;
        messageRef.setValue(newMessage).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                edtGuildMessage.setText("");
            } else {
                Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show();
                edtGuildMessage.setText(finalTrimmedMessage);
                edtGuildMessage.setSelection(edtGuildMessage.length());
            }
        });
    }

    /**
     * Performs the logic to leave the current guild.
     */
    private void leaveGuild() {
        User user = SharedPreferencesUtil.getUser(this);
        if (user == null || user.getGuildId() == null) return;

        String guildId = user.getGuildId();
        String uid = user.getUid();

        FirebaseDatabase.getInstance()
            .getReference("guilds")
            .child(guildId)
            .addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    String ownerUid = snapshot.child("ownerUid").getValue(String.class);

                    // Remove user from members
                    FirebaseDatabase.getInstance()
                        .getReference("guilds")
                        .child(guildId)
                        .child("members")
                        .child(uid)
                        .removeValue();

                    // Remove guildId from user
                    FirebaseDatabase.getInstance()
                        .getReference("users")
                        .child(uid)
                        .child("guildId")
                        .removeValue();

                    // If owner is leaving --> transfer ownership
                    if (uid.equals(ownerUid)) {
                        boolean transferred = false;

                        for (DataSnapshot member : snapshot.child("members").getChildren()) {
                            String newOwnerUid = member.getKey();

                            if (!uid.equals(newOwnerUid)) {
                                FirebaseDatabase.getInstance()
                                    .getReference("guilds")
                                    .child(guildId)
                                    .child("ownerUid")
                                    .setValue(newOwnerUid);

                                transferred = true;
                                break;
                            }
                        }

                        // if owner is the last member --> delete guild
                        if (!transferred) {
                            FirebaseDatabase.getInstance()
                                .getReference("guilds")
                                .child(guildId)
                                .removeValue();
                        }
                    }


                    // Update local user
                    user.setGuildId(null);
                    SharedPreferencesUtil.saveUser(GuildInfoActivity.this, user);

                    stopGuildChatListener();
                    layoutGuildInfo.setVisibility(View.GONE);
                    layoutNoGuild.setVisibility(View.VISIBLE);
                    txtGuildName.setText("Guild");
                }

                @Override
                public void onCancelled(DatabaseError error) {}
            });
    }

    /**
     * Displays a confirmation dialog before deleting the guild.
     */
    private void confirmDeleteGuild() {
        new AlertDialog.Builder(this)
            .setTitle("Delete Guild")
            .setMessage("This will permanently delete the guild for all members. Continue?")
            .setPositiveButton("Delete", (d, w) -> deleteGuild())
            .setNegativeButton("Cancel", null)
            .show();
    }

    /**
     * Displays a confirmation dialog before leaving the guild.
     */
    private void confirmLeaveGuild() {
        User user = SharedPreferencesUtil.getUser(this);
        if (user == null || user.getGuildId() == null) {
            return;
        }

        new AlertDialog.Builder(this)
            .setTitle("Leave Guild")
            .setMessage("Are you sure you want to leave this guild?")
            .setPositiveButton("Leave", (d, w) -> leaveGuild())
            .setNegativeButton("Cancel", null)
            .show();
    }

    /**
     * Performs the logic to delete the current guild for all members.
     */
    private void deleteGuild() {
        User user = SharedPreferencesUtil.getUser(this);
        if (user == null || user.getGuildId() == null) return;

        String guildId = user.getGuildId();

        FirebaseDatabase.getInstance()
            .getReference("guilds")
            .child(guildId)
            .addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    // Remove guildId from all members
                    for (DataSnapshot member : snapshot.child("members").getChildren()) {
                        FirebaseDatabase.getInstance()
                            .getReference("users")
                            .child(member.getKey())
                            .child("guildId")
                            .removeValue();
                    }

                    // Delete the guild
                    FirebaseDatabase.getInstance()
                        .getReference("guilds")
                        .child(guildId)
                        .removeValue();

                    // Update local user
                    user.setGuildId(null);
                    SharedPreferencesUtil.saveUser(GuildInfoActivity.this, user);

                    stopGuildChatListener();
                    layoutGuildInfo.setVisibility(View.GONE);
                    layoutNoGuild.setVisibility(View.VISIBLE);
                }

                @Override
                public void onCancelled(DatabaseError error) {}
            });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshGuildState();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopGuildChatListener();
    }

    /**
     * Stops the real-time guild chat listener and cleans up references.
     */
    private void stopGuildChatListener() {
        if (chatQuery != null && chatListener != null) {
            chatQuery.removeEventListener(chatListener);
        }

        chatListener = null;
        chatQuery = null;
        activeGuildId = null;
    }

    /**
     * Refreshes the guild state based on the user's guild membership.
     */
    private void refreshGuildState() {
        User user = SharedPreferencesUtil.getUser(this);

        if (user == null || user.getGuildId() == null) {
            stopGuildChatListener();
            layoutGuildInfo.setVisibility(View.GONE);
            layoutNoGuild.setVisibility(View.VISIBLE);

            txtGuildName.setText("Guild");

            return;
        }

        layoutNoGuild.setVisibility(View.GONE);
        layoutGuildInfo.setVisibility(View.VISIBLE);
        loadGuild(user.getGuildId());
    }
}
