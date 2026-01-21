package com.example.rougelikegame.android.models.world;

import com.badlogic.gdx.utils.Array;
import com.example.rougelikegame.android.models.characters.Enemy;
import com.example.rougelikegame.android.models.characters.Player;

public interface WaveSpawner {
    void spawnWave(int wave, Array<Enemy> enemies, Player player, Player.Difficulty difficulty, boolean allowBoss);
    void spawnWavePickups(int wave);
}
