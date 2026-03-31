package com.example.rougelikegame.android.models.items.passives;

import com.example.rougelikegame.android.models.characters.Player;
import com.example.rougelikegame.android.models.items.ItemTier;
import com.example.rougelikegame.android.models.items.PassiveItem;

/**
 * The Titan's Core item increases base damage significantly but also increases attack cooldown times.
 */
public class TitansCoreItem implements PassiveItem {

    public static final int ID = 14;

    @Override
    public int getItemId() {
        return ID;
    }

    @Override
    public String getKey() {
        return "titans_core";
    }

    @Override
    public String getDisplayName() {
        return "Titan's Core";
    }

    @Override
    public String getIconPath() {
        return "items/titans_core.png";
    }

    @Override
    public ItemTier getTier() {
        return ItemTier.S;
    }

    @Override
    public String getDescription() {
        return "Your attacks hit extremely hard but take much longer to perform.";
    }

    @Override
    public void onPickup(Player player) {
        player.meleeBaseDamage = (player.meleeBaseDamage + 5) * 2;
        player.rangedBaseDamage = (player.rangedBaseDamage + 5) * 2;

        player.meleeCooldownTime = player.meleeCooldownTime * 2.1f;
        player.rangedCooldownTime = player.rangedCooldownTime * 2.1f;
    }
}
