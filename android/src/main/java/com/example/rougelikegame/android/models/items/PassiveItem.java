package com.example.rougelikegame.android.models.items;

import com.example.rougelikegame.android.models.characters.Enemy;
import com.example.rougelikegame.android.models.characters.Player;

public interface PassiveItem {
    int getItemId(); // numeric ID
    String getKey(); // string key ("damage_up")
    String getDisplayName();

    default void onPickup(Player player) {}

    default void modifyStats(Player player, DamageContext ctx) {}
    default void modifyProjectileDamage(Player player, DamageContext ctx) {}
    default void modifyMeleeDamage(Player player, DamageContext ctx) {}

    default void onHitEnemy(Player player, Enemy enemy) {}
}
