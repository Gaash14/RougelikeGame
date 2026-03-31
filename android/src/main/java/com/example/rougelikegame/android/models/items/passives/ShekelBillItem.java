package com.example.rougelikegame.android.models.items.passives;

import com.example.rougelikegame.android.managers.AchievementManager;
import com.example.rougelikegame.android.models.characters.Player;
import com.example.rougelikegame.android.models.items.ItemTier;
import com.example.rougelikegame.android.models.items.PassiveItem;

/**
 * Item that grants a significant amount of coins instantly.
 */
public class ShekelBillItem implements PassiveItem {

    public static final int ID = 7;
    private static final int COIN_BONUS = 20;
    private final AchievementManager achievementManager = AchievementManager.getInstance();

    @Override
    public int getItemId() {
        return ID;
    }

    @Override
    public String getKey() {
        return "shekel_bill";
    }

    @Override
    public String getDisplayName() {
        return "20 Shekel Bill";
    }

    @Override
    public String getDescription() {
        return "Instantly grants 20 bonus coins on pickup.";
    }

    @Override
    public String getIconPath() {
        return "items/shekel_bill.png";
    }

    @Override
    public ItemTier getTier() {
        return ItemTier.C;
    }

    @Override
    public void onPickup(Player player) {
        player.addCoins(COIN_BONUS);
        if (player.coins >= 25) {
            achievementManager.unlock("coins_25");
        }
    }
}
