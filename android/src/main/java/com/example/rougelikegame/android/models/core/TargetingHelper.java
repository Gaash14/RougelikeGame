package com.example.rougelikegame.android.models.core;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.example.rougelikegame.android.models.characters.Enemy;

/**
 * The TargetingHelper class provides utility methods for finding and targeting enemies.
 */
public final class TargetingHelper {

    /**
     * Private constructor to prevent instantiation.
     */
    private TargetingHelper() {
    }

    /**
     * Finds the nearest enemy to a specified position.
     *
     * @param fromX   the X coordinate to measure from
     * @param fromY   the Y coordinate to measure from
     * @param enemies the list of active enemies
     * @return the nearest alive enemy, or null if none found
     */
    public static Enemy findNearestEnemy(float fromX, float fromY, Array<Enemy> enemies) {
        return getNearestEnemyWithinRange(fromX, fromY, Float.MAX_VALUE, enemies);
    }

    /**
     * Finds the nearest enemy within a specified range from a position.
     *
     * @param fromX    the X coordinate to measure from
     * @param fromY    the Y coordinate to measure from
     * @param maxRange the maximum distance to consider
     * @param enemies  the list of active enemies
     * @return the nearest alive enemy within range, or null if none found
     */
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
