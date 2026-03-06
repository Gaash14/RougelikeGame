package com.example.rougelikegame.android.models.items;

import com.example.rougelikegame.android.models.characters.Enemy;
import com.example.rougelikegame.android.models.characters.Player;
import com.example.rougelikegame.android.models.items.contexts.BlockChanceContext;
import com.example.rougelikegame.android.models.items.contexts.CooldownContext;
import com.example.rougelikegame.android.models.items.contexts.DamageContext;
import com.example.rougelikegame.android.models.items.contexts.HomingContext;
import com.example.rougelikegame.android.models.items.contexts.IncomingDamageContext;

public interface PassiveItem {
    int getItemId(); // numeric ID
    String getKey(); // string key ("damage_up")
    String getDisplayName();
    String getIconPath();
    ItemTier getTier();

    default void onPickup(Player player) {}

    default void modifyStats(Player player, DamageContext ctx) {}
    default void modifyProjectileDamage(Player player, DamageContext ctx) {}
    default void modifyMeleeDamage(Player player, DamageContext ctx) {}
    default void modifyBlockChance(Player player, BlockChanceContext ctx) {}
    default void modifyMeleeCooldown(Player player, CooldownContext ctx) {}
    default void modifyRangedCooldown(Player player, CooldownContext ctx) {}
    default void modifyHoming(Player player, HomingContext ctx) {}
    default void modifyIncomingDamage(Player player, IncomingDamageContext ctx) {}

    default void onHitEnemy(Player player, Enemy enemy) {}
    default void onPlayerDamaged(Player player, int damageTaken) {}
}
