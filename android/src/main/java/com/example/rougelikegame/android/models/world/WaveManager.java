package com.example.rougelikegame.android.models.world;

import com.badlogic.gdx.utils.Array;
import com.example.rougelikegame.android.managers.AchievementManager;
import com.example.rougelikegame.android.models.characters.Enemy;
import com.example.rougelikegame.android.models.characters.Player;

/**
 * WaveManager handles the progression of enemy waves in the game.
 * It tracks the current wave number, manages the timing between waves,
 * and triggers wave-related achievements and events.
 */
public class WaveManager {

    /**
     * Listener interface for wave start events.
     */
    public interface WaveStartListener {
        /**
         * Called when a new wave starts.
         *
         * @param waveNumber The number of the wave that is starting
         * @return true if the wave start is blocked (e.g., by a reward screen), false otherwise
         */
        boolean onWaveStarted(int waveNumber);
    }

    private static final int BOSS_WAVE = 7;

    private final AchievementManager achievementManager;

    private int wave = 1;

    private final float timeBetweenWaves = 2f;
    private float waveTimer = 0f;
    private boolean waitingForNextWave = false;

    /**
     * Creates a new WaveManager and initializes the AchievementManager reference.
     */
    public WaveManager() {
        this.achievementManager = AchievementManager.getInstance();
    }

    /**
     * @return The current wave number.
     */
    public int getWave() {
        return wave;
    }

    /**
     * Updates the wave progression logic.
     *
     * @param delta Time elapsed since the last frame
     * @param enemies List of active enemies
     * @param player The player object
     * @param difficulty Current game difficulty
     * @param spawner The WaveSpawner used to spawn new enemies
     * @param waveStartListener Listener for wave start events
     */
    public void update(
        float delta,
        Array<Enemy> enemies,
        Player player,
        Player.Difficulty difficulty,
        WaveSpawner spawner,
        WaveStartListener waveStartListener
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

                boolean blockedByReward = false;
                if (waveStartListener != null) {
                    blockedByReward = waveStartListener.onWaveStarted(wave);
                }

                if (!blockedByReward) {
                    player.giveImmunity(1.0f); // 1 second wave-start immunity

                    spawner.spawnWave(wave, enemies, player, difficulty, true);
                    spawner.spawnWavePickups(wave);
                }

                waitingForNextWave = false;
            }
        }
    }

    /**
     * @return true if the current wave is a boss wave.
     */
    public boolean isBossWave() {
        return wave == BOSS_WAVE;
    }
}
