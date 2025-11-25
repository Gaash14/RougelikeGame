package com.example.rougelikegame.screens;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.example.rougelikegame.models.Enemy;
import com.example.rougelikegame.models.Joystick;
import com.example.rougelikegame.models.Pickup;
import com.example.rougelikegame.models.Player;

import java.util.Random;

public class MainActivity extends ApplicationAdapter {
    SpriteBatch batch;

    Player player;
    Array<Enemy> enemies;
    Array<Pickup> pickups;

    Random rnd;
    BitmapFont font;

    private Stage stage;
    private Joystick joystick;
    private OrthographicCamera camera;

    Texture attackBtnTexture;
    Rectangle attackBtnBounds;

    @Override
    public void create() {
        batch = new SpriteBatch();

        setupCamera();
        setupPlayerAndEnemies();
        setupStageAndJoystick();
        setupAttackButton();
        setupInput();
        setupHealthbar();
    }


    private void setupCamera() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    private void setupPlayerAndEnemies() {
        player = new Player(100, 100);
        rnd = new Random();
        enemies = new Array<>();

        int numEnemies = 5;
        for (int i = 0; i < numEnemies; i++) {
            float x = rnd.nextInt(Gdx.graphics.getWidth() - 128);
            float y = rnd.nextInt(Gdx.graphics.getHeight() - 128);
            enemies.add(new Enemy("enemy.png", x, y, 100, 128, 128));
        }

        pickups = new Array<>();
        // spawn some pickups
        pickups.add(new Pickup("health_pickup.png", 400, 400, Pickup.Type.HEALTH));
        //pickups.add(new Pickup("speed_pickup.png", 800, 300, Pickup.Type.SPEED));
        //pickups.add(new Pickup("coin.png", 600, 200, Pickup.Type.COIN));

    }

    private void setupStageAndJoystick() {
        stage = new Stage(new ScreenViewport());

        joystick = new Joystick(
            "joystick_base.png",
            "joystick_knob.png",
            150, 150,
            100
        );

        stage.addActor(joystick);
    }

    private void setupAttackButton() {
        attackBtnTexture = new Texture("attack_icon.png");
        attackBtnBounds = new Rectangle(
            Gdx.graphics.getWidth() - 250,
            50,
            200,
            200
        );
    }

