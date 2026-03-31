package com.example.rougelikegame.android.models.items.passives;

import com.example.rougelikegame.android.models.items.ItemTier;
import com.example.rougelikegame.android.models.items.PassiveItem;

/**
 * The Beam item represents a powerful offensive upgrade that allows the player to use a light beam.
 */
public class BeamItem implements PassiveItem {

    public static final int ID = 5;

    @Override
    public int getItemId() {
        return ID;
    }

    @Override
    public String getKey() {
        return "beam";
    }

    @Override
    public String getDisplayName() {
        return "Light Beam";
    }

    @Override
    public String getDescription() {
        return "Unlocks a piercing light beam attack.";
    }

    @Override
    public String getIconPath() {
        return "items/beam.png";
    }

    @Override
    public ItemTier getTier() {
        return ItemTier.S;
    }
}
