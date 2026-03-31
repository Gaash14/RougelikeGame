package com.example.rougelikegame.android.models.items.passives;

import com.example.rougelikegame.android.models.characters.Player;
import com.example.rougelikegame.android.models.items.ItemTier;
import com.example.rougelikegame.android.models.items.PassiveItem;

/**
 * The Chaos Dice item modifies the player's luck such that all item tiers have equal chances of appearing.
 */
public class ChaosDiceItem implements PassiveItem {

    public static final int ID = 15;

    @Override
    public int getItemId() {
        return ID;
    }

    @Override
    public String getKey() {
        return "chaos_dice";
    }

    @Override
    public String getDisplayName() {
        return "Chaos Dice";
    }

    @Override
    public String getIconPath() {
        return "items/chaos_dice.png";
    }

    @Override
    public ItemTier getTier() {
        return ItemTier.A;
    }

    @Override
    public String getDescription() {
        return "Equalizes item reward odds across all tiers for this run.";
    }

    @Override
    public void onPickup(Player player) {
        player.setEqualItemWeights(true);
    }
}
