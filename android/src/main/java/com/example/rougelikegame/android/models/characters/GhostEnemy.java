package com.example.rougelikegame.android.models.characters;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.example.rougelikegame.android.models.world.Obstacle;
import com.example.rougelikegame.android.models.world.ProjectileSystem;

/**
 * A specialized enemy that attempts to keep a distance from the player
 * and fires projectiles from afar. It phase through obstacles by ignoring
 * obstacle collisions.
 */
public class GhostEnemy extends Enemy {

    private static final float SHOOT_COOLDOWN_SECONDS = 2.4f;
    private static final float PROJECTILE_SPEED = 680f;
    private float shootTimer = 0f;
    private final float preferredDistance = 280f;

    private final ProjectileSystem projectileSystem;

    /**
     * Constructs a new GhostEnemy.
     *
     * @param texture          the ghost texture
     * @param x                starting X position
     * @param y                starting Y position
     * @param projectileSystem system for managing projectiles
     * @param health           initial health
     * @param damage           contact damage (also used for projectile damage)
     */
    public GhostEnemy(Texture texture, float x, float y, ProjectileSystem projectileSystem, int health, int damage) {
        super(texture, x, y, 120f, 128, 128, health, damage);
        this.projectileSystem = projectileSystem;
    }

    @Override
    public void update(float delta, float playerX, float playerY) {
        shootTimer -= delta;

        float dx = playerX - x;
        float dy = playerY - y;
        float distSq = dx * dx + dy * dy;

        // Move AWAY if too close
        if (distSq < preferredDistance * preferredDistance) {
            float dist = (float) Math.sqrt(distSq);
            dx /= dist;
            dy /= dist;
            updateAnimationState(delta, -dx, -dy);
            x -= dx * speed * delta;
            y -= dy * speed * delta;
        } else {
            updateAnimationState(delta, 0f, 0f);
        }

        // Shoot if cooldown ready
        if (shootTimer <= 0) {
            shootTimer = SHOOT_COOLDOWN_SECONDS;

            projectileSystem.spawnEnemyProjectile(
                x + width / 2,
                y + height / 2,
                playerX - (x + width / 2),
                playerY - (y + height / 2),
                damage,
                PROJECTILE_SPEED
            );
        }

        x = MathUtils.clamp(x, 0, Gdx.graphics.getWidth() - width);
        y = MathUtils.clamp(y, 0, Gdx.graphics.getHeight() - height);

        bounds.setPosition(x, y);
    }

    @Override
    public void handleObstacleCollision(Array<Obstacle> obstacles) {
        // Ghost enemies can phase through obstacles, so we do nothing here
    }
}
