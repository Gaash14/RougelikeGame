package com.example.rougelikegame.android.screens.profile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rougelikegame.R;
import com.example.rougelikegame.android.adapters.SkinAdapter;
import com.example.rougelikegame.android.models.meta.Skin;
import com.example.rougelikegame.android.models.meta.User;
import com.example.rougelikegame.android.services.DatabaseService;
import com.example.rougelikegame.android.utils.SharedPreferencesUtil;
import com.example.rougelikegame.android.utils.SkinRegistry;

import java.util.List;

/**
 * Activity for displaying and equipping player skins.
 */
public class SkinsActivity extends AppCompatActivity {

    private RecyclerView recyclerSkins;
    private User user;

    private TextView txtSkinsProgress;
    private Button btnGoToShop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skins);

        recyclerSkins = findViewById(R.id.recyclerSkins);
        recyclerSkins.setLayoutManager(new LinearLayoutManager(this));

        txtSkinsProgress = findViewById(R.id.txtSkinsProgress);
        btnGoToShop = findViewById(R.id.btnGoToShop);

        btnGoToShop.setOnClickListener(v -> {
            startActivity(new Intent(this, com.example.rougelikegame.android.screens.shop.ShopActivity.class));
            finish();
        });

        user = SharedPreferencesUtil.getUser(this);
        if (user == null) {
            return;
        }

        loadSkins();
    }

    /**
     * Loads the list of skins and sets up the adapter for the RecyclerView.
     */
    private void loadSkins() {
        List<Skin> allSkins = SkinRegistry.getAllSkins();

        updateSkinsProgress(allSkins);

        SkinAdapter adapter = new SkinAdapter(
                allSkins,
                user,
                false,
                true,
                new SkinAdapter.SkinActionListener() {

                    @Override
                    public void onBuy(Skin skin) {
                        // Buying is NOT allowed here
                    }

                    @Override
                    public void onEquip(Skin skin) {
                        user.setEquippedSkinId(skin.getId());

                        SharedPreferencesUtil.saveUser(SkinsActivity.this, user);
                        DatabaseService.getInstance()
                                .setEquippedSkin(user.getUid(), skin.getId());

                        if (recyclerSkins.getAdapter() != null) {
                            recyclerSkins.getAdapter().notifyDataSetChanged();
                        }
                    }
                }
        );

        recyclerSkins.setAdapter(adapter);
    }

    /**
     * Updates the progress text showing how many skins are unlocked.
     *
     * @param skins List of all skins to check progress against.
     */
    private void updateSkinsProgress(List<Skin> skins) {
        int unlocked = 0;

        for (Skin skin : skins) {
            if (user.hasSkinUnlocked(skin.getId())) {
                unlocked++;
            }
        }

        int total = skins.size();
        txtSkinsProgress.setText("Unlocked: " + unlocked + " / " + total);
    }
}

