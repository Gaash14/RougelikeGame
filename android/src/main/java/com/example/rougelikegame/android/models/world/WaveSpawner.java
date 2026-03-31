package com.example.rougelikegame.android.models.world;

import com.badlogic.gdx.utils.Array;
import com.example.rougelikegame.android.models.characters.Enemy;
import com.example.rougelikegame.android.models.characters.Player;

/**
 * WaveSpawner is an interface for objects capable of spawning enemy waves
 * and wave-related items.
 */
public interface WaveSpawner {
    /**
     * Spawns a new wave of enemies.
     *
     * @param wave The wave number to spawn
     * @param enemies The list to add spawned enemies to
     * @param player The player object (to pass to enemies for targeting)
     * @param difficulty Current game difficulty
     * @param allowBoss Whether bosses are allowed to spawn in this wave
     */
    void spawnWave(int wave, Array<Enemy> enemies, Player player, Player.Difficulty difficulty, boolean allowBoss);

    /**
     * Spawns pickups for the given wave.
     *
     * @param wave The current wave number
     */
    void spawnWavePickups(int wave);
}
