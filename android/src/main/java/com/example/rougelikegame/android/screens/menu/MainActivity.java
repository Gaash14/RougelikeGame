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
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.example.rougelikegame.android.managers.AchievementManager;
import com.example.rougelikegame.android.managers.MusicManager;
import com.example.rougelikegame.android.managers.SoundManager;
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
import com.example.rougelikegame.android.models.items.contexts.HomingContext;
import com.example.rougelikegame.android.models.items.passives.BeamItem;
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
import java.util.HashMap;
import java.util.HashSet;
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
    private Texture backgroundTexture;
    private Texture hpIcon;
    private Texture waveIcon;
    private Texture damageIcon;
    private Texture speedIcon;
    private Texture coinsIcon;

    // Game state
    private final long runSeed;
    private static final boolean DEBUG = false;
    private final RunStats runStats = new RunStats();
    private final boolean dailyChallenge;
    private Random itemRnd;
    private Random pickupRnd;
    private Random enemyRnd;
    private Random miscRnd;
    public Player.Difficulty getDifficulty() { return difficulty; }

    private final AchievementManager achievementManager =
        AchievementManager.getInstance();

    private boolean bossDefeated = false;
    private float victoryRunTimeSeconds = 0f;
    private GameOverlayScreens overlayScreens;

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
        SoundManager.load();
        MusicManager.load();
        MusicManager.playBackgroundMusic();

        GameState gameState = new GameState(runSeed);
        itemRnd = gameState.getItemRandom();
        pickupRnd = gameState.getPickupRandom();
        enemyRnd = gameState.getEnemyRandom();
        miscRnd = gameState.getMiscRandom();

        Gdx.app.log("SEED", "Run seed = " + runSeed);

        setupCamera();
        loadTextures();
        setupPickupsAndObstacles();
        setupPlayerAndEnemies();
        setupStageAndJoystick();
        setupPauseButtons();
        setupAttackButton();
        setupInput();
        setupFont();
        setupOverlayScreens();

        Gdx.input.setCatchKey(Input.Keys.BACK, true);
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        if (!bossDefeated && !overlayScreens.isAnyOverlayActive()) {
            runStats.addTime(delta);
        }

        ScreenUtils.clear(0.05f, 0.05f, 0.08f, 1);

        if (!overlayScreens.isAnyOverlayActive() && !inputController.isPaused()) {
            update(delta);
        } else {
            overlayScreens.act(delta);
        }

        drawGame();
    }

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
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
        if (font != null) font.dispose();
        if (stage != null) stage.dispose();
        if (overlayScreens != null) overlayScreens.dispose();
        for (Texture tex : itemIconCache.values()) {
            if (tex != null && tex != fallbackItemTexture) {
                tex.dispose();
            }
        }
        itemIconCache.clear();
        if (fallbackItemTexture != null) fallbackItemTexture.dispose();
        if (joystick != null) joystick.dispose();
        if (attackBtnTexture != null) attackBtnTexture.dispose();
        if (enemyTexture != null) enemyTexture.dispose();
        if (ghostTexture != null) ghostTexture.dispose();
        if (bossTexture != null) bossTexture.dispose();
        if (playerTexture != null) playerTexture.dispose();
        if (hpIcon != null) hpIcon.dispose();
        if (waveIcon != null) waveIcon.dispose();
        if (damageIcon != null) damageIcon.dispose();
        if (speedIcon != null) speedIcon.dispose();
        if (coinsIcon != null) coinsIcon.dispose();
        if (backgroundTexture != null) backgroundTexture.dispose();
        MusicManager.dispose();
        SoundManager.dispose();
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

        backgroundTexture = new Texture("backgrounds/abyss_cave_bg.png");

        hpIcon = new Texture("ui/hp_icon.png");
        waveIcon = new Texture("ui/wave_icon.png");
        damageIcon = new Texture("ui/damage_icon.png");
        speedIcon = new Texture("ui/speed_icon.png");
        coinsIcon = new Texture("ui/coin_icon.png");

        hpIcon.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        waveIcon.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        damageIcon.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        speedIcon.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        coinsIcon.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    }

    private void setupPlayerAndEnemies() {
        player = new Player(100, 100, miscRnd);
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
            public void onPlayerDamaged(int damageTaken) {
                MainActivity.this.onPlayerDamaged(damageTaken);
            }

            @Override
            public void onEnemyKilled(Enemy enemy) {
                SoundManager.play("enemy_death");
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
            this::exitRun
        );

        Gdx.input.setInputProcessor(inputController.buildProcessor());
    }

    private void setupOverlayScreens() {
        overlayScreens = new GameOverlayScreens(font, glyphLayout, player.debugPixel, new GameOverlayScreens.OverlayCallbacks() {
            @Override
            public void onRewardSelected(int itemId) {
                MainActivity.this.onRewardItemSelected(itemId);
            }

            @Override
            public boolean canAffordRewardReroll() {
                return player.coins >= 20;
            }

            @Override
            public GameOverlayScreens.RewardOption[] onRewardRerollRequested() {
                return MainActivity.this.onRewardRerollRequested();
            }

            @Override
            public void onVictoryMainMenuSelected() {
                MainActivity.this.onVictoryMainMenuSelected();
            }

            @Override
            public void onVictoryEndlessModeSelected() {
                MainActivity.this.onVictoryEndlessModeSelected();
            }

            @Override
            public void onDeathMainMenuSelected() {
                MainActivity.this.onDeathMainMenuSelected();
            }

        });
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

        projectileSystem.update(delta, enemies);
        projectileSystem.handlePlayerProjectilesHitEnemies(player, enemies);
        projectileSystem.handleEnemyProjectilesHitPlayer(player, this::onPlayerDied, this::onPlayerDamaged);

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
        inputController.update(delta);
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

        if (obstacles == null) {
            obstacles = new Array<>();
        } else {
            obstacles.clear();
        }

        int numObstacles = 5;

        for (int i = 0; i < numObstacles; i++) {
            float x = miscRnd.nextInt(Gdx.graphics.getWidth() - 128);
            float y = miscRnd.nextInt(Gdx.graphics.getHeight() - 128);

            // avoid spawning on top of player
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
        if (overlayScreens != null
            && overlayScreens.isRewardScreenActive()
            && waveNumber == pendingWaveToSpawn) {
            return;
        }

        spawnRandomObstacles();

        // Boss wave
        if (allowBoss && waveManager.isBossWave()) {
            MusicManager.playBossMusic();
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
        int bonusEnemyDamage = waveManager.getWave() / 6; // +1 enemy dmg every 6 waves

        for (int i = 0; i < enemyCount; i++) {
            float x = enemyRnd.nextInt(Gdx.graphics.getWidth() - 128);
            float y = enemyRnd.nextInt(Gdx.graphics.getHeight() - 128);

            // avoid spawning right on top of player
            if (Math.abs(x - player.x) < 200 && Math.abs(y - player.y) < 200) {
                i--;
                continue;
            }

            if (enemyRnd.nextFloat() < ghostChance) {
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
        int cappedWave = Math.min(waveNumber, 15);
        int baseEnemyCount = 3 + ((cappedWave - 1) * 3) / 2; // wave 1=3, 2=4, 3=6...

        float multiplier = 1f;
        switch (this.getDifficulty()) {
            case EASY:
                multiplier = 0.75f; // EASY: wave 1=2, 2=3, 3=5...
                break;
            case HARD:
                multiplier = 1.35f; // HARD: wave 1=4, 2=5, 3=8...
                break;
            case NORMAL:
            default:
                // NORMAL: wave 1=3, 2=4, 3=6...
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
            float x = pickupRnd.nextInt(Gdx.graphics.getWidth() - 64);
            float y = pickupRnd.nextInt(Gdx.graphics.getHeight() - 64);

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

        if (allowSpeed) {
            pool.add(Pickup.Type.SPEED);
        }

        pool.add(Pickup.Type.COIN);  // 1 coin vs 2+ of others

        return pool.get(pickupRnd.nextInt(pool.size));
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
        SoundManager.play("pickup");

        switch (p.type) {
            case HEALTH:
                player.health += 4;
                if (DEBUG) Gdx.app.log("Pickups", "Picked up health → player HP = " + player.health);
                break;

            case SPEED:
                player.speed += 10;
                if (DEBUG) Gdx.app.log("Pickups", "Picked up speed → new speed = " + player.speed);
                break;

            case COIN:
                player.addCoins(1);
                if (player.coins >= 25) {
                    achievementManager.unlock("coins_25");
                }
                if (DEBUG) Gdx.app.log("Pickups", "Picked up coin → coins = " + player.coins);
                break;
        }
    }

    // Projectiles
    private void spawnProjectile(float x, float y, Vector2 dir, int baseDamage) {
        SoundManager.play("shoot");

        DamageContext ctx = new DamageContext(baseDamage);
        for (PassiveItem it : player.getPassiveItems()) {
            it.modifyProjectileDamage(player, ctx);
        }
        HomingContext homingContext = player.getEffectiveHomingContext();
        projectileSystem.spawnPlayerProjectile(x, y, dir, ctx.damage, homingContext);
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

    private void onPlayerDamaged(int damageTaken) {
        for (PassiveItem item : player.getPassiveItems()) {
            item.onPlayerDamaged(player, damageTaken);
        }
    }

    public void spawnCoinAtPlayerPosition() {
        pickups.add(new Pickup(Pickup.Type.COIN, player.x, player.y));
    }

    // Game over / boss defeat
    private void exitRun() {
        finishRun(bossDefeated);
        Gdx.app.exit();
    }

    private void onPlayerDied() {
        finishRun(bossDefeated);
        showDeathScreen();
    }

    private void onBossDefeat() {
        bossDefeated = true;
        victoryRunTimeSeconds = runStats.getRunTime();
        MusicManager.playBackgroundMusic();
        achievementManager.unlock("first_win");
        showVictoryScreen();
    }

    private void finishRun(boolean win) {
        if (runReported) return;   // prevent double-save
        runReported = true;

        if (scoreReporter != null) {
            boolean rangedChosen = player.playerClass == Player.PlayerClass.RANGED;
            scoreReporter.reportRun(
                waveManager.getWave(),
                win ? (int) victoryRunTimeSeconds : 0, // if won, time = boss-clear runtime. else, time = 0
                runStats.getEnemiesKilled(),
                runStats.getPickupsPicked(),
                runStats.getItemsPicked(),
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
                " itemsPicked=" + runStats.getItemsPicked() +
                " coins=" + player.coins +
                " win=" + win
        );
    }

    private void showVictoryScreen() {
        inputController.setPaused(false);
        overlayScreens.showVictoryScreen();
    }

    private void onVictoryMainMenuSelected() {
        finishRun(bossDefeated);
        Gdx.app.postRunnable(() -> Gdx.app.exit());
    }

    private void onVictoryEndlessModeSelected() {
        overlayScreens.hideVictoryScreen();
        Gdx.input.setInputProcessor(inputController.buildProcessor());
        // stop sending input to the victory menu and start sending input back to the gameplay controls
    }

    // Drawing
    private void drawGame() {
        batch.begin();

        // background
        batch.draw(
            backgroundTexture,
            0, 0,
            Gdx.graphics.getWidth(),
            Gdx.graphics.getHeight()
        );


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
        drawBeamChargeBar();

        if (inputController.isPaused()) {
            drawPauseOverlay();
        }

        overlayScreens.drawOverlay(batch);

        batch.end();

        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();

        overlayScreens.drawStage();
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
        final float panelX = 12f;
        final float panelPadding = 14f;
        final float panelTopMargin = 12f;
        final float iconSize = 64f;
        final float rowCount = 5f;
        final float lineHeight = 54f;
        final float textX = panelX + panelPadding + iconSize + 12f;
        final float panelWidth = 360f;
        final float panelHeight = panelPadding * 2f + (rowCount * lineHeight);
        final float panelY = Gdx.graphics.getHeight() - panelTopMargin - panelHeight;
        final float firstRowBaselineY = panelY + panelHeight - panelPadding - 6f;
        final float iconX = panelX + panelPadding;

        batch.setColor(0f, 0f, 0f, 0.55f);
        batch.draw(player.debugPixel, panelX, panelY, panelWidth, panelHeight);
        batch.setColor(Color.WHITE);

        float textY = firstRowBaselineY;
        float iconY = textY - iconSize + 6f;

        batch.draw(hpIcon, iconX, iconY, iconSize, iconSize);
        font.draw(batch, "HP: " + player.health, textX, textY);

        textY -= lineHeight;
        iconY -= lineHeight;
        batch.draw(waveIcon, iconX, iconY, iconSize, iconSize);
        font.draw(batch, "Wave: " + waveManager.getWave(), textX, textY);

        textY -= lineHeight;
        iconY -= lineHeight;
        batch.draw(damageIcon, iconX, iconY, iconSize, iconSize);
        font.draw(batch, "Damage: " + player.getDisplayedDamage(), textX, textY);

        textY -= lineHeight;
        iconY -= lineHeight;
        batch.draw(speedIcon, iconX, iconY, iconSize, iconSize);
        font.draw(batch, "Speed: " + player.speed, textX, textY);

        textY -= lineHeight;
        iconY -= lineHeight;
        batch.draw(coinsIcon, iconX, iconY, iconSize, iconSize);
        font.draw(batch, "Coins: " + player.coins, textX, textY);
    }

    private void drawBeamChargeBar() {
        if (!hasBeamItemEquipped() && !player.isChargingBeam()) return;

        float progress = player.getBeamChargeProgress();

        float barWidth = 60f;
        float barHeight = 8f;
        float x = player.x + (player.width - barWidth) / 2f;
        float y = player.y + player.height + 12f;

        batch.setColor(0f, 0f, 0f, 0.7f);
        batch.draw(player.debugPixel, x, y, barWidth, barHeight);

        batch.setColor(0.3f, 0.85f, 1f, 0.95f);
        batch.draw(player.debugPixel, x, y, barWidth * progress, barHeight);

        batch.setColor(1f, 1f, 1f, 1f);
    }

    private boolean hasBeamItemEquipped() {
        for (PassiveItem item : player.getPassiveItems()) {
            if (item.getItemId() == BeamItem.ID) {
                return true;
            }
        }
        return false;
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

        final String BOSS_NAME = "The Sugar-Fueled Anomaly, Reaper of Souls";

        float originalScaleX = font.getData().scaleX;
        float originalScaleY = font.getData().scaleY;
        Color originalFontColor = font.getColor().cpy();

        font.getData().setScale(1.15f, 1.15f);
        glyphLayout.setText(font, BOSS_NAME);

        float textX = x + (barWidth - glyphLayout.width) / 2f;
        float textY = y - 18f;
        float labelPaddingX = 20f;
        float labelPaddingY = 12f;
        float labelX = textX - labelPaddingX;
        float labelY = textY - glyphLayout.height - labelPaddingY;
        float labelWidth = glyphLayout.width + (labelPaddingX * 2f);
        float labelHeight = glyphLayout.height + (labelPaddingY * 2f);

        batch.setColor(0.04f, 0f, 0f, 0.72f);
        batch.draw(player.debugPixel, labelX, labelY, labelWidth, labelHeight);

        batch.setColor(0.55f, 0f, 0f, 0.95f);
        batch.draw(player.debugPixel, labelX, labelY + labelHeight - 4f, labelWidth, 4f);
        batch.draw(player.debugPixel, labelX, labelY, labelWidth, 4f);

        batch.setColor(1, 1, 1, 1);

        font.setColor(0.92f, 0.12f, 0.12f, 1f);
        font.draw(batch, glyphLayout, textX, textY);

        font.getData().setScale(originalScaleX, originalScaleY);
        font.setColor(originalFontColor);
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
        if (waveNumber % 3 != 0) {
            return false;
        }

        pendingWaveToSpawn = waveNumber;
        showRewardScreen(waveNumber);
        return true;
    }

    private void showRewardScreen(int waveNumber) {
        overlayScreens.showRewardScreen(waveNumber, pickRewardItemOptions());
    }

    private void showDeathScreen() {
        inputController.setPaused(false);
        overlayScreens.showDeathScreen();
    }

    private void onDeathMainMenuSelected() {
        finishRun(bossDefeated);
        Gdx.app.postRunnable(() -> Gdx.app.exit());
    }

    private void onRewardItemSelected(int itemId) {
        player.addPassiveItem(ItemRegistry.create(itemId));
        runStats.addItemPicked();
        overlayScreens.hideRewardScreen();

        player.giveImmunity(1.0f);
        spawnWave(pendingWaveToSpawn, enemies, player, difficulty, true);
        spawnWavePickups(pendingWaveToSpawn);
        pendingWaveToSpawn = -1;

        Gdx.input.setInputProcessor(inputController.buildProcessor());
    }

    private GameOverlayScreens.RewardOption[] pickRewardItemOptions() {
        Set<Integer> allIdsSet = ItemRegistry.getAllItemIds();
        List<Integer> allIds = new ArrayList<>(allIdsSet);
        List<Integer> candidateIds = new ArrayList<>();

        for (Integer id : allIds) {
            if (!player.hasItem(id)) {
                candidateIds.add(id);
            }
        }

        boolean ownsAllItems = candidateIds.isEmpty();
        if (ownsAllItems) {
            candidateIds.addAll(allIds);
        }

        int[] optionIds = new int[2];
        Set<Integer> pickedIds = new HashSet<>();
        for (int i = 0; i < optionIds.length; i++) {
            int pickedId = pickItemIdByTierWeight(candidateIds, pickedIds, ownsAllItems);
            optionIds[i] = pickedId;
            if (!ownsAllItems) {
                pickedIds.add(pickedId);
            }
        }

        GameOverlayScreens.RewardOption[] options = new GameOverlayScreens.RewardOption[2];
        for (int i = 0; i < 2; i++) {
            PassiveItem item = ItemRegistry.create(optionIds[i]);
            options[i] = new GameOverlayScreens.RewardOption(
                optionIds[i],
                item.getDisplayName(),
                item.getTier(),
                getItemIconTexture(item.getIconPath())
            );
        }

        return options;
    }

    private GameOverlayScreens.RewardOption[] onRewardRerollRequested() {
        if (!player.spendCoins(20)) {
            return null;
        }
        return pickRewardItemOptions();
    }

    private int pickItemIdByTierWeight(List<Integer> candidateIds, Set<Integer> blockedIds, boolean allowBlocked) {
        boolean equalItemWeights = player.hasEqualItemWeights();
        int totalWeight = 0;
        for (int id : candidateIds) {
            if (!allowBlocked && blockedIds.contains(id)) {
                continue;
            }
            totalWeight += getItemSelectionWeight(id, equalItemWeights);
        }

        if (totalWeight <= 0) {
            return candidateIds.get(itemRnd.nextInt(candidateIds.size()));
        }

        int roll = itemRnd.nextInt(totalWeight);
        for (int id : candidateIds) {
            if (!allowBlocked && blockedIds.contains(id)) {
                continue;
            }

            roll -= getItemSelectionWeight(id, equalItemWeights);
            if (roll < 0) {
                return id;
            }
        }

        return candidateIds.get(candidateIds.size() - 1);
    }

    private int getItemSelectionWeight(int itemId, boolean equalItemWeights) {
        if (equalItemWeights) {
            return 1;
        }
        return ItemRegistry.create(itemId).getTier().getWeight();
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

    @Override
    public void pause() {
        if (inputController != null) {
            inputController.setPaused(true);
        }
    }
}
