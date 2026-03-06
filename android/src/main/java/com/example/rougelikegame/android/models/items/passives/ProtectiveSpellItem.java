package com.example.rougelikegame.android.models.items.passives;

import com.example.rougelikegame.android.models.characters.Player;
import com.example.rougelikegame.android.models.items.ItemTier;
import com.example.rougelikegame.android.models.items.PassiveItem;
import com.example.rougelikegame.android.models.items.contexts.IncomingDamageContext;

public class ProtectiveSpellItem implements PassiveItem {

    public static final int ID = 13;
    private static final int HEALTH_BONUS = 10;
    private static final int DAMAGE_CAP = 1;

    @Override
    public int getItemId() {
        return ID;
    }

    @Override
    public String getKey() {
        return "protective_spell";
    }

    @Override
    public String getDisplayName() {
        return "Protective Spell";
    }

    @Override
    public void onPickup(Player player) {
        player.health += HEALTH_BONUS;
    }

    @Override
    public void modifyIncomingDamage(Player player, IncomingDamageContext ctx) {
        ctx.maxDamage = Math.min(ctx.maxDamage, DAMAGE_CAP);
    }

    @Override
    public String getDescription() {
        return "Caps incoming hit damage and grants bonus health.";
    }


    @Override
    public String getIconPath() {
        return "items/protective_spell.png";
    }

    @Override
    public ItemTier getTier() {
        return ItemTier.S;
    }
}
