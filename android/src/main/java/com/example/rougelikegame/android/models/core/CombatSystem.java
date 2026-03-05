package com.example.rougelikegame.android.models.core;

import com.badlogic.gdx.utils.Array;
import com.example.rougelikegame.android.managers.SoundManager;
import com.example.rougelikegame.android.models.characters.Enemy;
import com.example.rougelikegame.android.models.characters.Player;
import com.example.rougelikegame.android.models.items.contexts.DamageContext;
import com.example.rougelikegame.android.models.items.PassiveItem;

public class CombatSystem {

    public interface Callbacks {
        void onPlayerDied();
        void onBossDefeated();
        void onEnemyKilled(Enemy enemy);
        void onPlayerDamaged(int damageTaken);
    }

    private final Callbacks callbacks;

    public CombatSystem(Callbacks callbacks) {
        this.callbacks = callbacks;
    }

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

    // enemy touches player
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

    // player swinging
    public void handleAttackDamage(Player player, Array<Enemy> enemies) {
        if (!player.attacking) return;

        for (Enemy e : enemies) {
            if (!e.alive) continue;
            if (e.hitThisSwing) continue;
            if (!e.getBounds().overlaps(player.attackHitbox)) continue;

            DamageContext ctx = new DamageContext(player.getCurrentDamage());

            for (PassiveItem it : player.getPassiveItems()) {
                it.modifyMeleeDamage(player, ctx);
                it.onHitEnemy(player, e);
            }

            e.takeDamage(ctx.damage);
            SoundManager.play("hit");

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
