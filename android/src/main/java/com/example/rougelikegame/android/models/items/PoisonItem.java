package com.example.rougelikegame.android.models.items;

import com.example.rougelikegame.android.models.characters.Enemy;
import com.example.rougelikegame.android.models.characters.Player;

public class PoisonItem implements PassiveItem {

    public static final int ID = 2;
    private static final float DURATION = 2f;

    @Override public int getItemId() { return ID; }
    @Override public String getKey() { return "poison"; }
    @Override public String getDisplayName() { return "Poison"; }

    @Override
    public void onHitEnemy(Player player, Enemy enemy) {
        enemy.applyPoison(DURATION);
    }
}
