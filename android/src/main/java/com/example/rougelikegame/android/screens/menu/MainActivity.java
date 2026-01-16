package com.example.rougelikegame.android.screens.menu;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.example.rougelikegame.android.managers.AchievementManager;
import com.example.rougelikegame.android.models.core.ScoreReporter;
import com.example.rougelikegame.android.models.characters.BossEnemy;
import com.example.rougelikegame.android.models.characters.Enemy;
import com.example.rougelikegame.android.models.characters.GhostEnemy;
import com.example.rougelikegame.android.models.input.Joystick;
import com.example.rougelikegame.android.models.world.Obstacle;
import com.example.rougelikegame.android.models.world.Pickup;
import com.example.rougelikegame.android.models.characters.Player;
import com.example.rougelikegame.android.models.world.Projectile;

import java.util.Random;

public class MainActivity extends ApplicationAdapter {

    // Constructors (for AndroidLauncher / tests)
    private final ScoreReporter scoreReporter;
    private final Player.PlayerClass selectedClass;
    private final Player.Difficulty difficulty;
    private final String skinId;

    public MainActivity(
            ScoreReporter scoreReporter,
            Player.PlayerClass selectedClass,
            Player.Difficulty difficulty,
            String skinId
    ) {
        this.scoreReporter = scoreReporter;
        this.selectedClass = selectedClass;
        this.difficulty = difficulty;
        this.skinId = skinId;
    }
    public MainActivity(ScoreReporter scoreReporter) {
        this(scoreReporter, Player.PlayerClass.MELEE, Player.Difficulty.NORMAL, "default");
    }
    public MainActivity() {
        this(null, Player.PlayerClass.MELEE, Player.Difficulty.NORMAL, "default");
    }
    private boolean runReported = false;

    // LibGDX objects
    SpriteBatch batch;
    OrthographicCamera camera;
    Stage stage;
    Joystick joystick;
    BitmapFont font;

    // Game objects
    Player player;
    Array<Enemy> enemies;
    Array<Pickup> pickups;
    Array<Obstacle> obstacles;
    Array<Projectile> playerProjectiles = new Array<>();
    Array<Projectile> enemyProjectiles = new Array<>();

    // Game state
    Random rnd;
    public Player.Difficulty getDifficulty() { return difficulty; }

    private final AchievementManager achievementManager =
        AchievementManager.getInstance();

    int wave = 1;
    private static final int BOSS_WAVE = 7;

    float timeBetweenWaves = 2f; // seconds delay before next wave
    float waveTimer = 0f;
    boolean waitingForNextWave = false;

    private float runTime = 0f;
    private boolean bossDefeated = false;
    private int enemiesKilled = 0, pickupsPicked = 0;

    // Attack button
    Texture attackBtnTexture;
    Rectangle attackBtnBounds;

    // LibGDX
    @Override
    public void create() {
        batch = new SpriteBatch();
        rnd = new Random();

        setupCamera();
        setupPlayerAndEnemies();
        setupPickupsAndObstacles();
        setupStageAndJoystick();
        setupAttackButton();
        setupInput();
        setupFont();
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        if (!bossDefeated) {
            runTime += delta;
        }

        ScreenUtils.clear(0.3f, 0.3f, 0.3f, 1);

        update(delta);
        drawGame();
    }

    @Override
    public void dispose() {
        batch.dispose();
        for (Enemy e : enemies) {
            e.dispose();
        }
        for (Pickup p : pickups) {
            p.dispose();
        }
        for (Obstacle o : obstacles) {
            o.dispose();
        }
        Projectile.disposeTexture();

        player.dispose();
        font.dispose();
        stage.dispose();
        attackBtnTexture.dispose();
    }

    // Setup methods
    private void setupCamera() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    private void setupPlayerAndEnemies() {
        player = new Player(100, 100);
        player.setTexture(getPlayerTextureForSkin());
        player.playerClass = selectedClass;
        enemies = new Array<>();
        spawnWave(wave);
    }

