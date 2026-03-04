package com.example.rougelikegame.android.models.items.passives;

import com.example.rougelikegame.android.models.characters.Player;
import com.example.rougelikegame.android.models.items.ItemTier;
import com.example.rougelikegame.android.models.items.PassiveItem;
import com.example.rougelikegame.android.models.items.contexts.CooldownContext;
import com.example.rougelikegame.android.models.items.contexts.DamageContext;

public class OverclockInjectorItem implements PassiveItem {

    public static final int ID = 8;

    private static final float ATTACK_SPEED_MULTIPLIER = 5.5f;
    private static final float DAMAGE_MULTIPLIER = 0.2f;

    @Override
    public int getItemId() {
        return ID;
    }

    @Override
    public String getKey() {
        return "overclock_injector";
    }

    @Override
    public String getDisplayName() {
        return "Overclock Injector";
    }

    @Override
    public String getIconPath() {
        return "items/overclock_injector.png";
    }

    @Override
    public ItemTier getTier() {
        return ItemTier.B;
    }

    @Override
    public void modifyStats(Player player, DamageContext ctx) {
        ctx.damage = Math.round(ctx.damage * DAMAGE_MULTIPLIER);
    }

    @Override
    public void modifyProjectileDamage(Player player, DamageContext ctx) {
        ctx.damage = Math.round(ctx.damage * DAMAGE_MULTIPLIER);
    }

    @Override
    public void modifyMeleeDamage(Player player, DamageContext ctx) {
        ctx.damage = Math.round(ctx.damage * DAMAGE_MULTIPLIER);
    }

    @Override
    public void modifyMeleeCooldown(Player player, CooldownContext ctx) {
        ctx.cooldown /= ATTACK_SPEED_MULTIPLIER;
    }

    @Override
    public void modifyRangedCooldown(Player player, CooldownContext ctx) {
        ctx.cooldown /= ATTACK_SPEED_MULTIPLIER;
    }
}
