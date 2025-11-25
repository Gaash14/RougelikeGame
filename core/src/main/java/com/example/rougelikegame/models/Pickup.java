package com.example.rougelikegame.models;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class Pickup {

    public enum Type {
        HEALTH,
        SPEED,
        DAMAGE,
        COIN
    }

    public Type type;
    public Texture texture;
    public float x, y;
    public Rectangle bounds;

    public Pickup(String texturePath, float x, float y, Type type) {
        this.texture = new Texture(texturePath);
        this.x = x;
        this.y = y;
        this.type = type;

        this.bounds = new Rectangle(x, y, 64, 64); // size of pickup
    }

    public void draw(SpriteBatch batch) {
        batch.draw(texture, x, y, 64, 64);
    }

    public void dispose() {
        texture.dispose();
    }
}
