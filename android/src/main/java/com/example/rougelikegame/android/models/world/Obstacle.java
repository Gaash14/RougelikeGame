package com.example.rougelikegame.android.models.world;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

/**
 * Obstacles provide physical barriers that characters cannot pass through.
 */
public class Obstacle {

    public Texture texture;
    public float x;
    public float y;
    public Rectangle bounds;

    /**
     * Constructs a new Obstacle at the specified position.
     * @param x The x-coordinate.
     * @param y The y-coordinate.
     */
    public Obstacle(float x, float y) {
        this.texture = new Texture("obstacle.png");
        this.x = x;
        this.y = y;
        this.bounds = new Rectangle(x, y, 96, 96); // obstacle size
    }

    /**
     * Draws the obstacle using the provided SpriteBatch.
     * @param batch The SpriteBatch to draw with.
     */
    public void draw(SpriteBatch batch) {
        batch.draw(texture, x, y, 128, 128);
    }

    /**
     * Disposes of the obstacle's resources.
     */
    public void dispose() {
        if (texture != null) {
            texture.dispose();
        }
    }
}
