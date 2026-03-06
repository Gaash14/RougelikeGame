package com.example.rougelikegame.android.models.items;

import com.example.rougelikegame.android.models.items.passives.AbyssalBlessingItem;
import com.example.rougelikegame.android.models.items.passives.ArmorItem;
import com.example.rougelikegame.android.models.items.passives.AttackSpeedItem;
import com.example.rougelikegame.android.models.items.passives.BeamItem;
import com.example.rougelikegame.android.models.items.passives.BurningCoreItem;
import com.example.rougelikegame.android.models.items.passives.DamageUpItem;
import com.example.rougelikegame.android.models.items.passives.DarkBargainItem;
import com.example.rougelikegame.android.models.items.passives.HomingLensItem;
import com.example.rougelikegame.android.models.items.passives.OverclockInjectorItem;
import com.example.rougelikegame.android.models.items.passives.PiggyBankItem;
import com.example.rougelikegame.android.models.items.passives.PoisonItem;
import com.example.rougelikegame.android.models.items.passives.ShekelBillItem;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class ItemRegistry {

    private static final Map<Integer, Supplier<PassiveItem>> registry = new HashMap<>();

    static {
        register(DamageUpItem.ID, DamageUpItem::new);
        register(PoisonItem.ID, PoisonItem::new);
        register(AttackSpeedItem.ID, AttackSpeedItem::new);
        register(ArmorItem.ID, ArmorItem::new);
        register(BeamItem.ID, BeamItem::new);
        register(BurningCoreItem.ID, BurningCoreItem::new);
        register(ShekelBillItem.ID, ShekelBillItem::new);
        register(OverclockInjectorItem.ID, OverclockInjectorItem::new);
        register(AbyssalBlessingItem.ID, AbyssalBlessingItem::new);
        register(PiggyBankItem.ID, PiggyBankItem::new);
        register(DarkBargainItem.ID, DarkBargainItem::new);
        register(HomingLensItem.ID, HomingLensItem::new);
    }

    private static void register(int id, Supplier<PassiveItem> supplier) {
        registry.put(id, supplier);
    }

    public static PassiveItem create(int id) {
        Supplier<PassiveItem> supplier = registry.get(id);
        if (supplier == null) throw new IllegalArgumentException("Unknown item id: " + id);
        return supplier.get();
    }

    public static Set<Integer> getAllItemIds() {
        return new HashSet<>(registry.keySet());
    }
}
