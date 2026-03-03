package com.example.rougelikegame.android.screens.menu;

import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.example.rougelikegame.android.managers.AchievementManager;
import com.example.rougelikegame.android.models.characters.EnemyFactory;
import com.example.rougelikegame.android.models.core.CombatSystem;
import com.example.rougelikegame.android.models.core.GameState;
import com.example.rougelikegame.android.models.core.ScoreReporter;
import com.example.rougelikegame.android.models.characters.BossEnemy;
import com.example.rougelikegame.android.models.characters.Enemy;
import com.example.rougelikegame.android.models.characters.GhostEnemy;
import com.example.rougelikegame.android.models.input.GameInputController;
import com.example.rougelikegame.android.models.input.Joystick;
import com.example.rougelikegame.android.models.items.contexts.DamageContext;
import com.example.rougelikegame.android.models.items.ItemRegistry;
import com.example.rougelikegame.android.models.items.PassiveItem;
import com.example.rougelikegame.android.models.meta.RunStats;
import com.example.rougelikegame.android.models.meta.Skin;
import com.example.rougelikegame.android.models.world.Obstacle;
import com.example.rougelikegame.android.models.world.Pickup;
import com.example.rougelikegame.android.models.characters.Player;
import com.example.rougelikegame.android.models.world.ProjectileSystem;
import com.example.rougelikegame.android.models.world.WaveManager;
import com.example.rougelikegame.android.models.world.WaveSpawner;
import com.example.rougelikegame.android.utils.SkinRegistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class MainActivity extends ApplicationAdapter implements WaveSpawner {

    // Constructors (for AndroidLauncher / tests)
    private final ScoreReporter scoreReporter;
    private final Player.PlayerClass selectedClass;
    private final Player.Difficulty difficulty;
    private final String skinId;

    public MainActivity(
        ScoreReporter scoreReporter,
        Player.PlayerClass selectedClass,
        Player.Difficulty difficulty,
        String skinId,
        boolean dailyChallenge,
        long runSeed
    ) {
        this.scoreReporter = scoreReporter;
        this.selectedClass = selectedClass;
        this.difficulty = difficulty;
        this.skinId = skinId;
        this.dailyChallenge = dailyChallenge;
        this.runSeed = runSeed;
    }
    public MainActivity(ScoreReporter scoreReporter) {
        this(scoreReporter, Player.PlayerClass.MELEE,
            Player.Difficulty.NORMAL, "default", false, System.currentTimeMillis());
    }
    public MainActivity() {
        this(null, Player.PlayerClass.MELEE,
            Player.Difficulty.NORMAL, "default", false, System.currentTimeMillis());
    }
    private boolean runReported = false;

    // LibGDX objects
    SpriteBatch batch;
    OrthographicCamera camera;
    Stage stage;
    private Stage rewardStage;
    private boolean rewardScreenActive = false;
    private int rewardWave = -1;
    private int pendingWaveToSpawn = -1;
    private final Map<String, Texture> itemIconCache = new HashMap<>();
    private Texture fallbackItemTexture;
    Joystick joystick;
    BitmapFont font;

    // Game objects
    Player player;
    private EnemyFactory enemyFactory;
    Array<Enemy> enemies;
    Array<Pickup> pickups;
    Array<Obstacle> obstacles;
    private ProjectileSystem projectileSystem;
    private CombatSystem combatSystem;
    private WaveManager waveManager;
    private GameInputController inputController;

    // Cached textures
    private Texture playerTexture;
    private Texture enemyTexture;
    private Texture ghostTexture;
    private Texture bossTexture;

    // Game state
    private final long runSeed;
    private static final boolean DEBUG = false;
    private final RunStats runStats = new RunStats();
    private final boolean dailyChallenge;
    private Random rnd;
    public Player.Difficulty getDifficulty() { return difficulty; }

    private final AchievementManager achievementManager =
        AchievementManager.getInstance();

    private boolean bossDefeated = false;

    // Attack button
    Texture attackBtnTexture;
    Rectangle attackBtnBounds;

    // UI
    private final GlyphLayout glyphLayout = new GlyphLayout();
    private Rectangle resumeBounds;
    private Rectangle exitBounds;

    // LibGDX
    @Override
    public void create() {
        batch = new SpriteBatch();

        GameState gameState = new GameState(runSeed);
        rnd = gameState.getRandom();

        Gdx.app.log("SEED", "Run seed = " + runSeed);

        setupCamera();
        loadTextures();
        setupPlayerAndEnemies();
        setupPickupsAndObstacles();
        setupStageAndJoystick();
        setupPauseButtons();
        setupAttackButton();
        setupInput();
        setupFont();

        Gdx.input.setCatchKey(Input.Keys.BACK, true);
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        if (!bossDefeated && !rewardScreenActive) {
            runStats.addTime(delta);
        }

        ScreenUtils.clear(0.3f, 0.3f, 0.3f, 1);

        if (!rewardScreenActive && !inputController.isPaused()) {
            update(delta);
        } else if (rewardScreenActive && rewardStage != null) {
            rewardStage.act(delta);
        }

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
        if (projectileSystem != null) projectileSystem.disposeShared();

        player.dispose();
        font.dispose();
        stage.dispose();
        if (rewardStage != null) rewardStage.dispose();
        for (Texture tex : itemIconCache.values()) {
            if (tex != null && tex != fallbackItemTexture) {
                tex.dispose();
            }
        }
        itemIconCache.clear();
        if (fallbackItemTexture != null) fallbackItemTexture.dispose();
        joystick.dispose();
        attackBtnTexture.dispose();
        enemyTexture.dispose();
        ghostTexture.dispose();
        bossTexture.dispose();
        playerTexture.dispose();
    }

    // Setup methods
    private void setupCamera() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    private void loadTextures() {
        enemyTexture = new Texture("enemies/enemy.png");
        ghostTexture = new Texture("enemies/ghost_enemy.png");
        bossTexture  = new Texture("enemies/boss.png");
        playerTexture = getPlayerTextureForSkin();
        fallbackItemTexture = new Texture("items/error.png");
    }

    private void setupPlayerAndEnemies() {
        player = new Player(100, 100);
        player.setTexture(playerTexture);
        player.playerClass = selectedClass;

        projectileSystem = new ProjectileSystem();

        enemyFactory = new EnemyFactory(
            enemyTexture,
            ghostTexture,
            bossTexture,
            projectileSystem.getEnemyProjectiles()
        );

        enemies = new Array<>();

        combatSystem = new CombatSystem(new CombatSystem.Callbacks() {
            @Override
            public void onPlayerDied() {
                MainActivity.this.onPlayerDied();
            }

            @Override
            public void onBossDefeated() {
                MainActivity.this.onBossDefeat();
            }

            @Override
            public void onEnemyKilled(Enemy enemy) {
                runStats.addKill();

                if (runStats.getEnemiesKilled() >= 100) {
                    achievementManager.unlock("kills_100");
                }
            }
        });

        waveManager = new WaveManager();

        spawnWave(waveManager.getWave(), enemies, player, getDifficulty(), true);
    }

    private Texture getPlayerTextureForSkin() {
        Skin skin = SkinRegistry.getSkinById(skinId);
        return new Texture(skin.getTexturePath());
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
            100
        );
    }

    private void setupPauseButtons() {
        float centerX = Gdx.graphics.getWidth() / 2f;

        resumeBounds = new Rectangle(centerX - 150, Gdx.graphics.getHeight() / 2f + 20, 300, 80);
        exitBounds   = new Rectangle(centerX - 150, Gdx.graphics.getHeight() / 2f - 100, 300, 80);
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
        inputController = new GameInputController(
            camera,
            stage,
            joystick,
            player,
            enemies,
            attackBtnBounds,
            resumeBounds,
            exitBounds,
            new GameInputController.ProjectileSpawner() {
                @Override
                public void spawnProjectile(float x, float y, Vector2 dir, int damage) {
                    MainActivity.this.spawnProjectile(x, y, dir, damage);
                }

                @Override
                public void spawnBeam(Vector2 dir, float chargePercent) {
                    MainActivity.this.spawnBeam(dir, chargePercent);
                }
            },
            () -> Gdx.app.exit()
        );

        Gdx.input.setInputProcessor(inputController.buildProcessor());
    }

    private void setupFont() {
        FreeTypeFontGenerator generator =
            new FreeTypeFontGenerator(Gdx.files.internal("fonts/Roboto-Regular.ttf"));

        FreeTypeFontGenerator.FreeTypeFontParameter param =
            new FreeTypeFontGenerator.FreeTypeFontParameter();

        param.size = 48; // REAL font size, not scale
        param.color = Color.WHITE;
        param.minFilter = Texture.TextureFilter.Linear;
        param.magFilter = Texture.TextureFilter.Linear;

        font = generator.generateFont(param);
        generator.dispose();
    }

    // Update loop
    private void update(float delta) {
        updateGame(delta);

        projectileSystem.update(delta);
        projectileSystem.handlePlayerProjectilesHitEnemies(player, enemies);
        projectileSystem.handleEnemyProjectilesHitPlayer(player, this::onPlayerDied);

        checkPickups();

        combatSystem.preventEnemyOverlap(enemies);
        combatSystem.handlePlayerEnemyCollision(player, enemies);
        combatSystem.handleAttackDamage(player, enemies);
        combatSystem.cleanupDeadEnemies(enemies);

        waveManager.update(
            delta,
            enemies,
            player,
            difficulty,
            this,
            this::onWaveStarted
        );
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
        if (player == null) return;

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

    public void spawnWave(int waveNumber, Array<Enemy> enemies,
                           Player player,
                           Player.Difficulty difficulty,
                          boolean allowBoss) {
        if (rewardScreenActive && waveNumber == pendingWaveToSpawn) {
            return;
        }

        // Boss wave
        if (allowBoss && waveManager.isBossWave()) {
            float bossWidth = 256;
            float bossHeight = 256;

            float x = Gdx.graphics.getWidth() / 2f - bossWidth / 2f;
            float y = Gdx.graphics.getHeight() / 2f - bossHeight / 2f;

            enemies.add(
                enemyFactory.createBossEnemy(
                    x,
                    y,
                    calculateEnemyHP(250, true),
                    5,
                    this
                )
            );
            if (DEBUG) Gdx.app.log("Spawn", "Spawned boss on wave " + waveNumber);
            return;
        }

        // Normal enemies
        int enemyCount = calculateEnemyCount(waveNumber);

        float ghostChance = 0.10f; // 10% chance to spawn a ghost
        int bonusEnemyDamage = waveManager.getWave() / 5; // +1 enemy dmg every 5 waves

        for (int i = 0; i < enemyCount; i++) {
            float x = rnd.nextInt(Gdx.graphics.getWidth() - 128);
            float y = rnd.nextInt(Gdx.graphics.getHeight() - 128);

            // avoid spawning right on top of player
            if (Math.abs(x - player.x) < 200 && Math.abs(y - player.y) < 200) {
                i--;
                continue;
            }

            if (rnd.nextFloat() < ghostChance) {
                enemies.add(
                    enemyFactory.createGhostEnemy(
                        x,
                        y,
                        calculateEnemyHP(20, false),
                        1 + bonusEnemyDamage
                    )
                );
            } else {
                enemies.add(
                    enemyFactory.createNormalEnemy(
                        x,
                        y,
                        calculateEnemyHP(30, false),
                        1 + bonusEnemyDamage
                    )
                );
            }

        }

        if (DEBUG) Gdx.app.log("Spawn", "Spawned wave " + waveNumber + " with " + enemyCount + " enemies");
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
            waveBonus = (waveManager.getWave() * 2) - 2; // wave based scaling (wave 1=+0)
        }

        int finalHp = Math.round((baseHP + waveBonus) * hpMultiplier);

        // in case of 0 or negative hp
        return Math.max(1, finalHp);
    }

    public void spawnBossReinforcements() {
        int reinforcementWave = 5;
        spawnWave(reinforcementWave, enemies, player, getDifficulty(), false);
    }

    public void spawnWavePickups(int waveNumber) {
        int numPickups = 2 + waveNumber / 2;

        for (int i = 0; i < numPickups; i++) {
            float x = rnd.nextInt(Gdx.graphics.getWidth() - 64);
            float y = rnd.nextInt(Gdx.graphics.getHeight() - 64);

            Pickup.Type randomType = getRandomPickupType(player.speed);

            pickups.add(new Pickup(randomType, x, y));
        }
    }

    // Pickups
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

        return pool.get(rnd.nextInt(pool.size));
    }

    private void checkPickups() {
        for (int i = pickups.size - 1; i >= 0; i--) {
            Pickup p = pickups.get(i);

            if (player.bounds.overlaps(p.bounds)) {
                applyPickupEffect(p);
                pickups.removeIndex(i);
                runStats.addPickup();
            }
        }
    }

    private void applyPickupEffect(Pickup p) {
        switch (p.type) {
            case HEALTH:
                player.health += 5;
                if (DEBUG) Gdx.app.log("Pickups", "Picked up health → player HP = " + player.health);
                break;

            case SPEED:
                player.speed += 50;
                if (DEBUG) Gdx.app.log("Pickups", "Picked up speed → new speed = " + player.speed);
                break;

            case DAMAGE:
                player.attackBonus += 5;
                if (DEBUG) Gdx.app.log("Pickups", "Picked up damage → new bonus = " + player.attackBonus);
                break;

            case COIN:
                player.coins++;
                if (player.coins >= 25) {
                    achievementManager.unlock("coins_25");
                }
                if (DEBUG) Gdx.app.log("Pickups", "Picked up coin → coins = " + player.coins);
                break;
        }
    }

    // Projectiles
    private void spawnProjectile(float x, float y, Vector2 dir, int baseDamage) {
        DamageContext ctx = new DamageContext(baseDamage);
        for (PassiveItem it : player.getPassiveItems()) {
            it.modifyProjectileDamage(player, ctx);
        }
        projectileSystem.spawnPlayerProjectile(x, y, dir, ctx.damage);
    }

    private void spawnBeam(Vector2 dir, float chargePercent) {
        int baseDamage = player.getCurrentDamage();
        DamageContext ctx = new DamageContext(baseDamage);

        for (PassiveItem it : player.getPassiveItems()) {
            it.modifyProjectileDamage(player, ctx);
            it.modifyMeleeDamage(player, ctx);
        }

        float scaled = ctx.damage * Math.max(0f, Math.min(chargePercent, 1f));
        int beamTickDamage = Math.max(1, Math.round(scaled));

        projectileSystem.spawnBeam(
            player.x + player.width / 2f,
            player.y + player.height / 2f,
            dir,
            beamTickDamage
        );

        player.meleeCooldown = player.getEffectiveMeleeCooldownTime();
        player.triggerRangedCooldown();
    }

    // Game over / boss defeat
    private void onPlayerDied() {
        finishRun(false);

        Gdx.app.postRunnable(() -> Gdx.app.exit());
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
                waveManager.getWave(),
                win ? (int) runStats.getRunTime() : 0, // if won, time = runtime. else, time = 0
                runStats.getEnemiesKilled(),
                runStats.getPickupsPicked(),
                player.coins,
                win,
                rangedChosen
            );
        }

        Gdx.app.log("SAVE",
            "Saving run | wave=" + waveManager.getWave() +
                " time=" + runStats.getRunTime() +
                " kills=" + runStats.getEnemiesKilled() +
                " pickups=" + runStats.getPickupsPicked() +
                " coins=" + player.coins +
                " win=" + win
        );
    }

    // Drawing
    private void drawGame() {
        batch.begin();

        // world
        player.draw(batch);

        for (Enemy e : enemies) {
            if (!(e instanceof GhostEnemy)) {
                e.draw(batch);
            }
        }

        drawBossHealthBar();

        for (Obstacle o : obstacles) {
            o.draw(batch);
        }

        // ghosts are drawn on-top of obstacles
        for (Enemy e : enemies) {
            if (e instanceof GhostEnemy) {
                e.draw(batch);
            }
        }

        for (Pickup p : pickups) {
            p.draw(batch);
        }

        projectileSystem.drawAll(batch);

        joystick.draw(batch);

        // attack button
        batch.draw(
            attackBtnTexture,
            attackBtnBounds.x, attackBtnBounds.y,
            attackBtnBounds.width, attackBtnBounds.height
        );

        drawDebugAttackHitbox(); // remove later/change to actual animation
        drawUI();

        if (inputController.isPaused()) {
            drawPauseOverlay();
        }

        if (rewardScreenActive) {
            drawRewardOverlay();
        }

        batch.end();

        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();

        if (rewardScreenActive && rewardStage != null) {
            rewardStage.draw();
        }
    }

    private void drawPauseOverlay() {
        batch.setColor(0, 0, 0, 0.6f);
        batch.draw(
            player.debugPixel,
            0, 0,
            Gdx.graphics.getWidth(),
            Gdx.graphics.getHeight()
        );
        batch.setColor(1, 1, 1, 1);

        // RESUME text
        glyphLayout.setText(font, "RESUME");
        font.draw(
            batch,
            glyphLayout,
            resumeBounds.x + (resumeBounds.width - glyphLayout.width) / 2f,
            resumeBounds.y + (resumeBounds.height + glyphLayout.height) / 2f
        );

        // EXIT text
        glyphLayout.setText(font, "EXIT");
        font.draw(
            batch,
            glyphLayout,
            exitBounds.x + (exitBounds.width - glyphLayout.width) / 2f,
            exitBounds.y + (exitBounds.height + glyphLayout.height) / 2f
        );

        if (dailyChallenge) {
            font.draw(batch, "DAILY CHALLENGE", 20, 60);
        }
        else {
            font.draw(
                batch,
                "Seed: " + runSeed,
                20,
                60
            );
        }
    }

    private void drawUI() {
        float screenH = Gdx.graphics.getHeight();
        float y = screenH - 20;
        float lineHeight = 70;

        font.draw(batch, "HP: " + player.health, 20, y); y -= lineHeight;
        font.draw(batch, "Wave: " + waveManager.getWave(), 20, y); y -= lineHeight;
        font.draw(batch, "Damage: " + player.getDisplayedDamage(), 20, y); y -= lineHeight;
        font.draw(batch, "Speed: " + player.speed, 20, y); y -= lineHeight;
        font.draw(batch, "Coins: " + player.coins, 20, y);

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

    private boolean onWaveStarted(int waveNumber) {
        if (waveNumber % 4 != 0) {
            return false;
        }

        rewardWave = waveNumber;
        pendingWaveToSpawn = waveNumber;
        showRewardScreen();
        return true;
    }

    private void showRewardScreen() {
        rewardScreenActive = true;

        if (rewardStage != null) {
            rewardStage.dispose();
        }

        rewardStage = new Stage(new ScreenViewport());

        RewardOption[] options = pickRewardItemOptions();

        float screenW = Gdx.graphics.getWidth();
        float screenH = Gdx.graphics.getHeight();
        float cardW = Math.min(420f, screenW * 0.4f);
        float cardH = 180f;
        float gap = 80f;
        float totalW = (cardW * 2f) + gap;
        float startX = (screenW - totalW) / 2f;
        float y = (screenH - cardH) / 2f - 20f;

        RewardCardActor left = new RewardCardActor(options[0]);
        left.setBounds(startX, y, cardW, cardH);
        left.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                onRewardItemSelected(options[0].itemId);
            }
        });

        RewardCardActor right = new RewardCardActor(options[1]);
        right.setBounds(startX + cardW + gap, y, cardW, cardH);
        right.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                onRewardItemSelected(options[1].itemId);
            }
        });

        rewardStage.addActor(left);
        rewardStage.addActor(right);

        Gdx.input.setInputProcessor(rewardStage);
    }

    private void onRewardItemSelected(int itemId) {
        player.addPassiveItem(ItemRegistry.create(itemId));
        rewardScreenActive = false;

        if (rewardStage != null) {
            rewardStage.dispose();
            rewardStage = null;
        }

        player.giveImmunity(1.0f);
        spawnWave(pendingWaveToSpawn, enemies, player, difficulty, true);
        spawnWavePickups(pendingWaveToSpawn);
        pendingWaveToSpawn = -1;
        rewardWave = -1;

        Gdx.input.setInputProcessor(inputController.buildProcessor());
    }

    private RewardOption[] pickRewardItemOptions() {
        Set<Integer> allIdsSet = ItemRegistry.getAllItemIds();
        List<Integer> allIds = new ArrayList<>(allIdsSet);
        List<Integer> unowned = new ArrayList<>();

        for (Integer id : allIds) {
            if (!player.hasItem(id)) {
                unowned.add(id);
            }
        }

        Collections.shuffle(unowned, rnd);
        Collections.shuffle(allIds, rnd);

        int[] optionIds = new int[2];
        int index = 0;

        while (index < 2 && !unowned.isEmpty()) {
            optionIds[index++] = unowned.remove(0);
        }

        while (index < 2) {
            optionIds[index++] = allIds.get(rnd.nextInt(allIds.size()));
        }

        RewardOption[] options = new RewardOption[2];
        for (int i = 0; i < 2; i++) {
            PassiveItem item = ItemRegistry.create(optionIds[i]);
            options[i] = new RewardOption(
                optionIds[i],
                item.getDisplayName(),
                getItemIconTexture(item.getIconPath())
            );
        }

        return options;
    }

    private Texture getItemIconTexture(String iconPath) {
        if (iconPath == null || iconPath.trim().isEmpty()) {
            return fallbackItemTexture;
        }

        if (itemIconCache.containsKey(iconPath)) {
            return itemIconCache.get(iconPath);
        }

        Texture texture;
        try {
            texture = new Texture(iconPath);
        } catch (Exception ex) {
            Gdx.app.error("RewardUI", "Failed to load icon: " + iconPath + ", using fallback", ex);
            texture = fallbackItemTexture;
        }

        itemIconCache.put(iconPath, texture);
        return texture;
    }

    private void drawRewardOverlay() {
        batch.setColor(0, 0, 0, 0.7f);
        batch.draw(
            player.debugPixel,
            0, 0,
            Gdx.graphics.getWidth(),
            Gdx.graphics.getHeight()
        );
        batch.setColor(1, 1, 1, 1);

        glyphLayout.setText(font, "Choose a Passive Item");
        font.draw(
            batch,
            glyphLayout,
            (Gdx.graphics.getWidth() - glyphLayout.width) / 2f,
            Gdx.graphics.getHeight() * 0.72f
        );

        glyphLayout.setText(font, "Wave " + rewardWave + " Reward");
        font.draw(
            batch,
            glyphLayout,
            (Gdx.graphics.getWidth() - glyphLayout.width) / 2f,
            Gdx.graphics.getHeight() * 0.66f
        );
    }

    private static class RewardOption {
        private final int itemId;
        private final String displayName;
        private final Texture iconTexture;

        RewardOption(int itemId, String displayName, Texture iconTexture) {
            this.itemId = itemId;
            this.displayName = displayName;
            this.iconTexture = iconTexture;
        }
    }

    private class RewardCardActor extends Actor {
        private final RewardOption option;

        RewardCardActor(RewardOption option) {
            this.option = option;
        }

        @Override
        public void draw(com.badlogic.gdx.graphics.g2d.Batch actorBatch, float parentAlpha) {
            actorBatch.setColor(0.2f, 0.2f, 0.2f, 1f);
            actorBatch.draw(player.debugPixel, getX(), getY(), getWidth(), getHeight());

            actorBatch.setColor(1f, 1f, 1f, 1f);

            float iconSize = Math.min(getWidth() * 0.4f, getHeight() * 0.55f);
            float iconX = getX() + (getWidth() - iconSize) / 2f;
            float iconY = getY() + getHeight() - iconSize - 24f;
            actorBatch.draw(option.iconTexture, iconX, iconY, iconSize, iconSize);

            glyphLayout.setText(font, option.displayName);
            font.draw(
                actorBatch,
                glyphLayout,
                getX() + (getWidth() - glyphLayout.width) / 2f,
                getY() + 52f
            );
        }
    }

    @Override
    public void pause() {
        if (inputController != null) {
            inputController.setPaused(true);
        }
    }
}
