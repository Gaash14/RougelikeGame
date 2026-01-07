package com.example.rougelikegame.android.screens.guilds;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rougelikegame.R;
import com.example.rougelikegame.android.utils.SharedPreferencesUtil;
import com.example.rougelikegame.android.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class JoinGuildActivity extends AppCompatActivity {

    private ListView guildListView;
    private ArrayAdapter<String> adapter;

    private final List<String> guildNames = new ArrayList<>();
    private final List<String> guildIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_guild);

        guildListView = findViewById(R.id.guildListView);

        adapter = new ArrayAdapter<String>(
            this,
            R.layout.item_leaderboard,
            R.id.tvItem,
            guildNames
        );
        guildListView.setAdapter(adapter);

        loadGuilds();

        guildListView.setOnItemClickListener((parent, view, position, id) -> {
            joinGuild(guildIds.get(position));
        });
    }

    private void loadGuilds() {
        FirebaseDatabase.getInstance()
            .getReference("guilds")
            .addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    guildNames.clear();
                    guildIds.clear();

                    for (DataSnapshot guildSnap : snapshot.getChildren()) {
                        String guildId = guildSnap.getKey();
                        String name = guildSnap.child("name").getValue(String.class);

                        if (guildId != null && name != null) {
                            guildIds.add(guildId);
                            guildNames.add(name);
                        }
                    }

                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(
                        JoinGuildActivity.this,
                        "Failed to load guilds",
                        Toast.LENGTH_SHORT
                    ).show();
                }
            });
    }

    private void joinGuild(String guildId) {
        User user = SharedPreferencesUtil.getUser(this);
        if (user == null) return;

        if (user.getGuildId() != null) {
            Toast.makeText(this, "You are already in a guild", Toast.LENGTH_SHORT).show();
            return;
        }

        // update user locally
        user.setGuildId(guildId);
        SharedPreferencesUtil.saveUser(this, user);

        // update Firebase (user side)
        FirebaseDatabase.getInstance()
            .getReference("users")
            .child(user.getUid())
            .child("guildId")
            .setValue(guildId);

        // add user to guild members
        FirebaseDatabase.getInstance()
            .getReference("guilds")
            .child(guildId)
            .child("members")
            .child(user.getUid())
            .setValue(true);

        Toast.makeText(this, "Joined guild!", Toast.LENGTH_SHORT).show();
        finish();
    }
}
