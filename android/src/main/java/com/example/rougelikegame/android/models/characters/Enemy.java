package com.example.rougelikegame.android.models.characters;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.example.rougelikegame.android.graphics.FrameAnimation;
import com.example.rougelikegame.android.graphics.FrameAnimationManager;
import com.example.rougelikegame.android.models.world.Obstacle;

public class Enemy {
    protected Texture texture;
    protected FrameAnimationManager animationManager;
    protected String animationBasePath;
    protected float x, y;
    protected float speed;
    public float width, height;
    protected Rectangle bounds;
    public int health;
    public boolean alive = true;
    public boolean isBoss = false;
    public boolean hitThisSwing = false;
    public int damage;
    protected float stateTime = 0f;
    protected boolean moving = false;
    protected boolean facingLeft = false;

    private final StatusEffects effects = new StatusEffects();

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
        if (effects.isBurning()) {
            batch.setColor(1f, 0.65f, 0.45f, 1f);
        } else if (effects.isPoisoned()) {
            batch.setColor(0.8f, 0.55f, 1f, 1f);
        }
        TextureRegion frame = getCurrentFrame();
        if (frame != null) {
            drawCurrentFrame(batch, frame);
        } else {
            drawCurrentTexture(batch);
        }
        if (effects.isBurning() || effects.isPoisoned()) {
            batch.setColor(Color.WHITE);
        }
    }

    // Update enemy position
    public void update(float delta, float playerX, float playerY) {
        // movements
        float dx = playerX - x;
        float dy = playerY - y;

        // Normalize movement
        float length = (float) Math.sqrt(dx*dx + dy*dy);
        if (length != 0) {
            dx /= length;
            dy /= length;
        }

        updateAnimationState(delta, dx, dy);

        x += dx * speed * delta;
        y += dy * speed * delta;

        x = MathUtils.clamp(x, 0, Gdx.graphics.getWidth() - width);
        y = MathUtils.clamp(y, 0, Gdx.graphics.getHeight() - height);

        bounds.setPosition(x, y);

        effects.update(delta, this);
    }

    protected void updateAnimationState(float delta, float dx, float dy) {
        moving = !MathUtils.isZero(dx, 0.001f) || !MathUtils.isZero(dy, 0.001f);
        if (dx < 0f) {
            facingLeft = true;
        } else if (dx > 0f) {
            facingLeft = false;
        }

        if (moving) {
            stateTime += delta;
        } else {
            stateTime = 0f;
        }
    }

    public StatusEffects getEffects() {
        return effects;
    }

    public void applyPoison(float durationSeconds) {
        effects.applyPoison(durationSeconds);
    }

    public void applyBurn(float durationSeconds) { effects.applyBurn(durationSeconds); }

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
    }

    public void setAnimationManager(FrameAnimationManager animationManager) {
        this.animationManager = animationManager;
    }

    public void setAnimationBasePath(String animationBasePath) {
        this.animationBasePath = animationBasePath;
    }

    protected TextureRegion getCurrentFrame() {
        if (animationManager == null || animationBasePath == null || animationBasePath.trim().isEmpty()) {
            return null;
        }

        FrameAnimation animation = animationManager.getAnimation(animationBasePath);
        return animation.getFrame(stateTime, moving);
    }

    protected void drawCurrentFrame(SpriteBatch batch, TextureRegion frame) {
        if (facingLeft) {
            batch.draw(frame, x + width, y, -width, height);
            return;
        }
        batch.draw(frame, x, y, width, height);
    }

    protected void drawCurrentTexture(SpriteBatch batch) {
        if (facingLeft) {
            batch.draw(texture, x + width, y, -width, height);
            return;
        }
        batch.draw(texture, x, y, width, height);
    }
}
