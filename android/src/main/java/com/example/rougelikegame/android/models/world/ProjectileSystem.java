package com.example.rougelikegame.android.models.world;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.math.Vector2;
import com.example.rougelikegame.android.models.characters.Enemy;
import com.example.rougelikegame.android.models.characters.Player;
import com.example.rougelikegame.android.models.items.PassiveItem;

public class ProjectileSystem {

    private final Array<Projectile> playerProjectiles = new Array<>();
    private final Array<Projectile> enemyProjectiles = new Array<>();

    public Array<Projectile> getEnemyProjectiles() {
        return enemyProjectiles;
    }

    public Array<Projectile> getPlayerProjectiles() {
        return playerProjectiles;
    }

    // ----- Spawning -----

    public void spawnPlayerProjectile(float x, float y, Vector2 dir, int damage) {
        Projectile p = new Projectile(x, y, dir.x, dir.y, damage);
        playerProjectiles.add(p);
    }

    // ----- Update -----

    public void update(float delta) {
        updateList(playerProjectiles, delta);
        updateList(enemyProjectiles, delta);
    }

    private void updateList(Array<Projectile> list, float delta) {
        for (int i = list.size - 1; i >= 0; i--) {
            Projectile p = list.get(i);
            p.update(delta);
            if (!p.alive) {
                list.removeIndex(i);
            }
        }
    }

    // ----- Hits -----

    public void handlePlayerProjectilesHitEnemies(Player player, Array<Enemy> enemies) {
        for (Projectile p : playerProjectiles) {
            if (!p.alive) continue;

            for (Enemy e : enemies) {
                if (!e.alive) continue;

                if (p.getBounds().overlaps(e.getBounds())) {

                    e.takeDamage(p.damage);

                    for (PassiveItem it : player.getPassiveItems()) {
                        it.onHitEnemy(player, e);
                    }

                    p.alive = false;
                    break;
                }
            }
        }
    }

    public void handleEnemyProjectilesHitPlayer(Player player, Runnable onPlayerDied) {
        if (player.isImmune()) return;

        for (Projectile p : enemyProjectiles) {
            if (!p.alive) continue;

            if (p.getBounds().overlaps(player.bounds)) {
                player.health -= p.damage;
                p.alive = false;

                if (player.health <= 0) {
                    onPlayerDied.run();
                    return;
                }
            }
        }
    }

    // ----- Draw -----

    public void drawAll(com.badlogic.gdx.graphics.g2d.SpriteBatch batch) {
        for (Projectile p : playerProjectiles) p.draw(batch);
        for (Projectile p : enemyProjectiles) p.draw(batch);
    }

    // ----- Dispose -----

    public void disposeShared() {
        Projectile.disposeTexture();
    }
}
