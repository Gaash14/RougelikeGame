package com.example.rougelikegame.android.models.characters;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.example.rougelikegame.android.models.world.Projectile;
import com.example.rougelikegame.android.screens.menu.MainActivity;

import java.util.Random;

public class EnemyFactory {

    private final Texture enemyTexture;
    private final Texture ghostTexture;
    private final Texture bossTexture;
    private final Array<Projectile> enemyProjectiles;
    private final Random rnd = new Random();

    public EnemyFactory(
        Texture enemyTexture,
        Texture ghostTexture,
        Texture bossTexture,
        Array<Projectile> enemyProjectiles
    ) {
        this.enemyTexture = enemyTexture;
        this.ghostTexture = ghostTexture;
        this.bossTexture = bossTexture;
        this.enemyProjectiles = enemyProjectiles;
    }

    public Enemy createNormalEnemy(float x, float y, int hp, int damage) {
        return new Enemy(
            enemyTexture,
            x,
            y,
            100,
            128,
            128,
            hp,
            damage
        );
    }

    public Enemy createGhostEnemy(float x, float y, int hp, int damage) {
        return new GhostEnemy(
            ghostTexture,
            x,
            y,
            enemyProjectiles,
            hp,
            damage
        );
    }

    public Enemy createBossEnemy(float x, float y, int hp, int damage, MainActivity game) {
        return new BossEnemy(
            bossTexture,
            x,
            y,
            enemyProjectiles,
            hp,
            damage,
            game
        );
    }
}
