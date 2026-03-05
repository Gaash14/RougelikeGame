package com.example.rougelikegame.android.models.items;

public enum ItemTier {
    S(3),
    A(7),
    B(15),
    C(25),
    D(30);

    private final int weight;

    ItemTier(int weight) {
        this.weight = weight;
    }

    public int getWeight() {
        return weight;
    }
}
