package com.example.rougelikegame.android.models.world;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.math.Vector2;
import com.example.rougelikegame.android.managers.SoundManager;
import com.example.rougelikegame.android.models.characters.Enemy;
import com.example.rougelikegame.android.models.characters.Player;
import com.example.rougelikegame.android.models.items.PassiveItem;

public class ProjectileSystem {

    private static final float BEAM_LENGTH = 900f;
    private static final float BEAM_WIDTH = 22f;
    private static final float BEAM_LIFETIME = 0.6f;
    private static final float BEAM_TICK = 0.1f;
    private static final float BEAM_STEP = 30f;

    private final Array<Projectile> playerProjectiles = new Array<>();
    private final Array<Projectile> enemyProjectiles = new Array<>();
    private final Array<Beam> activeBeams = new Array<>();

    private static class Beam {
        final Vector2 start = new Vector2();
        final Vector2 dir = new Vector2();
        float lifetime = BEAM_LIFETIME;
        float tickTimer = 0f;
        int damagePerTick;

        Beam(float x, float y, Vector2 direction, int damagePerTick) {
            this.start.set(x, y);
            this.dir.set(direction).nor();
            this.damagePerTick = damagePerTick;
        }
    }

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
    public void spawnBeam(float x, float y, Vector2 dir, int damagePerTick) {
        if (dir.isZero(0.01f) || damagePerTick <= 0) return;
        activeBeams.add(new Beam(x, y, dir, damagePerTick));
    }

    public void update(float delta) {
        updateList(playerProjectiles, delta);
        updateList(enemyProjectiles, delta);

        for (int i = activeBeams.size - 1; i >= 0; i--) {
            Beam beam = activeBeams.get(i);
            beam.lifetime -= delta;
            beam.tickTimer += delta;

            if (beam.lifetime <= 0f) {
                activeBeams.removeIndex(i);
            }
        }
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
                    SoundManager.play("hit");

                    for (PassiveItem it : player.getPassiveItems()) {
                        it.onHitEnemy(player, e);
                    }

                    p.alive = false;
                    break;
                }
            }
        }

        for (Beam beam : activeBeams) {
            if (beam.tickTimer < BEAM_TICK) continue;

            beam.tickTimer -= BEAM_TICK;
            applyBeamTick(player, enemies, beam);
        }
    }

    private void applyBeamTick(Player player, Array<Enemy> enemies, Beam beam) {
        Rectangle sampleRect = new Rectangle(0, 0, BEAM_WIDTH, BEAM_WIDTH);
        Vector2 samplePoint = new Vector2();

        for (Enemy e : enemies) {
            if (!e.alive) continue;
            if (beamHitsEnemy(sampleRect, samplePoint, beam, e)) {
                e.takeDamage(beam.damagePerTick);
                SoundManager.play("hit", 0.4f);
                for (PassiveItem it : player.getPassiveItems()) {
                    it.onHitEnemy(player, e);
                }
            }
        }
    }

    private boolean beamHitsEnemy(Rectangle sampleRect, Vector2 samplePoint, Beam beam, Enemy enemy) {
        Rectangle enemyBounds = enemy.getBounds();

        for (float d = 0f; d <= BEAM_LENGTH; d += BEAM_STEP) {
            samplePoint.set(beam.start.x + beam.dir.x * d, beam.start.y + beam.dir.y * d);
            sampleRect.setCenter(samplePoint.x, samplePoint.y);
            if (sampleRect.overlaps(enemyBounds)) {
                return true;
            }
        }

        return false;
    }

    public void handleEnemyProjectilesHitPlayer(Player player, Runnable onPlayerDied, java.util.function.IntConsumer onPlayerDamaged) {
        if (player.isImmune()) return;

        for (Projectile p : enemyProjectiles) {
            if (!p.alive) continue;

            if (p.getBounds().overlaps(player.bounds)) {
                int damageTaken = player.applyIncomingDamage(p.damage);
                SoundManager.play("player_hurt");
                p.alive = false;

                if (damageTaken > 0 && onPlayerDamaged != null) {
                    onPlayerDamaged.accept(damageTaken);
                }

                if (player.health <= 0) {
                    onPlayerDied.run();
                    return;
                }
            }
        }
    }

    // ----- Draw -----

    public void drawAll(SpriteBatch batch) {
        for (Projectile p : playerProjectiles) p.draw(batch);
        for (Projectile p : enemyProjectiles) p.draw(batch);
        drawBeams(batch);
    }

    private void drawBeams(SpriteBatch batch) {
        for (Beam beam : activeBeams) {
            float angle = beam.dir.angleDeg();
            float alpha = MathUtils.clamp(beam.lifetime / BEAM_LIFETIME, 0.2f, 0.8f);

            batch.setColor(1f, 0.1f, 0.1f, alpha);
            batch.draw(
                Projectile.getTexture(),
                beam.start.x,
                beam.start.y - BEAM_WIDTH * 0.5f,
                0f,
                BEAM_WIDTH * 0.5f,
                BEAM_LENGTH,
                BEAM_WIDTH,
                1f,
                1f,
                angle,
                0,
                0,
                1,
                1,
                false,
                false
            );
        }
        batch.setColor(1f, 1f, 1f, 1f);
    }

    // ----- Dispose -----

    public void disposeShared() {
        Projectile.disposeTexture();
    }
}
