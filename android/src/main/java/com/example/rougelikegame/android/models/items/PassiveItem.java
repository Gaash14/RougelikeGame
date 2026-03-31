package com.example.rougelikegame.android.models.items;

import com.badlogic.gdx.utils.Array;
import com.example.rougelikegame.android.models.characters.Enemy;
import com.example.rougelikegame.android.models.characters.Player;
import com.example.rougelikegame.android.models.items.contexts.BlockChanceContext;
import com.example.rougelikegame.android.models.items.contexts.CooldownContext;
import com.example.rougelikegame.android.models.items.contexts.DamageContext;
import com.example.rougelikegame.android.models.items.contexts.HomingContext;
import com.example.rougelikegame.android.models.items.contexts.IncomingDamageContext;

/**
 * The PassiveItem interface defines the structure for all passive items in the game.
 * Passive items can modify player stats, combat mechanics, and react to game events.
 */
public interface PassiveItem {

    /**
     * @return the unique numeric ID of the item
     */
    int getItemId();

    /**
     * @return the unique string key of the item (e.g., "damage_up")
     */
    String getKey();

    /**
     * @return the display name of the item
     */
    String getDisplayName();

    /**
     * @return the path to the item's icon asset
     */
    String getIconPath();

    /**
     * @return the rarity tier of the item
     */
    ItemTier getTier();

    /**
     * @return a short description of the item's effects
     */
    String getDescription();

    /**
     * Called when the player picks up the item.
     *
     * @param player the player character
     */
    default void onPickup(Player player) {
    }

    /**
     * Modifies the player's base damage stats.
     */
    default void modifyStats(Player player, DamageContext ctx) {
    }

    /**
     * Modifies the damage of ranged projectiles.
     */
    default void modifyProjectileDamage(Player player, DamageContext ctx) {
    }

    /**
     * Modifies the damage of melee attacks.
     */
    default void modifyMeleeDamage(Player player, DamageContext ctx) {
    }

    /**
     * Modifies the player's chance to block incoming damage.
     */
    default void modifyBlockChance(Player player, BlockChanceContext ctx) {
    }

    /**
     * Modifies the cooldown between melee attacks.
     */
    default void modifyMeleeCooldown(Player player, CooldownContext ctx) {
    }

    /**
     * Modifies the cooldown between ranged attacks.
     */
    default void modifyRangedCooldown(Player player, CooldownContext ctx) {
    }

    /**
     * Modifies the homing properties of projectiles.
     */
    default void modifyHoming(Player player, HomingContext ctx) {
    }

    /**
     * Modifies incoming damage before it is applied to the player.
     */
    default void modifyIncomingDamage(Player player, IncomingDamageContext ctx) {
    }

    /**
     * Called when the player hits an enemy.
     *
     * @param player the player character
     * @param enemy  the enemy that was hit
     */
    default void onHitEnemy(Player player, Enemy enemy) {
    }

    /**
     * Called when the player hits an enemy, providing additional context.
     *
     * @param player      the player character
     * @param enemy       the enemy that was hit
     * @param enemies     the list of all active enemies
     * @param damageDealt the amount of damage dealt
     */
    default void onHitEnemy(Player player, Enemy enemy, Array<Enemy> enemies, int damageDealt) {
        onHitEnemy(player, enemy);
    }

    /**
     * Called when the player takes damage.
     *
     * @param player      the player character
     * @param damageTaken the amount of damage received
     */
    default void onPlayerDamaged(Player player, int damageTaken) {
    }
}
