package com.example.rougelikegame.android.models.game;

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
            case DAMAGE:
                texture = new Texture("pickups/pickup_damage.png");
                break;
            case COIN:
                texture = new Texture("pickups/pickup_coin.png");
                break;
        }

        bounds = new Rectangle(x, y, 128, 128); // size of pickup
    }

    public void draw(SpriteBatch batch) {
        batch.draw(texture, x, y, 128, 128);
    }

    public void dispose() {
        texture.dispose();
    }
}