    private Texture getPlayerTextureForSkin() {
        switch (skinId) {
            case "red":
                return new Texture("skins/player_red.png");
            case "shadow":
                return new Texture("skins/player_shadow.png");
            case "red_knight":
                return new Texture("skins/red_knight.png");
            case "angel":
                return new Texture("skins/player_angel.png");
            case "skin_wave_5":
                return new Texture("skins/wave_5.png");
            case "skin_wave_10":
                return new Texture("skins/wave_10.png");
            case "skin_100_wins":
                return new Texture("skins/100_kills.png");
            case "skin_200_coins":
                return new Texture("skins/200_coins.png");
            case "skin_first_win":
                return new Texture("skins/first_win.png");
            default:
                return new Texture("skins/player_default.png");
        }
    }

    private void setupPickupsAndObstacles() {
        pickups = new Array<>();
        obstacles = new Array<>();
        spawnRandomObstacles();
    }

    private void setupStageAndJoystick() {
        stage = new Stage(new ScreenViewport());

        joystick = new Joystick(
            "inputs/joystick_base.png",
            "inputs/joystick_knob.png",
            150, 150,
            100
        );

        stage.addActor(joystick);
    }

    private void setupAttackButton() {
        attackBtnTexture = new Texture("inputs/attack_icon.png");
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

                    if (player.playerClass == Player.PlayerClass.MELEE) {

                        player.meleeAttack(joystick);

                        // Reset hit flags for this swing
                        for (Enemy e : enemies) {
                            e.hitThisSwing = false;
                        }

                    } else if (player.playerClass == Player.PlayerClass.RANGED) {

                        if (player.canShoot()) {
                            Vector2 dir = player.getShootDirection(joystick);

                            spawnProjectile(
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

                return false;
            }
        });

        Gdx.input.setInputProcessor(multiplexer);
    }

    private void setupFont() {
        font = new BitmapFont();
        font.getData().setScale(5.0f);
    }

    // Update loop
    private void update(float delta) {
        updateGame(delta);

        updatePlayerProjectiles(delta);
        handlePlayerProjectileHits();

        updateEnemyProjectiles(delta);
        handleEnemyProjectileHits();

        checkPickups();
        preventEnemyOverlap();
        handlePlayerEnemyCollision();
        handleAttackDamage();
        cleanupDeadEnemiesAndWaves(delta);
    }

    private void updateGame(float delta) {
        // Player
        player.update(joystick, delta);
        player.handleObstacleCollision(obstacles);

        // Enemies
        for (Enemy e : enemies) {
            e.update(delta, player.x, player.y);
            e.handleObstacleCollision(obstacles);
        }
    }

    // Spawning & waves
    private void spawnRandomObstacles() {
        obstacles.clear();

        int numObstacles = 5;

        for (int i = 0; i < numObstacles; i++) {
            float x = rnd.nextInt(Gdx.graphics.getWidth() - 128);
            float y = rnd.nextInt(Gdx.graphics.getHeight() - 128);

            // avoid spawning right on top of player
            if (Math.abs(x - player.x) < 200 && Math.abs(y - player.y) < 200) {
                i--;
                continue;
            }

            obstacles.add(new Obstacle(x, y));
        }
    }

