package com.example.rougelikegame.android.models.items.passives;

import com.example.rougelikegame.android.models.characters.Player;
import com.example.rougelikegame.android.models.items.PassiveItem;
import com.example.rougelikegame.android.models.items.contexts.DamageContext;

public class DamageUpItem implements PassiveItem {

    public static final int ID = 1;

    @Override
    public int getItemId() {
        return ID;
    }

    @Override
    public String getKey() {
        return "damage_up";
    }

    @Override
    public String getDisplayName() {
        return "Damage Up";
    }

    @Override
    public String getIconPath() {
        return "pickups/pickup_damage.png";
    }

    @Override
    public void modifyStats(Player player, DamageContext ctx) {
        ctx.damage += 5;
    }

    @Override
    public void modifyProjectileDamage(Player player, DamageContext ctx) {
        ctx.damage += 5;
    }

    @Override
    public void modifyMeleeDamage(Player player, DamageContext ctx) {
        ctx.damage += 5;
    }
}