    private void setupInput() {
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(stage);
        multiplexer.addProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {

                Vector3 touch = new Vector3(screenX, screenY, 0);
                camera.unproject(touch);

                if (attackBtnBounds.contains(touch.x, touch.y)) {

                    player.attack(joystick);

                    // Reset hit flags for this swing
                    for (Enemy e : enemies) {
                        e.hitThisSwing = false;
                    }

                    return true;
                }

                return false;
            }
        });

        Gdx.input.setInputProcessor(multiplexer);
    }

    private void setupHealthbar() {
        font = new BitmapFont();               // default white font
        font.getData().setScale(5.0f);         // make it bigger
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();

        ScreenUtils.clear(0.3f, 0.3f, 0.3f, 1);

        updateGame(delta);
        checkPickups();
        preventOverlapping();
        handlePlayerEnemyCollision();
        handleAttackDamage();
        cleanupDeadEnemies();

        drawGame();
    }

    private void updateGame(float delta) {
        player.update(joystick, delta);

        for (Enemy e : enemies) {
            e.update(delta, player.x, player.y);
        }
    }

    private void checkPickups() {
        for (int i = pickups.size - 1; i >= 0; i--) {
            Pickup p = pickups.get(i);

            if (player.bounds.overlaps(p.bounds)) {

                applyPickupEffect(p);
                pickups.removeIndex(i);
            }
        }
    }

    private void preventOverlapping() {
        for (int i = 0; i < enemies.size; i++) {
            Enemy a = enemies.get(i);

            for (int j = i + 1; j < enemies.size; j++) {
                Enemy b = enemies.get(j);

                float dx = b.getX() - a.getX();
                float dy = b.getY() - a.getY();

                float dist = (float)Math.sqrt(dx*dx + dy*dy);
                float minDist = a.width;

                if (dist < minDist && dist > 0) {

                    float overlap = minDist - dist;

                    // normalize
                    dx /= dist;
                    dy /= dist;

                    // push each enemy half the overlap
                    a.setX(a.getX() - dx * overlap * 0.5f);
                    a.setY(a.getY() - dy * overlap * 0.5f);

                    b.setX(b.getX() + dx * overlap * 0.5f);
                    b.setY(b.getY() + dy * overlap * 0.5f);
                }
            }
        }
    }

    private void handlePlayerEnemyCollision() {
        for (Enemy e : enemies) {
            if (e.getBounds().overlaps(player.bounds)) {

                if (player.damageCooldown <= 0) {

                    // Apply damage
                    player.health--;
                    player.damageCooldown = player.damageCooldownTime;

                    // Compute knockback direction
                    float dx = player.x - e.getX();;
                    float dy = player.y - e.getY();;
                    float len = (float)Math.sqrt(dx*dx + dy*dy);
                    if (len != 0) {
                        dx /= len;
                        dy /= len;
                    }

                    // Apply knockback
                    player.knockbackX = dx;
                    player.knockbackY = dy;
                    player.knockbackTime = player.knockbackDuration;

                    System.out.println("Player hit! HP = " + player.health);
                }
            }
        }
    }

    private void handleAttackDamage() {
        if (!player.attacking) return;

        for (Enemy e : enemies) {

            if (!e.hitThisSwing && e.getBounds().overlaps(player.attackHitbox)) {

                e.takeDamage(10 + player.attackBonus);
                e.hitThisSwing = true;

                // knockback enemy away from player
                float dx = e.getX() - player.x;
                float dy = e.getY() - player.y;

                // normalize
                float length = (float)Math.sqrt(dx*dx + dy*dy);
                if (length != 0) {
                    dx /= length;
                    dy /= length;
                }

                // knockback strength
                float knockback = 160f;

                e.setX(e.getX() + dx * knockback);
                e.setY(e.getY() + dy * knockback);

                System.out.println("HIT! Enemy HP = " + e.health); // debug
            }
        }
    }

    private void cleanupDeadEnemies() {
        for (int i = enemies.size - 1; i >= 0; i--) {
            if (!enemies.get(i).alive) {
                enemies.removeIndex(i);
            }
        }
    }

    private void drawGame() {
        batch.begin();

        player.draw(batch);

        for (Enemy e : enemies) {
            e.draw(batch);
        }

        for (Pickup p : pickups) {
            p.draw(batch);
        }

        batch.draw(
            attackBtnTexture,
            attackBtnBounds.x, attackBtnBounds.y,
            attackBtnBounds.width, attackBtnBounds.height
        );

        drawDebugAttackHitbox(); // remove later (change to actual animation)

        font.draw(batch, "HP: " + player.health, 20, Gdx.graphics.getHeight() - 20);

        batch.end();

        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    private void drawDebugAttackHitbox() {
        if (!player.attacking) return;

        batch.setColor(1, 0, 0, 0.4f);
        batch.draw(
            player.debugPixel,
            player.attackHitbox.x,
            player.attackHitbox.y,
            player.attackHitbox.width,
            player.attackHitbox.height
        );
        batch.setColor(1, 1, 1, 1);
    } // remove later (change to actual animation)

    private void applyPickupEffect(Pickup p) {

        switch (p.type) {

            case HEALTH:
                player.health += 5;
                System.out.println("Picked up health → player HP = " + player.health);
                break;

            case SPEED:
                player.speed += 100;  // temporary buff
                System.out.println("Picked up speed → new speed = " + player.speed);
                break;

            case DAMAGE:
                player.attackBonus += 5; // you can add attackBonus to player
                System.out.println("Picked up damage → new bonus = " + player.attackBonus);
                break;

            case COIN:
                player.coins++;
                System.out.println("Picked up coin → coins = " + player.coins);
                break;
        }
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
