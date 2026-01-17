package com.example.rougelikegame.android.models.characters;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.example.rougelikegame.android.models.world.Obstacle;

public class Enemy {
    protected Texture texture;
    protected float x, y;
    protected float speed;
    public float width, height;
    protected Rectangle bounds;
    public int health;
    public boolean alive = true;
    public boolean isBoss = false;
    public boolean hitThisSwing = false;
    public int damage;

    public Enemy(Texture texture, float startX, float startY, float speed, float width, float height, int health, int damage) {
        this.texture = texture;
        this.x = startX;
        this.y = startY;
        this.speed = speed;
        this.width = width;
        this.height = height;
        this.health = health;
        this.damage = damage;

        this.bounds = new Rectangle(x, y, width, height);
    }

    // Draw the enemy
    public void draw(SpriteBatch batch) {
        batch.draw(texture, x, y, width, height);
    }

    // Update enemy position
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

        x = MathUtils.clamp(x, 0, Gdx.graphics.getWidth() - width);
        y = MathUtils.clamp(y, 0, Gdx.graphics.getHeight() - height);

        bounds.setPosition(x, y);
    }

    public void handleObstacleCollision(Array<Obstacle> obstacles) {
        for (Obstacle o : obstacles) {
            if (bounds.overlaps(o.bounds)) {
                float overlapX = Math.min(bounds.x + width - o.x, o.x + o.bounds.width - bounds.x);
                float overlapY = Math.min(bounds.y + height - o.y, o.y + o.bounds.height - bounds.y);

                // Push out in the direction of the smallest overlap
                if (overlapX < overlapY) {
                    // push left or right
                    if (x < o.x) {
                        x -= overlapX;
                    } else {
                        x += overlapX;
                    }
                } else {
                    // push down or up
                    if (y < o.y) {
                        y -= overlapY;
                    } else {
                        y += overlapY;
                    }
                }

                bounds.setPosition(x, y);
            }
        }
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
