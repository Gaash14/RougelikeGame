package com.example.rougelikegame.screens;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.example.rougelikegame.models.Enemy;
import com.example.rougelikegame.models.Joystick;
import com.example.rougelikegame.models.Player;

import java.util.Random;

public class MainActivity extends ApplicationAdapter {
    SpriteBatch batch;

    Player player;
    Array<Enemy> enemies;

    Random rnd;

    private Stage stage;
    private Joystick joystick;

    @Override
    public void create() {
        batch = new SpriteBatch();

        player = new Player(100, 100);
        rnd = new Random();
        enemies = new Array<>();

        int numEnemies = 5;

        for (int i = 0; i < numEnemies; i++) {
            float x = rnd.nextInt(Gdx.graphics.getWidth() - 128);
            float y = rnd.nextInt(Gdx.graphics.getHeight() - 128);
            enemies.add(new Enemy("enemy.png", x, y, 100, 128, 128));
        }

        // --- Joystick setup ---
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        joystick = new Joystick("joystick_base.png", "joystick_knob.png", 150, 150, 100);
        stage.addActor(joystick);
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();

        ScreenUtils.clear(0.3f, 0.3f, 0.3f, 1);

        // Update player movement based on joystick input
        player.update(joystick, delta);

        for (Enemy e : enemies) {
            e.update(delta, player.x, player.y);
        }

        for (Enemy e : enemies) {
            if (e.getBounds().overlaps(player.bounds)) {
                player.health--;
                player.x = 100;
                player.y = 100;
            }
        }

        batch.begin();

        player.draw(batch);

        for (Enemy e : enemies) {
            e.draw(batch);
        }

        batch.end();

        // draw joystick
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void dispose() {
        batch.dispose();

        for (Enemy e : enemies) {
            e.dispose();
        }

        player.dispose();

        stage.dispose();
    }
}

