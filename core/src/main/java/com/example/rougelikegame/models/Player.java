package com.example.rougelikegame.models;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

public class Player {

    Texture texture;
    public float x, y;
    public float width, height;
    public float speed = 300;
    public int health;
    public Rectangle bounds;

    public Rectangle attackHitbox;
    public boolean attacking;
    public float attackTime;
    public Texture debugPixel = new Texture("pixel.png");
    public float attackCooldown = 0f;
    public float attackCooldownTime = 0.5f;   // 0.5 seconds between attacks default

    public int coins = 0;
    public int attackBonus = 0;


    public Player(float x, float y) {
        texture = new Texture("player.png");
        this.x = x;
        this.y = y;
        this.width = 128;
        this.height = 128;
        this.health = 10;
        bounds = new Rectangle(x, y, width, height);
        attackHitbox = new Rectangle(x, y, 60, 60); // size of attack area
        attacking = false;
        attackTime = 0;
    }

    public void update(Joystick joystick, float delta) {
        float knobX = joystick.getPercentX(); // returns -1 to 1
        float knobY = joystick.getPercentY(); // returns -1 to 1

        x += knobX * speed * delta;
        y += knobY * speed * delta;

        bounds.setPosition(x, y);

        // Optional: keep player inside screen bounds
        x = MathUtils.clamp(x, 0, Gdx.graphics.getWidth() - texture.getWidth());
        y = MathUtils.clamp(y, 0, Gdx.graphics.getHeight() - texture.getHeight());

        if (attacking) {
            attackTime -= delta;
            if (attackTime <= 0) {
                attacking = false;
            }
        }

        if (attackCooldown > 0) {
            attackCooldown -= delta;
        }
    }

    public void attack(Joystick joystick) {
        // If cooldown is active, do nothing
        if (attackCooldown > 0) return;

        // start cooldown
        attackCooldown = attackCooldownTime;

        attacking = true;
        attackTime = 0.15f; // attack lasts 0.15 seconds

        float dx = joystick.getPercentX();
        float dy = joystick.getPercentY();

        // default attack direction if joystick is neutral
        if (dx == 0 && dy == 0) dx = 1;

        // place hitbox in front of player
        attackHitbox.setSize(80, 80);
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
    }

}
