package com.example.rougelikegame.android.models.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;

public class Joystick {

    private final Texture base;
    private final Texture knob;
    private final float radius;

    private float baseX, baseY;
    private float knobX, knobY;

    private boolean active = false;
    private int activePointer = -1;

    public Joystick(String baseFile, String knobFile, float radius) {
        this.base = new Texture(baseFile);
        this.knob = new Texture(knobFile);
        this.radius = radius;
    }

    // ================= INPUT =================

    public void touchDown(float x, float y, int pointer) {

        // left half only
        if (x > Gdx.graphics.getWidth() / 2f) return;

        // already moving → ignore other fingers
        if (active) return;

        activePointer = pointer;

        baseX = x;
        baseY = y;
        knobX = x;
        knobY = y;
        active = true;
    }

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

    public void touchUp(int pointer) {
        if (pointer != activePointer) return;

        active = false;
        activePointer = -1;
    }

    // ================= OUTPUT =================

    public float getPercentX() {
        if (!active) return 0f;
        return (knobX - baseX) / radius;
    }

    public float getPercentY() {
        if (!active) return 0f;
        return (knobY - baseY) / radius;
    }

    public boolean isActive() {
        return active;
    }

    // ================= DRAW =================

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

    public void dispose() {
        base.dispose();
        knob.dispose();
    }
}
