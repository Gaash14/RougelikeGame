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

    boolean attacking = false;
    float attackTimer = 0;
    float attackDuration = 0.2f; // 0.2 sec attack window

    Rectangle attackHitbox;

    public Player(float x, float y) {
        texture = new Texture("player.png");
        this.x = x;
        this.y = y;
        this.width = 128;
        this.height = 128;
        this.health = 10;
        bounds = new Rectangle(x, y, width, height);
        attackHitbox = new Rectangle();
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
            attackTimer -= delta;
            if (attackTimer <= 0) {
                attacking = false;
            }
        }
    }

    public void attack(Joystick joystick) {

        float dx = joystick.getPercentX();
        float dy = joystick.getPercentY();

        // If joystick not moved, default attack to the right
        if (dx == 0 && dy == 0) {
            dx = 1;
            dy = 0;
        }

        // Horizontal attack
        if (Math.abs(dx) > Math.abs(dy)) {
            if (dx > 0) { // right
                attackHitbox.setPosition(x + width, y + height / 2);
            } else { // left
                attackHitbox.setPosition(x - attackHitbox.width, y + height / 2);
            }
        }
        // Vertical attack
        else {
            if (dy > 0) { // up
                attackHitbox.setPosition(x + width / 2, y + height);
            } else { // down
                attackHitbox.setPosition(x + width / 2, y - attackHitbox.height);
            }
        }

        attacking = true;
        attackTimer = 0.15f; // attack lasts 0.15 seconds
    }


    public void draw(SpriteBatch batch) {
        batch.draw(texture, x, y, width, height);
    }


    public void dispose() {
        texture.dispose();
    }

}
