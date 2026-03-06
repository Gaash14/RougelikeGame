package com.example.rougelikegame.android.models.items.contexts;

public class IncomingDamageContext {
    public int damage;
    public int maxDamage;

    public IncomingDamageContext(int damage) {
        this.damage = damage;
        this.maxDamage = Integer.MAX_VALUE;
    }
}
