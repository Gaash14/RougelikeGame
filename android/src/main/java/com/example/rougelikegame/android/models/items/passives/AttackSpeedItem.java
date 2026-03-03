package com.example.rougelikegame.android.models.items.passives;

import com.example.rougelikegame.android.models.characters.Player;
import com.example.rougelikegame.android.models.items.PassiveItem;
import com.example.rougelikegame.android.models.items.contexts.CooldownContext;

public class AttackSpeedItem implements PassiveItem {

    public static final int ID = 3;

    private static final float MELEE_REDUCTION = 0.05f;
    private static final float RANGED_REDUCTION = 0.08f;
    private static final float MIN_MELEE_COOLDOWN = 0.2f;
    private static final float MIN_RANGED_COOLDOWN = 0.3f;

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
        ctx.cooldown = Math.max(MIN_MELEE_COOLDOWN, ctx.cooldown - MELEE_REDUCTION);
    }

    @Override
    public void modifyRangedCooldown(Player player, CooldownContext ctx) {
        ctx.cooldown = Math.max(MIN_RANGED_COOLDOWN, ctx.cooldown - RANGED_REDUCTION);
    }

    @Override
    public String getIconPath() {
        return "items/attack_speed.png";
    }
}
