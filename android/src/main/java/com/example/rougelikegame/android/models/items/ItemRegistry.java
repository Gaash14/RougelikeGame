package com.example.rougelikegame.android.models.items;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ItemRegistry {

    private static final Map<Integer, Supplier<PassiveItem>> registry = new HashMap<>();

    static {
        register(DamageUpItem.ID, DamageUpItem::new);
        register(PoisonItem.ID, PoisonItem::new);
    }

    private static void register(int id, Supplier<PassiveItem> supplier) {
        registry.put(id, supplier);
    }

    public static PassiveItem create(int id) {
        Supplier<PassiveItem> supplier = registry.get(id);
        if (supplier == null) throw new IllegalArgumentException("Unknown item id: " + id);
        return supplier.get();
    }
}
