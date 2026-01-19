package com.example.rougelikegame.android.models.world;

import com.badlogic.gdx.utils.Array;
import com.example.rougelikegame.android.managers.AchievementManager;
import com.example.rougelikegame.android.models.characters.Enemy;
import com.example.rougelikegame.android.models.characters.Player;

public class WaveManager {

    private static final int BOSS_WAVE = 7;

    private final AchievementManager achievementManager;

    private int wave = 1;

    private final float timeBetweenWaves = 2f;
    private float waveTimer = 0f;
    private boolean waitingForNextWave = false;

    public WaveManager() {
        this.achievementManager = AchievementManager.getInstance();
    }

    public int getWave() {
        return wave;
    }

    public void update(
        float delta,
        Array<Enemy> enemies,
        Player player,
        Player.Difficulty difficulty,
        WaveSpawner spawner
    ) {
        if (enemies.size == 0 && !waitingForNextWave) {
            waitingForNextWave = true;
            waveTimer = timeBetweenWaves;
        }

        if (waitingForNextWave) {
            waveTimer -= delta;

            if (waveTimer <= 0) {
                wave++;

                if (wave >= 5) achievementManager.unlock("wave_5");
                if (wave >= 10) achievementManager.unlock("wave_10");

                spawner.spawnWave(wave, enemies, player, difficulty);
                spawner.spawnWavePickups(wave);

                waitingForNextWave = false;
            }
        }
    }

    public boolean isBossWave() {
        return wave == BOSS_WAVE;
    }
}
