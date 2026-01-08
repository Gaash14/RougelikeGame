package com.example.rougelikegame.android.models.world;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Projectile {
    public float x, y;
    public float speed = 800f;
    public int damage;
    public boolean alive = true;

    private float life = 1.2f; // seconds
    private final Vector2 dir = new Vector2();
    private final Rectangle bounds = new Rectangle();

    private static Texture tex; // shared texture

    public Projectile(float x, float y, float dirX, float dirY, int damage) {
        this.damage = damage;
        this.x = x;
        this.y = y;
        this.dir.set(dirX, dirY).nor();

        if (tex == null) tex = new Texture("pixel.png");

        bounds.set(x, y, 16, 16);
    }

    public void update(float delta) {
        x += dir.x * speed * delta;
        y += dir.y * speed * delta;

        bounds.setPosition(x, y);

        life -= delta;
        if (life <= 0) alive = false;
    }

    public void draw(Batch batch) {
        batch.draw(tex, x, y, 16, 16);
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public static void disposeTexture() {
        if (tex != null) {
            tex.dispose();
            tex = null;
        }
    }
}
