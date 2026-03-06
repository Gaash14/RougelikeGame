package com.example.rougelikegame.android.models.items.passives;

import com.example.rougelikegame.android.models.characters.Player;
import com.example.rougelikegame.android.models.items.ItemTier;
import com.example.rougelikegame.android.models.items.PassiveItem;
import com.example.rougelikegame.android.models.items.contexts.CooldownContext;

public class AttackSpeedItem implements PassiveItem {

    public static final int ID = 3;

    public static final float MELEE_COOLDOWN_REDUCTION = 0.05f;
    public static final float RANGED_COOLDOWN_REDUCTION = 0.08f;
    public static final float MIN_MELEE_COOLDOWN = 0.2f;
    public static final float MIN_RANGED_COOLDOWN = 0.3f;

    @Override
    public int getItemId() {
        return ID;
    }

    @Override
    public String getKey() {
        return "attack_speed";
    }

    @Override
    public String getDisplayName() {
        return "Attack Speed";
    }

    @Override
    public void modifyMeleeCooldown(Player player, CooldownContext ctx) {
        ctx.cooldown = Math.max(MIN_MELEE_COOLDOWN, ctx.cooldown - MELEE_COOLDOWN_REDUCTION);
    }

    @Override
    public void modifyRangedCooldown(Player player, CooldownContext ctx) {
        ctx.cooldown = Math.max(MIN_RANGED_COOLDOWN, ctx.cooldown - RANGED_COOLDOWN_REDUCTION);
    }

    @Override
    public String getDescription() {
        return "Reduces melee and ranged attack cooldowns.";
    }

    @Override
    public String getIconPath() {
        return "items/attack_speed.png";
    }

    @Override
    public ItemTier getTier() {
        return ItemTier.C;
    }
}
