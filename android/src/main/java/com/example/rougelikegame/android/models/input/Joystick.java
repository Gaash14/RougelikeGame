package com.example.rougelikegame.android.models.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;

/**
 * The Joystick class provides a virtual on-screen joystick for player movement.
 */
public class Joystick {

    private final Texture base;
    private final Texture knob;
    private final float radius;

    private float baseX, baseY;
    private float knobX, knobY;

    private boolean active = false;
    private int activePointer = -1;

    /**
     * Constructs a Joystick with the specified textures and radius.
     *
     * @param baseFile the path to the joystick base texture
     * @param knobFile the path to the joystick knob texture
     * @param radius   the maximum distance the knob can move from the base
     */
    public Joystick(String baseFile, String knobFile, float radius) {
        this.base = new Texture(baseFile);
        this.knob = new Texture(knobFile);
        this.radius = radius;
    }

    /**
     * Handles the touch down event for the joystick.
     *
     * @param x       the X coordinate of the touch
     * @param y       the Y coordinate of the touch
     * @param pointer the pointer index
     */
    public void touchDown(float x, float y, int pointer) {
        // Only allow joystick activation on the left half of the screen
        if (x > Gdx.graphics.getWidth() / 2f) return;

        // If already active, ignore other fingers
        if (active) return;

        activePointer = pointer;
        baseX = x;
        baseY = y;
        knobX = x;
        knobY = y;
        active = true;
    }

    /**
     * Handles the touch dragged event for the joystick.
     *
     * @param x       the X coordinate of the touch
     * @param y       the Y coordinate of the touch
     * @param pointer the pointer index
     */
    public void touchDragged(float x, float y, int pointer) {
        if (!active || pointer != activePointer) return;

        float dx = x - baseX;
        float dy = y - baseY;

        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        if (dist > radius) {
            dx = dx / dist * radius;
            dy = dy / dist * radius;
        }

        knobX = baseX + dx;
        knobY = baseY + dy;
    }

    /**
     * Handles the touch up event for the joystick.
     *
     * @param pointer the pointer index
     */
    public void touchUp(int pointer) {
        if (pointer != activePointer) return;
        reset();
    }

    /**
     * @return the horizontal displacement as a percentage of the radius (-1.0 to 1.0)
     */
    public float getPercentX() {
        if (!active) return 0f;
        return (knobX - baseX) / radius;
    }

    /**
     * @return the vertical displacement as a percentage of the radius (-1.0 to 1.0)
     */
    public float getPercentY() {
        if (!active) return 0f;
        return (knobY - baseY) / radius;
    }

    /**
     * @return true if the joystick is currently active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Resets the joystick to its inactive state.
     */
    public void reset() {
        active = false;
        activePointer = -1;
        knobX = baseX;
        knobY = baseY;
    }

    /**
     * Draws the joystick using the provided SpriteBatch.
     *
     * @param batch the SpriteBatch to draw with
     */
    public void draw(Batch batch) {
        if (!active) return;

        batch.draw(
            base,
            baseX - radius,
            baseY - radius,
            radius * 2,
            radius * 2
        );

        batch.draw(
            knob,
            knobX - knob.getWidth() / 2f,
            knobY - knob.getHeight() / 2f
        );
    }

    /**
     * Disposes of the joystick textures.
     */
    public void dispose() {
        base.dispose();
        knob.dispose();
    }
}
