package com.example.rougelikegame.android.models.world;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import com.example.rougelikegame.android.managers.SoundManager;
import com.example.rougelikegame.android.models.characters.Enemy;
import com.example.rougelikegame.android.models.characters.Player;
import com.example.rougelikegame.android.models.items.PassiveItem;
import com.example.rougelikegame.android.models.items.contexts.HomingContext;

/**
 * ProjectileSystem manages all projectiles and beams in the game world.
 * It handles spawning, updating, collision detection, and rendering of both
 * player and enemy projectiles.
 */
public class ProjectileSystem {

    private static final float BEAM_LENGTH = 900f;
    private static final float BEAM_WIDTH = 22f;
    private static final float BEAM_LIFETIME = 0.6f;
    private static final float BEAM_TICK = 0.1f;
    private static final float BEAM_STEP = 30f;

    private final Array<Projectile> playerProjectiles = new Array<>();
    private final Array<Projectile> enemyProjectiles = new Array<>();
    private final Array<Beam> activeBeams = new Array<>();

    private final Pool<Projectile> projectilePool = new Pool<Projectile>() {
        @Override
        protected Projectile newObject() {
            return new Projectile();
        }
    };

    private final Pool<Beam> beamPool = new Pool<Beam>() {
        @Override
        protected Beam newObject() {
            return new Beam();
        }
    };

    // Pre-allocated objects for applyBeamTick to avoid GC
    private final Rectangle beamSampleRect = new Rectangle(0, 0, BEAM_WIDTH, BEAM_WIDTH);
    private final Vector2 beamSamplePoint = new Vector2();

    /**
     * Internal class representing a beam weapon effect.
     */
    private static class Beam implements Pool.Poolable {
        final Vector2 start = new Vector2();
        final Vector2 dir = new Vector2();
        float lifetime = BEAM_LIFETIME;
        float tickTimer = 0f;
        int damagePerTick;

        void init(float x, float y, Vector2 direction, int damagePerTick) {
            this.start.set(x, y);
            this.dir.set(direction).nor();
            this.damagePerTick = damagePerTick;
            this.lifetime = BEAM_LIFETIME;
            this.tickTimer = 0f;
        }

        @Override
        public void reset() {
            start.set(0, 0);
            dir.set(0, 0);
            lifetime = 0;
            tickTimer = 0;
            damagePerTick = 0;
        }
    }

    public Array<Projectile> getEnemyProjectiles() {
        return enemyProjectiles;
    }

    public Array<Projectile> getPlayerProjectiles() {
        return playerProjectiles;
    }

    /**
     * Spawns a new projectile fired by the player.
     *
     * @param x X coordinate of the starting position
     * @param y Y coordinate of the starting position
     * @param dir Direction vector of the projectile
     * @param damage Damage dealt by the projectile
     * @param homingContext Context containing homing behavior parameters
     */
    public void spawnPlayerProjectile(float x, float y, Vector2 dir, int damage, HomingContext homingContext) {
        Projectile p = projectilePool.obtain();
        p.init(
            x,
            y,
            dir.x,
            dir.y,
            damage,
            homingContext != null && homingContext.enabled,
            homingContext != null ? homingContext.homingRange : 0f,
            homingContext != null ? homingContext.homingStrength : 0f,
            homingContext != null ? homingContext.maxTurnRateDeg : 0f
        );
        playerProjectiles.add(p);
    }

    /**
     * Spawns a new projectile fired by an enemy.
     *
     * @param x X coordinate of the starting position
     * @param y Y coordinate of the starting position
     * @param dirX X component of direction
     * @param dirY Y component of direction
     * @param damage Damage dealt by the projectile
     * @param speed Speed of the projectile
     */
    public void spawnEnemyProjectile(float x, float y, float dirX, float dirY, int damage, float speed) {
        Projectile p = projectilePool.obtain();
        p.init(x, y, dirX, dirY, damage, false, 0, 0, 0);
        p.speed = speed;
        enemyProjectiles.add(p);
    }

    /**
     * Spawns a beam weapon effect.
     *
     * @param x X coordinate of the starting position
     * @param y Y coordinate of the starting position
     * @param dir Direction vector of the beam
     * @param damagePerTick Damage dealt per tick of the beam
     */
    public void spawnBeam(float x, float y, Vector2 dir, int damagePerTick) {
        if (dir.isZero(0.01f) || damagePerTick <= 0) return;
        Beam beam = beamPool.obtain();
        beam.init(x, y, dir, damagePerTick);
        activeBeams.add(beam);
    }

