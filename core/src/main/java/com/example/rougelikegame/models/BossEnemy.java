package com.example.rougelikegame.models;

public class BossEnemy extends Enemy {

    public BossEnemy(float startX, float startY, int health, int damage) {
        super("boss.png", startX, startY,70f,
            256f,256f, health, damage);

        this.isBoss = true;
        this.health = health;
    }

    @Override
    public void update(float delta, float playerX, float playerY) {
        // currently has exact same movements as normal enemy; change later
        super.update(delta, playerX, playerY);

        // later add range attacks
    }
}