    private void spawnWave(int waveNumber) {
        // Boss wave
        if (waveNumber == BOSS_WAVE) {
            float bossWidth = 256;
            float bossHeight = 256;

            float x = Gdx.graphics.getWidth() / 2f - bossWidth / 2f;
            float y = Gdx.graphics.getHeight() / 2f - bossHeight / 2f;

            enemies.add(new BossEnemy(x, y, enemyProjectiles,
                calculateEnemyHP(250, true),5, this));
            System.out.println("Spawned boss on wave " + waveNumber);
            return;
        }

        // Normal enemies
        int enemyCount = calculateEnemyCount(waveNumber);

        float rangedChance = 0.15f;
        int bonusEnemyDamage = wave / 5; // +1 enemy dmg every 5 waves

        for (int i = 0; i < enemyCount; i++) {
            float x = rnd.nextInt(Gdx.graphics.getWidth() - 128);
            float y = rnd.nextInt(Gdx.graphics.getHeight() - 128);

            // avoid spawning right on top of player
            if (Math.abs(x - player.x) < 200 && Math.abs(y - player.y) < 200) {
                i--;
                continue;
            }

            if (rnd.nextFloat() < rangedChance) {
                enemies.add(new GhostEnemy(x, y, enemyProjectiles,
                    calculateEnemyHP(20, false),1 + bonusEnemyDamage));
            } else {
                enemies.add(new Enemy("enemies/enemy.png", x, y, 100, 128, 128,
                    calculateEnemyHP(30, false), 1 + bonusEnemyDamage));
            }

        }

        System.out.println("Spawned wave " + waveNumber + " with " + enemyCount + " enemies");
    }

    private int calculateEnemyCount(int waveNumber) {
        int baseEnemyCount = 3 + (waveNumber - 1) * 2; // wave 1=3, 2=5, 3=7...
        if (waveNumber > 15) {
            baseEnemyCount = 31; // don't increase enemy count after wave 15
        }

        float multiplier = 1f;
        switch (this.getDifficulty()) {
            case EASY:
                multiplier = 0.75f; // EASY: wave 1=2, 2=4, 3=5...
                break;
            case HARD:
                multiplier = 1.4f; // HARD: wave 1=4 2=7 3=10...
                break;
            case NORMAL:
            default:
                // NORMAL: wave 1=3, 2=5, 3=7...
                break;
        }

        return Math.round(baseEnemyCount * multiplier);
    }

    public int calculateEnemyHP(int baseHP, boolean isBoss) {
        float hpMultiplier = 1f;

        switch (this.getDifficulty()) {
            case EASY:
                hpMultiplier = 0.8f;
                break;
            case HARD:
                hpMultiplier = 1.35f;
                break;
            case NORMAL:
            default:
                break;
        }

        int waveBonus = 0;
        if (!isBoss) {
            waveBonus = (wave * 2) - 2; // wave based scaling (wave 1=+0)
        }

        int finalHp = Math.round((baseHP + waveBonus) * hpMultiplier);

        // in case of 0 or negative hp
        return Math.max(1, finalHp);
    }

    public void spawnBossReinforcements() {
        int reinforcementWave = Math.max(1, wave - 1); // scale with current wave
        spawnWave(reinforcementWave);
    }

    private void spawnWavePickups(int waveNumber) {
        int numPickups = 2 + waveNumber / 2;

        for (int i = 0; i < numPickups; i++) {
            float x = rnd.nextInt(Gdx.graphics.getWidth() - 64);
            float y = rnd.nextInt(Gdx.graphics.getHeight() - 64);

            Pickup.Type randomType = getRandomPickupType(player.speed);

            pickups.add(new Pickup(randomType, x, y));
        }
    }

    // Pickups & collisions
    private Pickup.Type getRandomPickupType(float currentPlayerSpeed) {

        boolean allowSpeed = currentPlayerSpeed < player.maxSpeed;

        Array<Pickup.Type> pool = new Array<>();

        // (duplicates = higher chance)
        pool.add(Pickup.Type.HEALTH);
        pool.add(Pickup.Type.HEALTH);

        pool.add(Pickup.Type.DAMAGE);
        pool.add(Pickup.Type.DAMAGE);

        if (allowSpeed) {
            pool.add(Pickup.Type.SPEED);
        }

        pool.add(Pickup.Type.COIN);  // 1 coin vs 2+ of others

        return pool.random();
    }

    private void checkPickups() {
        for (int i = pickups.size - 1; i >= 0; i--) {
            Pickup p = pickups.get(i);

            if (player.bounds.overlaps(p.bounds)) {
                applyPickupEffect(p);
                pickups.removeIndex(i);
                pickupsPicked++;
            }
        }
    }

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

