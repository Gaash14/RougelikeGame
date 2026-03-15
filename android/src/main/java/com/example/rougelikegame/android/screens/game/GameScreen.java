package com.example.rougelikegame.android.screens.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.example.rougelikegame.android.managers.AchievementManager;
import com.example.rougelikegame.android.managers.SoundManager;
import com.example.rougelikegame.android.models.characters.Enemy;
import com.example.rougelikegame.android.models.characters.EnemyFactory;
import com.example.rougelikegame.android.models.characters.Player;
import com.example.rougelikegame.android.models.core.CombatSystem;
import com.example.rougelikegame.android.models.core.GameState;
import com.example.rougelikegame.android.models.core.ScoreReporter;
import com.example.rougelikegame.android.models.input.GameInputController;
import com.example.rougelikegame.android.models.input.Joystick;
import com.example.rougelikegame.android.models.items.ItemRegistry;
import com.example.rougelikegame.android.models.items.PassiveItem;
import com.example.rougelikegame.android.models.items.contexts.DamageContext;
import com.example.rougelikegame.android.models.meta.RunStats;
import com.example.rougelikegame.android.models.world.Obstacle;
import com.example.rougelikegame.android.models.world.Pickup;
import com.example.rougelikegame.android.models.world.ProjectileSystem;
import com.example.rougelikegame.android.models.world.WaveManager;
import com.example.rougelikegame.android.models.world.WaveSpawner;

import java.util.Random;

public class GameScreen extends ApplicationAdapter implements WaveSpawner {

    private final ScoreReporter scoreReporter;
    private final Player.PlayerClass selectedClass;
    private final Player.Difficulty difficulty;
    private final String skinId;
    private final boolean dailyChallenge;
    private final long runSeed;
    private boolean runReported = false;

    // Core LibGDX
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private Stage stage;
    private BitmapFont font;
    private Joystick joystick;

    // Systems & Managers
    private GameAssets assets;
    private GameRenderer renderer;
    private GameSpawner spawner;
    private RewardOverlay rewardOverlay;
    private WaveManager waveManager;
    private ProjectileSystem projectileSystem;
    private CombatSystem combatSystem;
    private GameInputController inputController;
    private final RunStats runStats = new RunStats();
    private final AchievementManager achievementManager = AchievementManager.getInstance();

    // Game Objects
    private Player player;
    private Array<Enemy> enemies;
    private Array<Pickup> pickups;
    private Array<Obstacle> obstacles;
    private Random itemRnd, pickupRnd, enemyRnd, miscRnd;

    // UI Bounds
    private Rectangle attackBtnBounds, resumeBounds, exitBounds;
    private boolean bossDefeated = false;
    private boolean rewardScreenActive = false;
    private int pendingWaveToSpawn = -1;

    public GameScreen(ScoreReporter scoreReporter, Player.PlayerClass selectedClass, Player.Difficulty difficulty, String skinId, boolean dailyChallenge, long runSeed) {
        this.scoreReporter = scoreReporter;
        this.selectedClass = selectedClass;
        this.difficulty = difficulty;
        this.skinId = skinId;
        this.dailyChallenge = dailyChallenge;
        this.runSeed = runSeed;
    }

    public GameScreen(ScoreReporter scoreReporter) {
        this(scoreReporter, Player.PlayerClass.MELEE, Player.Difficulty.NORMAL, "default", false, System.currentTimeMillis());
    }

    public GameScreen() {
        this(null, Player.PlayerClass.MELEE, Player.Difficulty.NORMAL, "default", false, System.currentTimeMillis());
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        SoundManager.load();

        GameState gameState = new GameState(runSeed);
        itemRnd = gameState.getItemRandom();
        pickupRnd = gameState.getPickupRandom();
        enemyRnd = gameState.getEnemyRandom();
        miscRnd = gameState.getMiscRandom();

        setupCamera();
        assets = new GameAssets(skinId);
        setupFont();
        setupGameObjects();
        setupUI();
        setupInput();

        Gdx.input.setCatchKey(Input.Keys.BACK, true);
    }

    private void setupCamera() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    private void setupFont() {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Roboto-Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
        param.size = 48;
        param.color = Color.WHITE;
        param.minFilter = Texture.TextureFilter.Linear;
        param.magFilter = Texture.TextureFilter.Linear;
        font = generator.generateFont(param);
        generator.dispose();
    }

