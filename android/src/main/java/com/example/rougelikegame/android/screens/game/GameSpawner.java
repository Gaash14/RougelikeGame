package com.example.rougelikegame.android.screens.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.example.rougelikegame.android.models.characters.Enemy;
import com.example.rougelikegame.android.models.characters.EnemyFactory;
import com.example.rougelikegame.android.models.characters.Player;
import com.example.rougelikegame.android.models.world.Obstacle;
import com.example.rougelikegame.android.models.world.Pickup;
import com.example.rougelikegame.android.models.world.WaveManager;
import com.example.rougelikegame.android.models.world.WaveSpawner;

import java.util.Random;

public class GameSpawner implements WaveSpawner {

    private final EnemyFactory enemyFactory;
    private final WaveManager waveManager;
    private final Random enemyRnd;
    private final Random pickupRnd;
    private final Random miscRnd;
    private final Array<Pickup> pickups;
    private final Array<Obstacle> obstacles;
    private final Array<Enemy> enemies;

    public GameSpawner(
        EnemyFactory enemyFactory,
        WaveManager waveManager,
        Random enemyRnd,
        Random pickupRnd,
        Random miscRnd,
        Array<Enemy> enemies,
        Array<Pickup> pickups,
        Array<Obstacle> obstacles
    ) {
        this.enemyFactory = enemyFactory;
        this.waveManager = waveManager;
        this.enemyRnd = enemyRnd;
        this.pickupRnd = pickupRnd;
        this.miscRnd = miscRnd;
        this.enemies = enemies;
        this.pickups = pickups;
        this.obstacles = obstacles;
    }

    @Override
    public void spawnWave(int waveNumber, Array<Enemy> enemies, Player player, Player.Difficulty difficulty, boolean allowBoss) {
        spawnRandomObstacles(player);

        // Boss wave
        if (allowBoss && waveManager.isBossWave()) {
            float bossWidth = 256;
            float bossHeight = 256;
            float x = Gdx.graphics.getWidth() / 2f - bossWidth / 2f;
            float y = Gdx.graphics.getHeight() / 2f - bossHeight / 2f;

            enemies.add(enemyFactory.createBossEnemy(x, y, calculateEnemyHP(250, true, difficulty, waveNumber), 5, (GameScreen) Gdx.app.getApplicationListener()));
            return;
        }

        // Normal enemies
        int enemyCount = calculateEnemyCount(waveNumber, difficulty);
        float ghostChance = 0.10f;
        int bonusEnemyDamage = waveNumber / 6;

        for (int i = 0; i < enemyCount; i++) {
            float x = enemyRnd.nextInt(Gdx.graphics.getWidth() - 128);
            float y = enemyRnd.nextInt(Gdx.graphics.getHeight() - 128);

            if (Math.abs(x - player.x) < 200 && Math.abs(y - player.y) < 200) {
                i--;
                continue;
            }

            if (enemyRnd.nextFloat() < ghostChance) {
                enemies.add(enemyFactory.createGhostEnemy(x, y, calculateEnemyHP(20, false, difficulty, waveNumber), 1 + bonusEnemyDamage));
            } else {
                enemies.add(enemyFactory.createNormalEnemy(x, y, calculateEnemyHP(30, false, difficulty, waveNumber), 1 + bonusEnemyDamage));
            }
        }
    }

    @Override
    public void spawnWavePickups(int waveNumber, Player player) {
        int numPickups = 2 + waveNumber / 2;

        for (int i = 0; i < numPickups; i++) {
            float x = pickupRnd.nextInt(Gdx.graphics.getWidth() - 64);
            float y = pickupRnd.nextInt(Gdx.graphics.getHeight() - 64);
            pickups.add(new Pickup(getRandomPickupType(player.speed, player.maxSpeed), x, y));
        }
    }

    public void spawnRandomObstacles(Player player) {
        obstacles.clear();
        int numObstacles = 5;
        for (int i = 0; i < numObstacles; i++) {
            float x = miscRnd.nextInt(Gdx.graphics.getWidth() - 128);
            float y = miscRnd.nextInt(Gdx.graphics.getHeight() - 128);
            if (Math.abs(x - player.x) < 200 && Math.abs(y - player.y) < 200) {
                i--;
                continue;
            }
            obstacles.add(new Obstacle(x, y));
        }
    }

    private int calculateEnemyCount(int waveNumber, Player.Difficulty difficulty) {
        int cappedWave = Math.min(waveNumber, 15);
        int baseEnemyCount = 3 + ((cappedWave - 1) * 3) / 2;
        float multiplier = 1f;
        switch (difficulty) {
            case EASY: multiplier = 0.75f; break;
            case HARD: multiplier = 1.35f; break;
        }
        return Math.round(baseEnemyCount * multiplier);
    }

    private int calculateEnemyHP(int baseHP, boolean isBoss, Player.Difficulty difficulty, int wave) {
        float hpMultiplier = 1f;
        switch (difficulty) {
            case EASY: hpMultiplier = 0.8f; break;
            case HARD: hpMultiplier = 1.35f; break;
        }
        int waveBonus = isBoss ? 0 : (wave * 2) - 2;
        return Math.max(1, Math.round((baseHP + waveBonus) * hpMultiplier));
    }

    private Pickup.Type getRandomPickupType(float currentPlayerSpeed, float maxSpeed) {
        Array<Pickup.Type> pool = new Array<>();
        pool.add(Pickup.Type.HEALTH);
        pool.add(Pickup.Type.HEALTH);
        if (currentPlayerSpeed < maxSpeed) pool.add(Pickup.Type.SPEED);
        pool.add(Pickup.Type.COIN);
        return pool.get(pickupRnd.nextInt(pool.size));
    }

    public void spawnBossReinforcements(Player player, Player.Difficulty difficulty) {
        spawnWave(5, enemies, player, difficulty, false);
    }
}
