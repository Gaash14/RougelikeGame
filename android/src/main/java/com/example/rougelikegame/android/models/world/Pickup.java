package com.example.rougelikegame.android.models.world;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

/**
 * Pickups provide various benefits to the player when collected, such as health or coins.
 */
public class Pickup {

    /**
     * Defines the types of pickups available.
     */
    public enum Type {
        HEALTH,
        SPEED,
        COIN
    }

    public Type type;
    public Texture texture;
    public float x;
    public float y;
    public Rectangle bounds;

    /**
     * Constructs a new Pickup of the specified type at the given position.
     * @param type The type of the pickup.
     * @param x The x-coordinate.
     * @param y The y-coordinate.
     */
    public Pickup(Type type, float x, float y) {
        this.type = type;
        this.x = x;
        this.y = y;

        // Choose texture based on pickup type
        switch (type) {
            case HEALTH:
                texture = new Texture("pickups/pickup_health.png");
                break;
            case SPEED:
                texture = new Texture("pickups/pickup_speed.png");
                break;
            case COIN:
                texture = new Texture("pickups/pickup_coin.png");
                break;
            default:
                break;
        }

        bounds = new Rectangle(x, y, 128, 128); // size of pickup
    }

    /**
     * Draws the pickup using the provided SpriteBatch.
     * @param batch The SpriteBatch to draw with.
     */
    public void draw(SpriteBatch batch) {
        if (texture != null) {
            batch.draw(texture, x, y, 128, 128);
        }
    }

    /**
     * Disposes of the pickup's resources.
     */
    public void dispose() {
        if (texture != null) {
            texture.dispose();
        }
    }
}
