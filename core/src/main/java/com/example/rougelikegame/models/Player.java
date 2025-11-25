package com.example.rougelikegame.models;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

public class Player {

    // textures
    private final Texture texture;
    public final Texture debugPixel = new Texture("pixel.png");

    // position & size
    public float x, y;
    public final float width = 128;
    public final float height = 128;

    // stats
    public int health = 10;
    public float speed = 300f;
    public int coins = 0;
    public int attackBonus = 0;

    // collisions
    public final Rectangle bounds;
    public final Rectangle attackHitbox;

    public float damageCooldown = 0f;
    public float damageCooldownTime = 0.5f;
    public float knockbackX = 0;
    public float knockbackY = 0;
    public float knockbackTime = 0;
    public float knockbackDuration = 0.25f;  // how long knockback lasts
    public float knockbackStrength = 350f;   // how fast player gets pushed

    // attack system
    public boolean attacking = false;
    private float attackTime = 0f;

    public float attackCooldown = 0f;
    public float attackCooldownTime = 0.5f;   // seconds between attacks

    public Player(float x, float y) {
        this.texture = new Texture("player.png");

        this.x = x;
        this.y = y;

        this.bounds = new Rectangle(x, y, width, height);

        // size of attack area
        this.attackHitbox = new Rectangle(x, y, 100, 100);
    }

    public void update(Joystick joystick, float delta) {
        // Movement
        float dx = joystick.getPercentX();
        float dy = joystick.getPercentY();

        x += dx * speed * delta;
        y += dy * speed * delta;

        // Clamp screen boundaries
        x = MathUtils.clamp(x, 0, Gdx.graphics.getWidth() - width);
        y = MathUtils.clamp(y, 0, Gdx.graphics.getHeight() - height);

        bounds.setPosition(x, y);

        // Attack animation timer
        if (attacking) {
            attackTime -= delta;
            if (attackTime <= 0) attacking = false;
        }

        // Attack cooldown
        if (attackCooldown > 0) {
            attackCooldown -= delta;
        }

        if (damageCooldown > 0) {
            damageCooldown -= delta;
        }

        // APPLY KNOCKBACK
        if (knockbackTime > 0) {
            knockbackTime -= delta;
            x += knockbackX * knockbackStrength * delta;
            y += knockbackY * knockbackStrength * delta;
        } else {
            // Normal joystick movement
            x += joystick.getPercentX() * speed * delta;
            y += joystick.getPercentY() * speed * delta;
        }
    }

    public void attack(Joystick joystick) {
        // if cooldown is active, do nothing
        if (attackCooldown > 0) return;

        attackCooldown = attackCooldownTime;
        attacking = true;
        attackTime = 0.15f; // attack lasts 0.15 seconds

        float dx = joystick.getPercentX();
        float dy = joystick.getPercentY();

        // Direction defaults to right if joystick is neutral
        if (dx == 0 && dy == 0) dx = 1;

        // Position hitbox in the attack direction
        attackHitbox.setPosition(
            x + dx * width,
            y + dy * height
        );
    }

    public void draw(SpriteBatch batch) {
        batch.draw(texture, x, y, width, height);
    }

    public void dispose() {
        texture.dispose();
        debugPixel.dispose();
    }
}
