package com.example.rougelikegame.android.models.items.passives;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.example.rougelikegame.android.managers.SoundManager;
import com.example.rougelikegame.android.models.characters.Enemy;
import com.example.rougelikegame.android.models.characters.Player;
import com.example.rougelikegame.android.models.items.ItemTier;
import com.example.rougelikegame.android.models.items.PassiveItem;

/**
 * The Lightning Chain item gives a chance for attacks to arc to the nearest enemy on hit.
 */
public class LightningChainItem implements PassiveItem {

    public static final int ID = 16;
    private static final float CHAIN_CHANCE = 0.5f;

    @Override
    public int getItemId() {
        return ID;
    }

    @Override
    public String getKey() {
        return "lightning_chain";
    }

    @Override
    public String getDisplayName() {
        return "Lightning Chain";
    }

    @Override
    public String getIconPath() {
        return "items/lightning_chain.png";
    }

    @Override
    public ItemTier getTier() {
        return ItemTier.A;
    }

    @Override
    public String getDescription() {
        return "Hits have a 50% chance to arc into another nearby enemy.";
    }

    @Override
    public void onHitEnemy(Player player, Enemy enemy, Array<Enemy> enemies, int damageDealt) {
        if (enemies == null || player.getRandomSource().nextFloat() >= CHAIN_CHANCE) {
            return;
        }

        Enemy chainedEnemy = findClosestOtherEnemy(enemy, enemies);
        if (chainedEnemy == null) {
            return;
        }

        chainedEnemy.takeDamage(damageDealt);
        SoundManager.play("hit");
    }

    /**
     * Finds the closest alive enemy to the source enemy.
     * @param sourceEnemy The enemy that was originally hit.
     * @param enemies The list of all enemies.
     * @return The closest other enemy, or null if none found.
     */
    private Enemy findClosestOtherEnemy(Enemy sourceEnemy, Array<Enemy> enemies) {
        Enemy nearest = null;
        float bestDistSq = Float.MAX_VALUE;
        float sourceCenterX = sourceEnemy.getX() + sourceEnemy.width * 0.5f;
        float sourceCenterY = sourceEnemy.getY() + sourceEnemy.height * 0.5f;

        for (int i = 0; i < enemies.size; i++) {
            Enemy candidate = enemies.get(i);
            if (candidate == null || candidate == sourceEnemy || !candidate.alive) {
                continue;
            }

            float candidateCenterX = candidate.getX() + candidate.width * 0.5f;
            float candidateCenterY = candidate.getY() + candidate.height * 0.5f;
            float distSq = Vector2.dst2(sourceCenterX, sourceCenterY, candidateCenterX, candidateCenterY);

            if (distSq < bestDistSq) {
                bestDistSq = distSq;
                nearest = candidate;
            }
        }

        return nearest;
    }
}
