package com.example.rougelikegame.android.models.items.passives;

import com.example.rougelikegame.android.models.characters.Player;
import com.example.rougelikegame.android.models.items.PassiveItem;
import com.example.rougelikegame.android.models.items.contexts.BlockChanceContext;

public class ArmorItem implements PassiveItem {

    public static final int ID = 4;
    public static final float BLOCK_CHANCE_BONUS = 0.10f;

    @Override
    public int getItemId() {
        return ID;
    }

    @Override
    public String getKey() {
        return "armor";
    }

    @Override
    public String getDisplayName() {
        return "Armor";
    }

    @Override
    public void modifyBlockChance(Player player, BlockChanceContext ctx) {
        ctx.chance += BLOCK_CHANCE_BONUS;
    }
}
