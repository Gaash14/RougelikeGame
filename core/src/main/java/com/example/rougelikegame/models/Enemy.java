package com.example.rougelikegame.models;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class Enemy {
    private Texture texture;
    private float x, y;
    private float speed;
    public float width, height;
    private Rectangle bounds;
    public int health;
    public boolean alive = true;
    public boolean hitThisSwing = false;

    public Enemy(String texture, float startX, float startY, float speed, float width, float height) {
        this.texture = new Texture(texture);
        this.x = startX;
        this.y = startY;
        this.speed = speed;
        this.width = width;
        this.height = height;
        this.health = 30;

        this.bounds = new Rectangle(x, y, width, height);
    }

    // Draw the enemy
    public void draw(SpriteBatch batch) {
        batch.draw(texture, x, y, width, height);
    }

    // Update enemy position (basic AI: moves toward player)
    public void update(float delta, float playerX, float playerY) {
        float dx = playerX - x;
        float dy = playerY - y;

        // Normalize movement
        float length = (float) Math.sqrt(dx*dx + dy*dy);
        if (length != 0) {
            dx /= length;
            dy /= length;
        }

        x += dx * speed * delta;
        y += dy * speed * delta;

        // Update collision bounds
        bounds.setPosition(x, y);
    }

    public void takeDamage(int dmg) {
        health -= dmg;
        if (health <= 0) {
            alive = false;
        }
    }

    public float getX() { return x; }
    public float getY() { return y; }

    public void setX(float x) {
        this.x = x;
        bounds.setX(x); // keep collision synced
    }

    public void setY(float y) {
        this.y = y;
        bounds.setY(y); // keep collision synced
    }

    // Get collision rectangle
    public Rectangle getBounds() {
        return bounds;
    }

    // Dispose the texture when done
    public void dispose() {
        texture.dispose();
    }
}