    private void preventEnemyOverlap() {
        for (int i = 0; i < enemies.size; i++) {
            Enemy a = enemies.get(i);

            for (int j = i + 1; j < enemies.size; j++) {
                Enemy b = enemies.get(j);

                float dx = b.getX() - a.getX();
                float dy = b.getY() - a.getY();

                float dist = (float) Math.sqrt(dx * dx + dy * dy);
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

                    // apply damage
                    player.health -= e.damage;

                    if (player.health <= 0) {
                        onPlayerDied();
                    }

                    // start damage cooldown
                    player.damageCooldown = player.damageCooldownTime;

                    // knockback direction
                    float dx = player.x - e.getX();
                    float dy = player.y - e.getY();
                    float len = (float) Math.sqrt(dx * dx + dy * dy);
                    if (len != 0) {
                        dx /= len;
                        dy /= len;
                    }

                    // Apply knockback to player
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
            if (e.hitThisSwing) continue;
            if (!e.getBounds().overlaps(player.attackHitbox)) continue;

            e.takeDamage(player.getCurrentDamage());
            e.hitThisSwing = true;

            // knockback enemy away from player
            float dx = e.getX() - player.x;
            float dy = e.getY() - player.y;

            float length = (float) Math.sqrt(dx * dx + dy * dy);
            if (length != 0) {
                dx /= length;
                dy /= length;
            }

            // knockback strength
            float knockback = 160f;

            e.setX(e.getX() + dx * knockback);
            e.setY(e.getY() + dy * knockback);

            System.out.println("HIT! Enemy HP = " + e.health);
        }
    }

    private void cleanupDeadEnemiesAndWaves(float delta) {
        // Remove dead enemies & detect boss death
        for (int i = enemies.size - 1; i >= 0; i--) {
            Enemy dead = enemies.get(i);
            if (!dead.alive) {
                enemies.removeIndex(i);
                enemiesKilled++;

                if (enemiesKilled >= 100) {
                    achievementManager.unlock("kills_100");
                }

                if (dead.isBoss) {
                    onBossDefeat();
                }
            }
        }

        // Wave progression
        if (enemies.size == 0 && !waitingForNextWave) {
            waitingForNextWave = true;
            waveTimer = timeBetweenWaves; // start countdown
        }

        if (waitingForNextWave) {
            waveTimer -= delta;

            if (waveTimer <= 0) {
                wave++;

                if (wave >= 5) {
                    achievementManager.unlock("wave_5");
                }
                if (wave >= 10) {
                    achievementManager.unlock("wave_10");
                }

                if (scoreReporter != null) {
                    scoreReporter.reportHighestWave(wave);
                }

                spawnWave(wave);
                spawnWavePickups(wave);
                waitingForNextWave = false;
            }
        }
    }

    // Projectiles
    private void spawnProjectile(float x, float y, Vector2 dir, int damage) {
        Projectile p = new Projectile(x, y, dir.x, dir.y, damage);
        playerProjectiles.add(p);
    }

    private void updatePlayerProjectiles(float delta) {
        for (int i = playerProjectiles.size - 1; i >= 0; i--) {
            Projectile p = playerProjectiles.get(i);
            p.update(delta);

            if (!p.alive) {
                playerProjectiles.removeIndex(i);
            }
        }
    }

    private void handlePlayerProjectileHits() {
        for (Projectile p : playerProjectiles) {
            if (!p.alive) continue;

            for (Enemy e : enemies) {
                if (!e.alive) continue;

                if (p.getBounds().overlaps(e.getBounds())) {
                    e.takeDamage(p.damage);
                    p.alive = false;
                    break;
                }
            }
        }
    }

    private void updateEnemyProjectiles(float delta) {
        for (int i = enemyProjectiles.size - 1; i >= 0; i--) {
            Projectile p = enemyProjectiles.get(i);
            p.update(delta);

            if (!p.alive) {
                enemyProjectiles.removeIndex(i);
            }
        }
    }

    private void handleEnemyProjectileHits() {
        for (Projectile p : enemyProjectiles) {
            if (!p.alive) continue;

            if (p.getBounds().overlaps(player.bounds)) {
                player.health -= p.damage;
                p.alive = false;
                System.out.println("Player hit! HP = " + player.health);

                if (player.health <= 0) {
                    onPlayerDied();
                    return;
                }
            }
        }
    }

    // Game over / boss defeat
    private void onPlayerDied() {
        finishRun(false);

        // Give Firebase time to write
        Gdx.app.postRunnable(() -> {
            try {
                Thread.sleep(300);
            } catch (InterruptedException ignored) {}
            Gdx.app.postRunnable(() -> Gdx.app.exit());
        });
    }

    private void onBossDefeat() {
        bossDefeated = true;
        achievementManager.unlock("first_win");
        finishRun(true);
    }

    private void finishRun(boolean win) {
        if (runReported) return;   // prevent double-save
        runReported = true;

        if (scoreReporter != null) {
            boolean rangedChosen = player.playerClass == Player.PlayerClass.RANGED;
            scoreReporter.reportRun(
                wave,
                win ? (int) runTime : 0, // if won, time = runtime. else, time = 0
                enemiesKilled,
                pickupsPicked,
                player.coins,
                win,
                rangedChosen
            );
        }
    }

    // Drawing
    private void drawGame() {
        batch.begin();

        // world
        player.draw(batch);

        for (Enemy e : enemies) {
            e.draw(batch);
        }

        drawBossHealthBar();

        for (Obstacle o : obstacles) {
            o.draw(batch);
        }

        for (Pickup p : pickups) {
            p.draw(batch);
        }

        for (Projectile p : playerProjectiles) {
            p.draw(batch);
        }

        for (Projectile p : enemyProjectiles) {
            p.draw(batch);
        }

        // attack button
        batch.draw(
            attackBtnTexture,
            attackBtnBounds.x, attackBtnBounds.y,
            attackBtnBounds.width, attackBtnBounds.height
        );

        drawDebugAttackHitbox(); // remove later/change to actual animation
        drawUI();

        batch.end();

        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    private void drawUI() {
        float screenH = Gdx.graphics.getHeight();

        font.draw(batch, "HP: " + player.health, 20, screenH - 20);
        font.draw(batch, "Wave: " + wave, 20, screenH - 90);
        font.draw(batch, "Damage: " + player.getCurrentDamage(), 20, screenH - 140);
        font.draw(batch, "Speed: " + player.speed, 20, screenH - 190);
        font.draw(batch, "Coins: " + player.coins, 20, screenH - 240);
    }

    private void drawBossHealthBar() {
        BossEnemy boss = getBoss();
        if (boss == null) return;

        float barWidth = Gdx.graphics.getWidth() * 0.7f;
        float barHeight = 30f;

        float x = (Gdx.graphics.getWidth() - barWidth) / 2f;
        float y = Gdx.graphics.getHeight() - 60f;

        float healthPercent =
            (float) boss.getHealth() / boss.getMaxHealth();

        // Background (dark red)
        batch.setColor(0.3f, 0f, 0f, 1f);
        batch.draw(player.debugPixel, x, y, barWidth, barHeight);

        // Health (bright red)
        batch.setColor(1f, 0f, 0f, 1f);
        batch.draw(player.debugPixel, x, y, barWidth * healthPercent, barHeight);

        // Reset color
        batch.setColor(1, 1, 1, 1);
    }

    private BossEnemy getBoss() {
        for (Enemy e : enemies) {
            if (e instanceof BossEnemy) {
                return (BossEnemy) e;
            }
        }
        return null;
    }

    private void drawDebugAttackHitbox() { // remove later/change to actual animation
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
    }
}
