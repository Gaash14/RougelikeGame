package com.example.rougelikegame.android.models.input;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.example.rougelikegame.android.models.characters.Enemy;
import com.example.rougelikegame.android.models.characters.Player;
import com.example.rougelikegame.android.models.items.PassiveItem;
import com.example.rougelikegame.android.models.items.passives.BeamItem;

/**
 * The GameInputController class handles all user input during gameplay,
 * including movement via joystick, attacks, and menu interactions.
 */
public class GameInputController {

    /**
     * Interface for spawning projectiles and beams based on input.
     */
    public interface ProjectileSpawner {
        void spawnProjectile(float x, float y, Vector2 dir, int damage);
        void spawnBeam(Vector2 dir, float chargePercent);
    }

    private final OrthographicCamera camera;
    private final Stage stage;
    private final Joystick joystick;

    private final Player player;
    private final Array<Enemy> enemies;

    private final Rectangle attackBtnBounds;
    private final Rectangle resumeBounds;
    private final Rectangle exitBounds;
    private final Rectangle adminBtnBounds;

    private final ProjectileSpawner projectileSpawner;
    private final Runnable onExitRequested;
    private final Runnable toggleAdminConsole;

    private boolean paused = false;
    private int attackPointer = -1;
    private boolean rangedAttackHeld = false;

    /**
     * Constructs a GameInputController with the necessary game components and bounds.
     */
    public GameInputController(
        OrthographicCamera camera,
        Stage stage,
        Joystick joystick,
        Player player,
        Array<Enemy> enemies,
        Rectangle attackBtnBounds,
        Rectangle resumeBounds,
        Rectangle exitBounds,
        Rectangle adminBtnBounds,
        ProjectileSpawner projectileSpawner,
        Runnable onExitRequested,
        Runnable toggleAdminConsole
    ) {
        this.camera = camera;
        this.stage = stage;
        this.joystick = joystick;
        this.player = player;
        this.enemies = enemies;
        this.attackBtnBounds = attackBtnBounds;
        this.resumeBounds = resumeBounds;
        this.exitBounds = exitBounds;
        this.adminBtnBounds = adminBtnBounds;
        this.projectileSpawner = projectileSpawner;
        this.onExitRequested = onExitRequested;
        this.toggleAdminConsole = toggleAdminConsole;
    }

    /**
     * Sets the paused state of the game input.
     *
     * @param paused true to pause input handling, false to resume
     */
    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    /**
     * @return true if the game is currently paused
     */
    public boolean isPaused() {
        return paused;
    }

    /**
     * Resets the input state for gameplay.
     */
    public void resetGameplayInputState() {
        joystick.reset();
        attackPointer = -1;
        rangedAttackHeld = false;
        player.cancelCharge();
    }

    /**
     * Updates the input controller state.
     *
     * @param delta the time elapsed since the last frame
     */
    public void update(float delta) {
        if (paused) return;

        if (rangedAttackHeld && player.playerClass == Player.PlayerClass.RANGED) {
            fireRangedShots();
        }
    }

    /**
     * Builds an InputMultiplexer that includes the stage and custom gameplay input handling.
     *
     * @return the configured InputMultiplexer
     */
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

                // ---------------- ADMIN BUTTON ----------------
                if (paused && adminBtnBounds != null && adminBtnBounds.contains(touch.x, touch.y)) {
                    if (toggleAdminConsole != null) {
                        toggleAdminConsole.run();
                        return true;
                    }
                }

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
                    if (hasBeamItem()) {
                        attackPointer = pointer;
                        player.startCharge();
                        return true;
                    }

                    if (player.playerClass == Player.PlayerClass.MELEE) {

                        if (player.canMelee()) {
                            for (Enemy e : enemies) {
                                e.hitThisSwing = false;
                            }
                            player.meleeAttack(joystick, enemies);
                        }

                    } else if (player.playerClass == Player.PlayerClass.RANGED) {

                        attackPointer = pointer;
                        rangedAttackHeld = true;
                        fireRangedShots();
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

                if (pointer == attackPointer) {
                    attackPointer = -1;
                    if (hasBeamItem()) {
                        float chargePercent = player.releaseChargePercent();

                        if (chargePercent >= (0.8f / 1.2f)) {
                            Vector2 dir = player.getShootDirection(joystick);
                            projectileSpawner.spawnBeam(dir, chargePercent);
                        }
                    }
                    rangedAttackHeld = false;
                }

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

    /**
     * Fires ranged projectiles while the player is able to shoot.
     */
    private void fireRangedShots() {
        while (player.canShoot()) {
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

    /**
     * Checks if the player has the BeamItem.
     *
     * @return true if the player has the BeamItem, false otherwise
     */
    private boolean hasBeamItem() {
        for (PassiveItem item : player.getPassiveItems()) {
            if (item.getItemId() == BeamItem.ID) {
                return true;
            }
        }
        return false;
    }
}
