package com.example.rougelikegame.android.models.characters;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import com.example.rougelikegame.android.graphics.FrameAnimationManager;
import com.example.rougelikegame.android.models.world.Projectile;
import com.example.rougelikegame.android.screens.menu.MainActivity;

/**
 * Factory class for creating various types of enemies (Normal, Ghost, Boss).
 * Manages the textures and animation managers needed for enemy instantiation.
 */
public class EnemyFactory {

    private static final String DEFAULT_ENEMY_TEXTURE_PATH = "enemies/enemy.png";
    private static final String GHOST_ENEMY_TEXTURE_PATH = "enemies/ghost_enemy.png";
    private static final String BOSS_ENEMY_TEXTURE_PATH = "enemies/boss.png";

    private final Texture enemyTexture;
    private final Texture ghostTexture;
    private final Texture bossTexture;
    private final Array<Projectile> enemyProjectiles;
    private final FrameAnimationManager animationManager;

    /**
     * Constructs an EnemyFactory.
     *
     * @param enemyTexture     texture for normal enemies
     * @param ghostTexture     texture for ghost enemies
     * @param bossTexture      texture for boss enemies
     * @param animationManager manager for handling enemy animations
     * @param enemyProjectiles array to store projectiles fired by enemies
     */
    public EnemyFactory(
        Texture enemyTexture,
        Texture ghostTexture,
        Texture bossTexture,
        FrameAnimationManager animationManager,
        Array<Projectile> enemyProjectiles
    ) {
        this.enemyTexture = enemyTexture;
        this.ghostTexture = ghostTexture;
        this.bossTexture = bossTexture;
        this.animationManager = animationManager;
        this.enemyProjectiles = enemyProjectiles;
    }

    /**
     * Creates a normal enemy.
     *
     * @param x      starting X position
     * @param y      starting Y position
     * @param hp     initial health
     * @param damage contact damage
     * @return a new Enemy instance
     */
    public Enemy createNormalEnemy(float x, float y, int hp, int damage) {
        Enemy enemy = new Enemy(
            enemyTexture,
            x,
            y,
            100,
            128,
            128,
            hp,
            damage
        );
        enemy.setAnimationManager(animationManager);
        enemy.setAnimationBasePath(DEFAULT_ENEMY_TEXTURE_PATH);
        return enemy;
    }

    /**
     * Creates a ghost enemy.
     *
     * @param x      starting X position
     * @param y      starting Y position
     * @param hp     initial health
     * @param damage contact damage
     * @return a new GhostEnemy instance
     */
    public Enemy createGhostEnemy(float x, float y, int hp, int damage) {
        Enemy enemy = new GhostEnemy(
            ghostTexture,
            x,
            y,
            enemyProjectiles,
            hp,
            damage
        );
        enemy.setAnimationManager(animationManager);
        enemy.setAnimationBasePath(GHOST_ENEMY_TEXTURE_PATH);
        return enemy;
    }

    /**
     * Creates a boss enemy.
     *
     * @param x      starting X position
     * @param y      starting Y position
     * @param hp     initial health
     * @param damage contact damage
     * @param game   reference to MainActivity
     * @return a new BossEnemy instance
     */
    public Enemy createBossEnemy(float x, float y, int hp, int damage, MainActivity game) {
        Enemy enemy = new BossEnemy(
            bossTexture,
            x,
            y,
            enemyProjectiles,
            hp,
            damage,
            game
        );
        enemy.setAnimationManager(animationManager);
        enemy.setAnimationBasePath(BOSS_ENEMY_TEXTURE_PATH);
        return enemy;
    }
}
