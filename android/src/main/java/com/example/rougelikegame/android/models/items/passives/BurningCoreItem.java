package com.example.rougelikegame.android.models.items.passives;

import com.example.rougelikegame.android.models.characters.Enemy;
import com.example.rougelikegame.android.models.characters.Player;
import com.example.rougelikegame.android.models.items.ItemTier;
import com.example.rougelikegame.android.models.items.PassiveItem;

public class BurningCoreItem implements PassiveItem {

    public static final int ID = 6;
    private static final float BURN_CHANCE = 0.25f;
    private static final float BURN_DURATION_SECONDS = 4f;

    @Override
    public int getItemId() {
        return ID;
    }

    @Override
    public String getKey() {
        return "burning_core";
    }

    @Override
    public String getDisplayName() {
        return "Burning Core";
    }

    @Override
    public void onHitEnemy(Player player, Enemy enemy) {
        if (player.getRandomSource().nextFloat() < BURN_CHANCE) {
            enemy.applyBurn(BURN_DURATION_SECONDS);
        }
    }

    @Override
    public String getDescription() {
        return "Hits have a chance to ignite enemies over time.";
    }

    @Override
    public String getIconPath() {
        return "items/burning_core.png";
    }

    @Override
    public ItemTier getTier() {
        return ItemTier.B;
    }
}