    private void setupGameObjects() {
        player = new Player(100, 100, miscRnd);
        player.setTexture(assets.playerTexture);
        player.playerClass = selectedClass;

        enemies = new Array<>();
        pickups = new Array<>();
        obstacles = new Array<>();
        projectileSystem = new ProjectileSystem();
        waveManager = new WaveManager();

        EnemyFactory enemyFactory = new EnemyFactory(assets.enemyTexture, assets.ghostTexture, assets.bossTexture, projectileSystem.getEnemyProjectiles());
        spawner = new GameSpawner(enemyFactory, waveManager, enemyRnd, pickupRnd, miscRnd, enemies, pickups, obstacles);

        combatSystem = new CombatSystem(new CombatSystem.Callbacks() {
            @Override public void onPlayerDied() { GameScreen.this.onPlayerDied(); }
            @Override public void onBossDefeated() { GameScreen.this.onBossDefeat(); }
            @Override public void onPlayerDamaged(int d) { GameScreen.this.onPlayerDamaged(d); }
            @Override public void onEnemyKilled(Enemy e) {
                SoundManager.play("enemy_death");
                runStats.addKill();
                if (runStats.getEnemiesKilled() >= 100) achievementManager.unlock("kills_100");
            }
        });

        renderer = new GameRenderer(batch, font, assets);
        rewardOverlay = new RewardOverlay(player, itemRnd, font, assets, this::onRewardItemSelected);

        spawner.spawnWave(waveManager.getWave(), enemies, player, difficulty, true);
    }

    private void setupUI() {
        stage = new Stage(new ScreenViewport());
        joystick = new Joystick("inputs/joystick_base.png", "inputs/joystick_knob.png", 100);

        float centerX = Gdx.graphics.getWidth() / 2f;
        resumeBounds = new Rectangle(centerX - 150, Gdx.graphics.getHeight() / 2f + 20, 300, 80);
        exitBounds = new Rectangle(centerX - 150, Gdx.graphics.getHeight() / 2f - 100, 300, 80);
        attackBtnBounds = new Rectangle(Gdx.graphics.getWidth() - 250, 50, 200, 200);
    }

    private void setupInput() {
        inputController = new GameInputController(camera, stage, joystick, player, enemies, attackBtnBounds, resumeBounds, exitBounds, new GameInputController.ProjectileSpawner() {
            @Override public void spawnProjectile(float x, float y, Vector2 dir, int dmg) { GameScreen.this.spawnProjectile(x, y, dir, dmg); }
            @Override public void spawnBeam(Vector2 dir, float charge) { GameScreen.this.spawnBeam(dir, charge); }
        }, this::exitRun);
        Gdx.input.setInputProcessor(inputController.buildProcessor());
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        if (!bossDefeated && !rewardScreenActive) runStats.addTime(delta);

        ScreenUtils.clear(0.05f, 0.05f, 0.08f, 1);

        if (!rewardScreenActive && !inputController.isPaused()) {
            update(delta);
        } else if (rewardScreenActive) {
            rewardOverlay.update(delta);
        }

        renderer.render(player, enemies, pickups, obstacles, projectileSystem, waveManager, joystick, inputController, attackBtnBounds, resumeBounds, exitBounds, runSeed, dailyChallenge);

        if (rewardScreenActive) {
            rewardOverlay.draw(batch);
        }
    }

