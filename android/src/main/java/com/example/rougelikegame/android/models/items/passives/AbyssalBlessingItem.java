package com.example.rougelikegame.android.models.items.passives;

import com.example.rougelikegame.android.models.characters.Player;
import com.example.rougelikegame.android.models.items.ItemTier;
import com.example.rougelikegame.android.models.items.PassiveItem;
import com.example.rougelikegame.android.models.items.contexts.CooldownContext;
import com.example.rougelikegame.android.models.items.contexts.DamageContext;

public class AbyssalBlessingItem implements PassiveItem {

    public static final int ID = 9;

    private static final int HEALTH_BONUS = 10;
    private static final int DAMAGE_BONUS = 5;
    private static final int SPEED_BONUS = 50;
    private static final int COIN_BONUS = 1;

    @Override
    public int getItemId() {
        return ID;
    }

    @Override
    public String getKey() {
        return "abyssal_blessing";
    }

    @Override
    public String getDisplayName() {
        return "Abyssal Blessing";
    }

    @Override
    public void onPickup(Player player) {
        player.health += HEALTH_BONUS;
        player.speed += SPEED_BONUS;
        player.addCoins(COIN_BONUS);
    }

    @Override
    public void modifyStats(Player player, DamageContext ctx) {
        ctx.damage += DAMAGE_BONUS;
    }

    @Override
    public void modifyProjectileDamage(Player player, DamageContext ctx) {
        ctx.damage += DAMAGE_BONUS;
    }

    @Override
    public void modifyMeleeDamage(Player player, DamageContext ctx) {
        ctx.damage += DAMAGE_BONUS;
    }

    @Override
    public void modifyMeleeCooldown(Player player, CooldownContext ctx) {
        ctx.cooldown = Math.max(
            AttackSpeedItem.MIN_MELEE_COOLDOWN,
            ctx.cooldown - AttackSpeedItem.MELEE_COOLDOWN_REDUCTION
        );
    }

    @Override
    public void modifyRangedCooldown(Player player, CooldownContext ctx) {
        ctx.cooldown = Math.max(
            AttackSpeedItem.MIN_RANGED_COOLDOWN,
            ctx.cooldown - AttackSpeedItem.RANGED_COOLDOWN_REDUCTION
        );
    }

    @Override
    public String getDescription() {
        return "Boosts health, damage, speed, and grants bonus coins.";
    }

    @Override
    public String getIconPath() {
        return "items/abyssal_blessing.png";
    }

    @Override
    public ItemTier getTier() {
        return ItemTier.A;
    }
}
