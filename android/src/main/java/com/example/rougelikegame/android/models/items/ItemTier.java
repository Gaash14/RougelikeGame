package com.example.rougelikegame.android.models.items;

public enum ItemTier {
    S(1),
    A(3),
    B(8),
    C(15),
    D(25);

    private final int weight;

    ItemTier(int weight) {
        this.weight = weight;
    }

    public int getWeight() {
        return weight;
    }
}
