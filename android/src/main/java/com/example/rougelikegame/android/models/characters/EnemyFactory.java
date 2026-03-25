package com.example.rougelikegame.android.models.characters;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import com.example.rougelikegame.android.graphics.FrameAnimationManager;
import com.example.rougelikegame.android.models.world.Projectile;
import com.example.rougelikegame.android.screens.menu.MainActivity;

public class EnemyFactory {

    private static final String DEFAULT_ENEMY_TEXTURE_PATH = "enemies/enemy.png";
    private static final String GHOST_ENEMY_TEXTURE_PATH = "enemies/ghost_enemy.png";
    private static final String BOSS_ENEMY_TEXTURE_PATH = "enemies/boss.png";

    private final Texture enemyTexture;
    private final Texture ghostTexture;
    private final Texture bossTexture;
    private final Array<Projectile> enemyProjectiles;
    private final FrameAnimationManager animationManager;

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
