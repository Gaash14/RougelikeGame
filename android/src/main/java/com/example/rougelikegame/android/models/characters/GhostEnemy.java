package com.example.rougelikegame.android.models.characters;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.example.rougelikegame.android.models.world.Projectile;

public class GhostEnemy extends Enemy {

    private float shootCooldown = 1.5f;
    private float shootTimer = 0f;
    private float preferredDistance = 280f;
    private int damage;

    private Array<Projectile> projectiles;

    public GhostEnemy(float x, float y, Array<Projectile> projectiles, int health, int damage) {
        super("enemies/ghost_enemy.png", x, y, 120f, 128, 128, health, damage);
        this.projectiles = projectiles;
        this.health = health;
        this.damage = damage;
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
            x -= dx * speed * delta;
            y -= dy * speed * delta;
        }

        // Shoot if cooldown ready
        if (shootTimer <= 0) {
            shootTimer = shootCooldown;

            projectiles.add(
                new Projectile(
                    x + width / 2,
                    y + height / 2,
                    playerX - x,
                    playerY - y,
                    damage
                )
            );
        }

        x = MathUtils.clamp(x, 0, Gdx.graphics.getWidth() - width);
        y = MathUtils.clamp(y, 0, Gdx.graphics.getHeight() - height);

        bounds.setPosition(x, y);
    }
}
