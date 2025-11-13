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
    public float health;
    public Rectangle bounds;

    public Player(float x, float y) {
        texture = new Texture("player.png");
        this.x = x;
        this.y = y;
        this.width = 128;
        this.height = 128;
        this.health = 10;
        bounds = new Rectangle(x, y, width, height);
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
    }

    public void draw(SpriteBatch batch) {
        batch.draw(texture, x, y, width, height);
    }


    public void dispose() {
        texture.dispose();
    }

}
