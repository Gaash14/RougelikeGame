package com.example.rougelikegame.android.models.items.passives;

import com.badlogic.gdx.Gdx;
import com.example.rougelikegame.android.models.characters.Player;
import com.example.rougelikegame.android.models.items.ItemTier;
import com.example.rougelikegame.android.models.items.PassiveItem;
import com.example.rougelikegame.android.models.items.contexts.CooldownContext;
import com.example.rougelikegame.android.models.items.contexts.DamageContext;

public class DarkBargainItem implements PassiveItem {

    public static final int ID = 11;

    private static final int HP = 0;
    private static final int DAMAGE = 1;
    private static final int ATTACK_SPEED = 2;
    private static final int MOVEMENT_SPEED = 3;
    private static final int STAT_COUNT = 4;

    private static final int HP_DELTA = 10;
    private static final int DAMAGE_DELTA = 5;
    private static final int SPEED_DELTA = 50;

    private static final int MIN_HEALTH = 1;
    private static final int MIN_DAMAGE = 1;
    private static final int MIN_SPEED = 100;

    private int statUp = -1;
    private int statDown = -1;

    @Override
    public int getItemId() {
        return ID;
    }

    @Override
    public String getKey() {
        return "dark_bargain";
    }

    @Override
    public String getDisplayName() {
        return "Dark Bargain";
    }

    @Override
    public String getDescription() {
        return "Greatly buffs one stat while weakening another.";
    }

    @Override
    public String getIconPath() {
        return "items/dark_bargain.png";
    }

    @Override
    public ItemTier getTier() {
        return ItemTier.D;
    }

    @Override
    public void onPickup(Player player) {
        var rng = player.getRandomSource();

        statUp = rng.nextInt(STAT_COUNT);
        do {
            statDown = rng.nextInt(STAT_COUNT);
        } while (statDown == statUp);

        applyImmediateStats(player);

        Gdx.app.log("DarkBargain", "UP=" + statUp + " DOWN=" + statDown);
    }

    private void applyImmediateStats(Player player) {
        if (statUp == HP) {
            player.health += HP_DELTA;
        }
        if (statDown == HP) {
            player.health = Math.max(MIN_HEALTH, player.health - HP_DELTA);
        }

        if (statUp == MOVEMENT_SPEED) {
            player.speed += SPEED_DELTA;
        }
        if (statDown == MOVEMENT_SPEED) {
            player.speed = Math.max(MIN_SPEED, player.speed - SPEED_DELTA);
        }
    }

    @Override
    public void modifyStats(Player player, DamageContext ctx) {
        ctx.damage = applyDamageChange(ctx.damage);
    }

    @Override
    public void modifyProjectileDamage(Player player, DamageContext ctx) {
        ctx.damage = applyDamageChange(ctx.damage);
    }

    @Override
    public void modifyMeleeDamage(Player player, DamageContext ctx) {
        ctx.damage = applyDamageChange(ctx.damage);
    }

    private int applyDamageChange(int damage) {
        if (statUp == DAMAGE) {
            damage += DAMAGE_DELTA;
        }
        if (statDown == DAMAGE) {
            damage = Math.max(MIN_DAMAGE, damage - DAMAGE_DELTA);
        }
        return damage;
    }

    @Override
    public void modifyMeleeCooldown(Player player, CooldownContext ctx) {
        ctx.cooldown = applyAttackSpeedChange(
            ctx.cooldown,
            AttackSpeedItem.MELEE_COOLDOWN_REDUCTION,
            AttackSpeedItem.MIN_MELEE_COOLDOWN
        );
    }

    @Override
    public void modifyRangedCooldown(Player player, CooldownContext ctx) {
        ctx.cooldown = applyAttackSpeedChange(
            ctx.cooldown,
            AttackSpeedItem.RANGED_COOLDOWN_REDUCTION,
            AttackSpeedItem.MIN_RANGED_COOLDOWN
        );
    }

    private float applyAttackSpeedChange(float cooldown, float delta, float minCooldown) {
        if (statUp == ATTACK_SPEED) {
            cooldown = Math.max(minCooldown, cooldown - delta);
        }
        if (statDown == ATTACK_SPEED) {
            cooldown += delta;
        }
        return cooldown;
    }
}
