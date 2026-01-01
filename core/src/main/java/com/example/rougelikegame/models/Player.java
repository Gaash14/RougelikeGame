package com.example.rougelikegame.models;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class Player {

    public enum PlayerClass {
        MELEE,
        RANGED
    }
    public PlayerClass playerClass = PlayerClass.MELEE; // default

    // textures
    private final Texture texture;
    public final Texture debugPixel = new Texture("pixel.png");

    // position & size
    public float x, y;
    public final float width = 128;
    public final float height = 128;

    // stats
    public int health = 10;
    public int speed = 400;
    public int coins = 0;
    public int attackBonus = 0;

    // collisions
    public final Rectangle bounds;
    public final Rectangle attackHitbox;

    // damage + knockback (knockback is enemy -> player)
    public float damageCooldown = 0f;
    public float damageCooldownTime = 0.5f;
    public float knockbackX = 0;
    public float knockbackY = 0;
    public float knockbackTime = 0;
    public float knockbackDuration = 0.25f;
    public float knockbackStrength = 350f;

    // base damage values
    public int meleeBaseDamage = 10;
    public int rangedBaseDamage = 6; // weaker than melee

    // melee attack
    public boolean attacking = false;
    private float attackTime = 0f;

    public float meleeCooldown = 0f;
    public float meleeCooldownTime = 0.5f;

    // ranged attack
    public float rangedCooldown = 0f;
    public float rangedCooldownTime = 0.8f; // longer cooldown

    public Player(float x, float y) {
        this.texture = new Texture("player.png");

        this.x = x;
        this.y = y;

        this.bounds = new Rectangle(x, y, width, height);
        this.attackHitbox = new Rectangle(x, y, 100, 100);
    }

    public void update(Joystick joystick, float delta) {

        // timers
        if (attacking) {
            attackTime -= delta;
            if (attackTime <= 0) attacking = false;
        }

        if (meleeCooldown > 0) meleeCooldown -= delta;
        if (rangedCooldown > 0) rangedCooldown -= delta;
        if (damageCooldown > 0) damageCooldown -= delta;

        float dx = joystick.getPercentX();
        float dy = joystick.getPercentY();

        // movement / knockback
        if (knockbackTime > 0) {
            knockbackTime -= delta;
            x += knockbackX * knockbackStrength * delta;
            y += knockbackY * knockbackStrength * delta;
        } else {
            x += dx * speed * delta;
            y += dy * speed * delta;
        }

        x = MathUtils.clamp(x, 0, Gdx.graphics.getWidth() - width);
        y = MathUtils.clamp(y, 0, Gdx.graphics.getHeight() - height);

        bounds.setPosition(x, y);
    }

    public int getCurrentDamage() {
        if (playerClass == PlayerClass.MELEE) {
            return meleeBaseDamage + attackBonus;
        } else {
            return rangedBaseDamage + (attackBonus / 2);
        }
    }

    public void handleObstacleCollision(Array<Obstacle> obstacles) {
        for (Obstacle o : obstacles) {
            if (bounds.overlaps(o.bounds)) {

                float overlapX = Math.min(bounds.x + width - o.bounds.x, o.bounds.x + o.bounds.width - bounds.x);
                float overlapY = Math.min(bounds.y + height - o.bounds.y, o.bounds.y + o.bounds.height - bounds.y);

                // Push out in the direction of the smallest overlap
                if (overlapX < overlapY) {
                    if (bounds.x < o.bounds.x) x -= overlapX;
                    else x += overlapX;
                } else {
                    if (bounds.y < o.bounds.y) y -= overlapY;
                    else y += overlapY;
                }

                bounds.setPosition(x, y);
            }
        }
    }

    // ---------- MELEE ----------
    public boolean canMelee() {
        return meleeCooldown <= 0f;
    }

    public void meleeAttack(Joystick joystick) {
        if (!canMelee()) return;

        meleeCooldown = meleeCooldownTime;
        attacking = true;
        attackTime = 0.15f;

        float dx = joystick.getPercentX();
        float dy = joystick.getPercentY();

        // default right if neutral
        if (Math.abs(dx) < 0.15f && Math.abs(dy) < 0.15f) {
            dx = 1;
            dy = 0;
        }

        attackHitbox.setPosition(
            x + dx * width,
            y + dy * height
        );
    }

    // ---------- RANGED ----------
    public boolean canShoot() {
        return rangedCooldown <= 0f;
    }

    public Vector2 getShootDirection(Joystick joystick) {
        float dx = joystick.getPercentX();
        float dy = joystick.getPercentY();

        if (Math.abs(dx) < 0.15f && Math.abs(dy) < 0.15f) {
            return new Vector2(1, 0); // default right
        }

        return new Vector2(dx, dy).nor();
    }

    public void triggerRangedCooldown() {
        rangedCooldown = rangedCooldownTime;
    }

    public void draw(SpriteBatch batch) {
        batch.draw(texture, x, y, width, height);
    }

    public void dispose() {
        texture.dispose();
        debugPixel.dispose();
    }
}
