package com.example.rougelikegame.android.models.core;

import com.badlogic.gdx.utils.Array;
import com.example.rougelikegame.android.managers.SoundManager;
import com.example.rougelikegame.android.models.characters.Enemy;
import com.example.rougelikegame.android.models.characters.Player;
import com.example.rougelikegame.android.models.items.PassiveItem;
import com.example.rougelikegame.android.models.items.contexts.DamageContext;

/**
 * The CombatSystem class manages interactions between the player and enemies,
 * including collisions, attacks, and enemy cleanup.
 */
public class CombatSystem {

    /**
     * Callbacks interface for combat-related events.
     */
    public interface Callbacks {
        void onPlayerDied();
        void onBossDefeated();
        void onEnemyKilled(Enemy enemy);
        void onPlayerDamaged(int damageTaken);
    }

    private final Callbacks callbacks;

    /**
     * Constructs a CombatSystem with the specified callbacks.
     *
     * @param callbacks the callbacks to handle combat events
     */
    public CombatSystem(Callbacks callbacks) {
        this.callbacks = callbacks;
    }

    /**
     * Prevents enemies from overlapping by pushing them apart.
     *
     * @param enemies the list of active enemies
     */
    public void preventEnemyOverlap(Array<Enemy> enemies) {
        for (int i = 0; i < enemies.size; i++) {
            Enemy a = enemies.get(i);

            for (int j = i + 1; j < enemies.size; j++) {
                Enemy b = enemies.get(j);

                float dx = b.getX() - a.getX();
                float dy = b.getY() - a.getY();

                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                float minDist = a.width;

                if (dist < minDist && dist > 0) {
                    float overlap = minDist - dist;

                    // normalize
                    dx /= dist;
                    dy /= dist;

                    // push each enemy half the overlap
                    a.setX(a.getX() - dx * overlap * 0.5f);
                    a.setY(a.getY() - dy * overlap * 0.5f);

                    b.setX(b.getX() + dx * overlap * 0.5f);
                    b.setY(b.getY() + dy * overlap * 0.5f);
                }
            }
        }
    }

    /**
     * Handles collisions between the player and enemies.
     *
     * @param player  the player character
     * @param enemies the list of active enemies
     */
    public void handlePlayerEnemyCollision(Player player, Array<Enemy> enemies) {
        if (player.isImmune()) return;

        for (Enemy e : enemies) {
            if (!e.alive) continue;

            if (e.getBounds().overlaps(player.bounds)) {

                if (player.damageCooldown <= 0) {

                    int damageTaken = player.applyIncomingDamage(e.damage);
                    SoundManager.play("player_hurt");

                    if (damageTaken > 0) {
                        callbacks.onPlayerDamaged(damageTaken);
                    }

                    if (player.health <= 0) {
                        callbacks.onPlayerDied();
                        return;
                    }

                    // start damage cooldown
                    player.damageCooldown = player.damageCooldownTime;

                    // knockback direction
                    float dx = player.x - e.getX();
                    float dy = player.y - e.getY();
                    float len = (float) Math.sqrt(dx * dx + dy * dy);
                    if (len != 0) {
                        dx /= len;
                        dy /= len;
                    }

                    // Apply knockback to player
                    player.knockbackX = dx;
                    player.knockbackY = dy;
                    player.knockbackTime = player.knockbackDuration;
                }
            }
        }
    }

    /**
     * Handles damage dealt by the player to enemies during an attack.
     *
     * @param player  the player character
     * @param enemies the list of active enemies
     */
    public void handleAttackDamage(Player player, Array<Enemy> enemies) {
        if (!player.attacking) return;

        for (int i = 0; i < enemies.size; i++) {
            Enemy e = enemies.get(i);
            if (!e.alive) continue;
            if (e.hitThisSwing) continue;
            if (!e.getBounds().overlaps(player.attackHitbox)) continue;

            DamageContext ctx = new DamageContext(player.getCurrentDamage());

            for (PassiveItem it : player.getPassiveItems()) {
                it.modifyMeleeDamage(player, ctx);
            }

            e.takeDamage(ctx.damage);
            SoundManager.play("hit");

            for (PassiveItem it : player.getPassiveItems()) {
                it.onHitEnemy(player, e, enemies, ctx.damage);
            }

            e.hitThisSwing = true;

            // knockback enemy away from player
            float dx = e.getX() - player.x;
            float dy = e.getY() - player.y;

            float length = (float) Math.sqrt(dx * dx + dy * dy);
            if (length != 0) {
                dx /= length;
                dy /= length;
            }

            float knockback = 160f;
            e.setX(e.getX() + dx * knockback);
            e.setY(e.getY() + dy * knockback);
        }
    }

    /**
     * Removes dead enemies from the list and triggers killed callbacks.
     *
     * @param enemies the list of active enemies
     */
    public void cleanupDeadEnemies(Array<Enemy> enemies) {
        for (int i = enemies.size - 1; i >= 0; i--) {
            Enemy dead = enemies.get(i);
            if (!dead.alive) {
                enemies.removeIndex(i);

                callbacks.onEnemyKilled(dead);

                if (dead.isBoss) {
                    callbacks.onBossDefeated();
                }
            }
        }
    }
}
