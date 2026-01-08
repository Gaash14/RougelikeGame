package com.example.rougelikegame.android.screens.shop;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rougelikegame.R;
import com.example.rougelikegame.android.adapters.SkinAdapter;
import com.example.rougelikegame.android.services.DatabaseService;
import com.example.rougelikegame.android.utils.SharedPreferencesUtil;
import com.example.rougelikegame.android.models.meta.Skin;
import com.example.rougelikegame.android.models.meta.User;

import java.util.ArrayList;
import java.util.List;

public class ShopActivity extends AppCompatActivity {

    private TextView txtCoins;
    private RecyclerView recyclerSkins;

    private User user;
    private List<Skin> skins;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);

        txtCoins = findViewById(R.id.txtCoins);
        recyclerSkins = findViewById(R.id.recyclerSkins);

        recyclerSkins.setLayoutManager(new LinearLayoutManager(this));

        user = SharedPreferencesUtil.getUser(this);
        if (user == null) return;

        updateCoinsUI();

        loadSkins();
    }

    private void updateCoinsUI() {
        txtCoins.setText("Coins: " + user.getNumOfCoins());
    }

    private void loadSkins() {
        skins = new ArrayList<>();

        skins.add(new Skin("default", "Default", 0));
        skins.add(new Skin("red_knight", "Red Knight", 50));
        skins.add(new Skin("shadow", "Shadow", 100));

        SkinAdapter adapter = new SkinAdapter(skins, user, new SkinAdapter.SkinActionListener() {
            @Override
            public void onBuy(Skin skin) {
                if (user.getNumOfCoins() < skin.getPrice()) return;

                user.setNumOfCoins(user.getNumOfCoins() - skin.getPrice());
                user.getOwnedSkins().put(skin.getId(), true);

                SharedPreferencesUtil.saveUser(ShopActivity.this, user);
                DatabaseService.getInstance().createNewUser(user, null);

                updateCoinsUI();
                recyclerSkins.getAdapter().notifyDataSetChanged();
            }

            @Override
            public void onEquip(Skin skin) {
                user.setEquippedSkinId(skin.getId());

                SharedPreferencesUtil.saveUser(ShopActivity.this, user);
                DatabaseService.getInstance().createNewUser(user, null);
                recyclerSkins.getAdapter().notifyDataSetChanged();
            }
        });

        recyclerSkins.setAdapter(adapter);
    }
}
