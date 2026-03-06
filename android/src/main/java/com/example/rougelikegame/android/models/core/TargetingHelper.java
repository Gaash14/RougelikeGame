package com.example.rougelikegame.android.models.core;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.example.rougelikegame.android.models.characters.Enemy;

public final class TargetingHelper {

    private TargetingHelper() {
    }

    public static Enemy findNearestEnemy(float fromX, float fromY, Array<Enemy> enemies) {
        return getNearestEnemyWithinRange(fromX, fromY, Float.MAX_VALUE, enemies);
    }

    public static Enemy getNearestEnemyWithinRange(float fromX, float fromY, float maxRange, Array<Enemy> enemies) {
        if (enemies == null || enemies.size == 0 || maxRange <= 0f) {
            return null;
        }

        float maxRangeSq = maxRange * maxRange;
        float bestDistSq = maxRangeSq;
        Enemy nearest = null;

        for (Enemy enemy : enemies) {
            if (enemy == null || !enemy.alive) continue;

            float enemyCenterX = enemy.getX() + enemy.width * 0.5f;
            float enemyCenterY = enemy.getY() + enemy.height * 0.5f;
            float distSq = Vector2.dst2(fromX, fromY, enemyCenterX, enemyCenterY);

            if (distSq < bestDistSq) {
                bestDistSq = distSq;
                nearest = enemy;
            }
        }

        return nearest;
    }
}
