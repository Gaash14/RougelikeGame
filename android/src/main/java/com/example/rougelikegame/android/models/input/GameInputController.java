package com.example.rougelikegame.android.models.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.example.rougelikegame.android.models.characters.Enemy;
import com.example.rougelikegame.android.models.characters.Player;
import com.example.rougelikegame.android.models.world.Projectile;

public class GameInputController {

    public interface ProjectileSpawner {
        void spawnProjectile(float x, float y, Vector2 dir, int damage);
    }

    private final OrthographicCamera camera;
    private final Stage stage;
    private final Joystick joystick;

    private final Player player;
    private final Array<Enemy> enemies;

    private final Rectangle attackBtnBounds;
    private final Rectangle resumeBounds;
    private final Rectangle exitBounds;

    private final ProjectileSpawner projectileSpawner;
    private final Runnable onExitRequested;

    private boolean paused = false;

    public GameInputController(
        OrthographicCamera camera,
        Stage stage,
        Joystick joystick,
        Player player,
        Array<Enemy> enemies,
        Rectangle attackBtnBounds,
        Rectangle resumeBounds,
        Rectangle exitBounds,
        ProjectileSpawner projectileSpawner,
        Runnable onExitRequested
    ) {
        this.camera = camera;
        this.stage = stage;
        this.joystick = joystick;
        this.player = player;
        this.enemies = enemies;
        this.attackBtnBounds = attackBtnBounds;
        this.resumeBounds = resumeBounds;
        this.exitBounds = exitBounds;
        this.projectileSpawner = projectileSpawner;
        this.onExitRequested = onExitRequested;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public boolean isPaused() {
        return paused;
    }

    public InputMultiplexer buildProcessor() {
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(stage);
        multiplexer.addProcessor(new InputAdapter() {

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                Vector3 touch = new Vector3(screenX, screenY, 0);
                camera.unproject(touch);

                // JOYSTICK
                joystick.touchDown(touch.x, touch.y, pointer);

                // ---------------- PAUSE MENU ----------------
                if (paused) {
                    if (resumeBounds.contains(touch.x, touch.y)) {
                        paused = false;
                        return true;
                    }

                    if (exitBounds.contains(touch.x, touch.y)) {
                        onExitRequested.run();
                        return true;
                    }

                    return true;
                }

                // ---------------- ATTACK BUTTON ----------------
                if (attackBtnBounds.contains(touch.x, touch.y)) {

                    if (player.playerClass == Player.PlayerClass.MELEE) {

                        player.meleeAttack(joystick);

                        for (Enemy e : enemies) {
                            e.hitThisSwing = false;
                        }

                    } else if (player.playerClass == Player.PlayerClass.RANGED) {

                        if (player.canShoot()) {
                            Vector2 dir = player.getShootDirection(joystick);

                            projectileSpawner.spawnProjectile(
                                player.x + player.width / 2f,
                                player.y + player.height / 2f,
                                dir,
                                player.getCurrentDamage()
                            );

                            player.triggerRangedCooldown();
                        }
                    }

                    return true;
                }

                return true;
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                Vector3 touch = new Vector3(screenX, screenY, 0);
                camera.unproject(touch);

                joystick.touchDragged(touch.x, touch.y, pointer);
                return true;
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                joystick.touchUp(pointer);
                return true;
            }

            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.BACK) {
                    paused = !paused;
                    return true;
                }
                return false;
            }
        });

        return multiplexer;
    }
}
