package com.example.rougelikegame.android.models.characters;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.example.rougelikegame.android.models.world.Obstacle;
import com.example.rougelikegame.android.models.world.Projectile;

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

    private final Array<Projectile> projectiles;

    /**
     * Constructs a new GhostEnemy.
     *
     * @param texture     the ghost texture
     * @param x           starting X position
     * @param y           starting Y position
     * @param projectiles array to add fired projectiles to
     * @param health      initial health
     * @param damage      contact damage (also used for projectile damage)
     */
    public GhostEnemy(Texture texture, float x, float y, Array<Projectile> projectiles, int health, int damage) {
        super(texture, x, y, 120f, 128, 128, health, damage);
        this.projectiles = projectiles;
    }

    @Override
    public void update(float delta, float playerX, float playerY) {
        shootTimer -= delta;

        float dx = playerX - x;
        float dy = playerY - y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        // Move AWAY if too close
        if (dist < preferredDistance) {
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

            Projectile projectile = new Projectile(
                x + width / 2,
                y + height / 2,
                playerX - x,
                playerY - y,
                damage
            );
            projectile.speed = PROJECTILE_SPEED;
            projectiles.add(projectile);
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