    private void update(float delta) {
        player.update(joystick, delta);
        inputController.update(delta);
        player.handleObstacleCollision(obstacles);

        for (Enemy e : enemies) {
            e.update(delta, player.x, player.y);
            e.handleObstacleCollision(obstacles);
        }

        projectileSystem.update(delta, enemies);
        projectileSystem.handlePlayerProjectilesHitEnemies(player, enemies);
        projectileSystem.handleEnemyProjectilesHitPlayer(player, this::onPlayerDied, this::onPlayerDamaged);

        checkPickups();
        combatSystem.preventEnemyOverlap(enemies);
        combatSystem.handlePlayerEnemyCollision(player, enemies);
        combatSystem.handleAttackDamage(player, enemies);
        combatSystem.cleanupDeadEnemies(enemies);

        waveManager.update(delta, enemies, player, difficulty, spawner, this::onWaveStarted);
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
            case HEALTH: player.health += 4; break;
            case SPEED: player.speed += 10; break;
            case COIN:
                player.addCoins(1);
                if (player.coins >= 25) achievementManager.unlock("coins_25");
                break;
        }
    }

    private void spawnProjectile(float x, float y, Vector2 dir, int baseDamage) {
        SoundManager.play("shoot");
        DamageContext ctx = new DamageContext(baseDamage);
        for (PassiveItem it : player.getPassiveItems()) it.modifyProjectileDamage(player, ctx);
        projectileSystem.spawnPlayerProjectile(x, y, dir, ctx.damage, player.getEffectiveHomingContext());
    }

    private void spawnBeam(Vector2 dir, float chargePercent) {
        DamageContext ctx = new DamageContext(player.getCurrentDamage());
        for (PassiveItem it : player.getPassiveItems()) {
            it.modifyProjectileDamage(player, ctx);
            it.modifyMeleeDamage(player, ctx);
        }
        int beamTickDamage = Math.max(1, Math.round(ctx.damage * Math.max(0f, Math.min(chargePercent, 1f))));
        projectileSystem.spawnBeam(player.x + player.width / 2f, player.y + player.height / 2f, dir, beamTickDamage);
        player.meleeCooldown = player.getEffectiveMeleeCooldownTime();
        player.triggerRangedCooldown();
    }

    private void onPlayerDamaged(int damageTaken) {
        for (PassiveItem item : player.getPassiveItems()) item.onPlayerDamaged(player, damageTaken);
    }

    private void onRewardItemSelected(int itemId) {
        player.addPassiveItem(ItemRegistry.create(itemId));
        runStats.addItemPicked();
        rewardScreenActive = false;
        player.giveImmunity(1.0f);
        spawner.spawnWave(pendingWaveToSpawn, enemies, player, difficulty, true);
        spawner.spawnWavePickups(pendingWaveToSpawn, player);
        pendingWaveToSpawn = -1;
        Gdx.input.setInputProcessor(inputController.buildProcessor());
    }

    private boolean onWaveStarted(int waveNumber) {
        if (waveNumber % 3 != 0) return false;
        pendingWaveToSpawn = waveNumber;
        rewardScreenActive = true;
        rewardOverlay.show(waveNumber);
        return true;
    }

    private void onPlayerDied() { finishRun(bossDefeated); Gdx.app.postRunnable(Gdx.app::exit); }
    private void onBossDefeat() { bossDefeated = true; achievementManager.unlock("first_win"); }
    private void exitRun() { finishRun(bossDefeated); Gdx.app.exit(); }

    private void finishRun(boolean win) {
        if (runReported) return;
        runReported = true;
        if (scoreReporter != null) {
            scoreReporter.reportRun(waveManager.getWave(), win ? (int) runStats.getRunTime() : 0, runStats.getEnemiesKilled(), runStats.getPickupsPicked(), runStats.getItemsPicked(), player.coins, win, player.playerClass == Player.PlayerClass.RANGED);
        }
    }

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (assets != null) assets.dispose();
        if (font != null) font.dispose();
        if (stage != null) stage.dispose();
        if (rewardOverlay != null) rewardOverlay.dispose();
        if (joystick != null) joystick.dispose();
        for (Enemy e : enemies) e.dispose();
        for (Pickup p : pickups) p.dispose();
        for (Obstacle o : obstacles) o.dispose();
        if (projectileSystem != null) projectileSystem.disposeShared();
        player.dispose();
        SoundManager.dispose();
    }

    @Override public void pause() { if (inputController != null) inputController.setPaused(true); }
    @Override public void spawnWave(int w, Array<Enemy> e, Player p, Player.Difficulty d, boolean b) { spawner.spawnWave(w, e, p, d, b); }
    @Override public void spawnWavePickups(int w, Player p) { spawner.spawnWavePickups(w, p); }

    public void spawnBossReinforcements() {
        spawner.spawnBossReinforcements(player, difficulty);
    }

    public void spawnCoinAtPlayerPosition() {
        pickups.add(new Pickup(Pickup.Type.COIN, player.x, player.y));
    }

    public Player getPlayer() { return player; }
    public Player.Difficulty getDifficulty() { return difficulty; }
}
