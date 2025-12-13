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
import com.example.rougelikegame.ScoreReporter;
import com.example.rougelikegame.models.BossEnemy;
import com.example.rougelikegame.models.Enemy;
import com.example.rougelikegame.models.Joystick;
import com.example.rougelikegame.models.Obstacle;
import com.example.rougelikegame.models.Pickup;
import com.example.rougelikegame.models.Player;

import java.util.Random;

public class MainActivity extends ApplicationAdapter {

    private final ScoreReporter scoreReporter;
    public MainActivity(ScoreReporter scoreReporter) {
        this.scoreReporter = scoreReporter;
    }
    public MainActivity() {
        this.scoreReporter = null;
    }

    SpriteBatch batch;

    Player player;
    Array<Enemy> enemies;
    Array<Pickup> pickups;
    Array<Obstacle> obstacles;

    Random rnd;
    BitmapFont font;

    private Stage stage;
    private Joystick joystick;
    private OrthographicCamera camera;

    Texture attackBtnTexture;
    Rectangle attackBtnBounds;

    private float runTime = 0f;
    private boolean runEnded = false;

    int wave = 1;
    private final int BOSS_WAVE = 3;
    float timeBetweenWaves = 2f;   // seconds delay before next wave
    float waveTimer = 0f;
    boolean waitingForNextWave = false;

    @Override
    public void create() {
        batch = new SpriteBatch();

        setupCamera();
        setupPlayerAndEnemies();
        setupPickupsAndObstacles();
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

        spawnWave(wave);
    }

    private void setupPickupsAndObstacles() {
        pickups = new Array<>();

        obstacles = new Array<>();
        spawnRandomObstacles();
    }

    private void spawnRandomObstacles() {
        obstacles.clear();

        int numObstacles = 5;

        for (int i = 0; i < numObstacles; i++) {

            float x = rnd.nextInt(Gdx.graphics.getWidth() - 128);
            float y = rnd.nextInt(Gdx.graphics.getHeight() - 128);

            // Make sure they don't spawn on the player
            if (Math.abs(x - player.x) < 200 && Math.abs(y - player.y) < 200) {
                i--;
                continue;
            }

            obstacles.add(new Obstacle(x, y));
        }
    }

    private void spawnWave(int waveNumber) {
        // boss enemy spawn
        if (wave == BOSS_WAVE) {
            float bossWidth = 256;
            float bossHeight = 256;

            float x = Gdx.graphics.getWidth() / 2f - bossWidth / 2f;
            float y = Gdx.graphics.getHeight() / 2f - bossHeight / 2f;

            enemies.add(new BossEnemy(x, y));
            return;
        }

        // normal enemy spawn
        int enemyCount = 3 + (waveNumber - 1) * 2; // wave 1 = 3, wave 2 = 5, wave 3 = 7...

        Random rnd = new Random();

        for (int i = 0; i < enemyCount; i++) {
            float x = rnd.nextInt(Gdx.graphics.getWidth() - 128);
            float y = rnd.nextInt(Gdx.graphics.getHeight() - 128);

            enemies.add(new Enemy("enemy.png", x, y, 100, 128, 128));
        }

        System.out.println("Spawned wave " + waveNumber + " with " + enemyCount + " enemies");
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
        if (!runEnded) runTime += delta;

        ScreenUtils.clear(0.3f, 0.3f, 0.3f, 1);

        updateGame(delta);
        checkPickups();
        preventOverlapping(delta);
        handlePlayerEnemyCollision();
        handleAttackDamage();
        cleanupDeadEnemies(delta);

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

    private void preventOverlapping(float delta) {
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

        player.handleObstacleCollision(obstacles);

        for (Enemy e : enemies) {
            e.update(delta, player.x, player.y);
            e.handleObstacleCollision(obstacles);
        }
    }

    private void handlePlayerEnemyCollision() {
        for (Enemy e : enemies) {
            if (e.getBounds().overlaps(player.bounds)) {

                if (player.damageCooldown <= 0) {

                    // Apply damage
                    int dmg;
                    if (e.isBoss) {
                        dmg = 2;
                    } else {
                        dmg = 1;
                    }
                    player.health -= dmg;

                    if (player.health <= 0) { onPlayerDied(); }

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

    private void onPlayerDied() {
        if (scoreReporter != null) {
            scoreReporter.saveHighestWave(wave);
        }

        // Quit the LibGDX game (closes AndroidLauncher and returns to previous Activity)
        Gdx.app.postRunnable(() -> Gdx.app.exit());
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

    private void cleanupDeadEnemies(float delta) {
        for (int i = enemies.size - 1; i >= 0; i--) {
            Enemy dead = enemies.get(i);
            if (!dead.alive) {
                enemies.removeIndex(i);

                if (dead.isBoss) {
                    onBossDefeat();
                }
            }
        }

        // wave progression logic
        if (enemies.size == 0 && !waitingForNextWave) {
            waitingForNextWave = true;
            waveTimer = timeBetweenWaves;   // start countdown
        }

        if (waitingForNextWave) {
            waveTimer -= delta;

            if (waveTimer <= 0) {
                wave++;
                spawnWave(wave);
                spawnWavePickups(wave);   // <-- NEW
                waitingForNextWave = false;
            }
        }
    }

    private void spawnWavePickups(int wave) {
        int numPickups = 2 + wave / 2;

        for (int i = 0; i < numPickups; i++) {
            float x = rnd.nextInt(Gdx.graphics.getWidth() - 64);
            float y = rnd.nextInt(Gdx.graphics.getHeight() - 64);

            Pickup.Type randomType = Pickup.Type.values()[rnd.nextInt(Pickup.Type.values().length)];
            pickups.add(new Pickup(randomType, x, y));
        }
    }

    private void drawGame() {
        batch.begin();

        player.draw(batch);

        for (Enemy e : enemies) {
            e.draw(batch);
        }

        for (Obstacle o : obstacles) {
            o.draw(batch);
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

        drawUI();

        batch.end();

        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    private void drawUI() {
        font.draw(batch, "HP: " + player.health, 20, Gdx.graphics.getHeight() - 20);
        font.draw(batch, "Wave: " + wave, 20, Gdx.graphics.getHeight() - 80);
        font.draw(batch, "Damage: " + (10 + player.attackBonus), 20, Gdx.graphics.getHeight() - 130);
        font.draw(batch, "Speed: " + player.speed, 20, Gdx.graphics.getHeight() - 180);
        font.draw(batch, "Coins: " + player.coins, 20, Gdx.graphics.getHeight() - 230);
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
                player.speed += 50;
                System.out.println("Picked up speed → new speed = " + player.speed);
                break;

            case DAMAGE:
                player.attackBonus += 5;
                System.out.println("Picked up damage → new bonus = " + player.attackBonus);
                break;

            case COIN:
                player.coins++;
                System.out.println("Picked up coin → coins = " + player.coins);
                break;
        }
    }

    private void onBossDefeat() {
        if (scoreReporter != null) {
            // final wave reached
            scoreReporter.saveHighestWave(wave);

            // runTime assumed to be in seconds (float) – cast to int
            scoreReporter.saveBestTime((int) runTime);
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        for (Enemy e : enemies) {
            e.dispose();
        }
        player.dispose();
        font.dispose();
        stage.dispose();
    }
}