    /**
     * Updates all active projectiles and beams.
     *
     * @param delta Time elapsed since the last frame
     * @param enemies List of active enemies for homing calculations
     */
    public void update(float delta, Array<Enemy> enemies) {
        updateList(playerProjectiles, delta, enemies);
        updateList(enemyProjectiles, delta, null);

        for (int i = activeBeams.size - 1; i >= 0; i--) {
            Beam beam = activeBeams.get(i);
            beam.lifetime -= delta;
            beam.tickTimer += delta;

            if (beam.lifetime <= 0f) {
                activeBeams.removeIndex(i);
                beamPool.free(beam);
            }
        }
    }

    /**
     * Internal helper to update a list of projectiles.
     */
    private void updateList(Array<Projectile> list, float delta, Array<Enemy> enemies) {
        for (int i = list.size - 1; i >= 0; i--) {
            Projectile p = list.get(i);
            p.update(delta, enemies);
            if (!p.alive) {
                list.removeIndex(i);
                projectilePool.free(p);
            }
        }
    }

    /**
     * Handles collisions between player projectiles (and beams) and enemies.
     *
     * @param player The player object
     * @param enemies List of active enemies
     */
    public void handlePlayerProjectilesHitEnemies(Player player, Array<Enemy> enemies) {
        for (int i = 0; i < playerProjectiles.size; i++) {
            Projectile p = playerProjectiles.get(i);
            if (!p.alive) continue;

            for (int j = 0; j < enemies.size; j++) {
                Enemy e = enemies.get(j);
                if (!e.alive) continue;

                if (p.getBounds().overlaps(e.getBounds())) {
                    e.takeDamage(p.damage);
                    SoundManager.play("hit");

                    for (PassiveItem it : player.getPassiveItems()) {
                        it.onHitEnemy(player, e, enemies, p.damage);
                    }

                    p.alive = false;
                    // Projectile will be removed and freed in update()
                    break;
                }
            }
        }

        for (int i = 0; i < activeBeams.size; i++) {
            Beam beam = activeBeams.get(i);
            if (beam.tickTimer < BEAM_TICK) continue;

            beam.tickTimer -= BEAM_TICK;
            applyBeamTick(player, enemies, beam);
        }
    }

    /**
     * Applies a single tick of damage from a beam to all enemies it hits.
     */
    private void applyBeamTick(Player player, Array<Enemy> enemies, Beam beam) {
        for (int i = 0; i < enemies.size; i++) {
            Enemy e = enemies.get(i);
            if (!e.alive) continue;
            if (beamHitsEnemy(beamSampleRect, beamSamplePoint, beam, e)) {
                e.takeDamage(beam.damagePerTick);
                SoundManager.play("hit", 0.4f);
                for (PassiveItem it : player.getPassiveItems()) {
                    it.onHitEnemy(player, e, enemies, beam.damagePerTick);
                }
            }
        }
    }

    /**
     * Checks if a beam intersects with a specific enemy.
     */
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

    /**
     * Handles collisions between enemy projectiles and the player.
     *
     * @param player The player object
     * @param onPlayerDied Callback invoked when player health reaches zero
     * @param onPlayerDamaged Callback invoked when the player takes damage
     */
    public void handleEnemyProjectilesHitPlayer(Player player, Runnable onPlayerDied, java.util.function.IntConsumer onPlayerDamaged) {
        for (Projectile p : enemyProjectiles) {
            if (!p.alive) continue;

            if (p.getBounds().overlaps(player.bounds)) {
                if (!player.canTakeDamage()) {
                    p.alive = false;
                    continue;
                }

                int damageTaken = player.applyIncomingDamage(p.damage);
                p.alive = false;

                if (damageTaken > 0 && onPlayerDamaged != null) {
                    SoundManager.play("player_hurt");
                    onPlayerDamaged.accept(damageTaken);
                }

                if (player.health <= 0) {
                    onPlayerDied.run();
                    return;
                }
            }
        }
    }

    /**
     * Draws all active projectiles and beams.
     *
     * @param batch The SpriteBatch used for rendering
     */
    public void drawAll(SpriteBatch batch) {
        for (Projectile p : playerProjectiles) p.draw(batch);
        for (Projectile p : enemyProjectiles) p.draw(batch);
        drawBeams(batch);
    }

    /**
     * Internal helper to draw beam effects.
     */
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

    /**
     * Disposes of shared resources used by projectiles.
     */
    public void disposeShared() {
        Projectile.disposeTexture();
    }
}
