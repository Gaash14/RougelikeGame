package com.example.rougelikegame.android.models;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;

public class Joystick extends Actor {
    private Texture base, knob;
    private float baseX, baseY, knobX, knobY, radius;
    private boolean touched = false;

    public Joystick(String baseFile, String knobFile, float x, float y, float radius) {
        this.base = new Texture(baseFile);
        this.knob = new Texture(knobFile);
        this.baseX = x;
        this.baseY = y;
        this.radius = radius;
        this.knobX = x;
        this.knobY = y;

        setBounds(x - radius, y - radius, radius * 2, radius * 2);

        addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                touched = true;
                updateKnob(x, y);
                return true;
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                if (touched) updateKnob(x, y);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                touched = false;
                knobX = baseX;
                knobY = baseY;
            }
        });
    }

    private void updateKnob(float x, float y) {
        float dx = x - baseX;
        float dy = y - baseY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        if (distance > radius) {
            dx = dx / distance * radius;
            dy = dy / distance * radius;
        }

        knobX = baseX + dx;
        knobY = baseY + dy;
    }

    public float getPercentX() {
        return (knobX - baseX) / radius;
    }

    public float getPercentY() {
        return (knobY - baseY) / radius;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.draw(base, baseX - radius, baseY - radius, radius * 2, radius * 2);
        batch.draw(knob, knobX - knob.getWidth() / 2f, knobY - knob.getHeight() / 2f);
    }
}
