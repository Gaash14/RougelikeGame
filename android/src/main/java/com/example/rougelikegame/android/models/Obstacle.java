package com.example.rougelikegame.android.models;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class Obstacle {

    public Texture texture;
    public float x, y;
    public Rectangle bounds;

    public Obstacle(float x, float y) {
        this.texture = new Texture("obstacle.png");
        this.x = x;
        this.y = y;
        this.bounds = new Rectangle(x, y, 96, 96); // obstacle size
    }

    public void draw(SpriteBatch batch) {
        batch.draw(texture, x, y, 128, 128);
    }

    public void dispose() {
        texture.dispose();
    }
}
