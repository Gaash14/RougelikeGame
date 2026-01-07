package com.example.rougelikegame.android.models.enemies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.example.rougelikegame.android.models.game.Projectile;
import com.example.rougelikegame.android.screens.MainActivity;

public class BossEnemy extends Enemy {

    private float shootCooldown = 2.5f;
    private float shootTimer = 0f;
    private int damage;

    private boolean reinforcementsSummoned = false;
    private int maxHealth;
    private MainActivity game;

    private Array<Projectile> projectiles;

    public BossEnemy(float startX, float startY,Array<Projectile> projectiles,
                     int health, int damage, MainActivity game) {
        super("enemies/boss.png", startX, startY,70f,
            256f,256f, health, damage);

        this.projectiles = projectiles;
        this.isBoss = true;
        this.health = health;
        this.maxHealth = health;
        this.damage = damage;
        this.game = game;
    }

    @Override
    public void update(float delta, float playerX, float playerY) {

        // slow chase
        super.update(delta * 0.4f, playerX, playerY);

        if (!reinforcementsSummoned && health <= maxHealth / 2) {
            reinforcementsSummoned = true;
            game.spawnBossReinforcements();
        }

        shootTimer -= delta;

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

        // clamp to screen
        x = MathUtils.clamp(x, 0, Gdx.graphics.getWidth() - width);
        y = MathUtils.clamp(y, 0, Gdx.graphics.getHeight() - height);
        bounds.setPosition(x, y);
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }
}
