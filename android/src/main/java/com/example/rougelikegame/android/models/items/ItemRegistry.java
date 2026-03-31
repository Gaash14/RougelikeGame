package com.example.rougelikegame.android.models.items;

import com.example.rougelikegame.android.models.items.passives.AbyssalBlessingItem;
import com.example.rougelikegame.android.models.items.passives.ArmorItem;
import com.example.rougelikegame.android.models.items.passives.AttackSpeedItem;
import com.example.rougelikegame.android.models.items.passives.BeamItem;
import com.example.rougelikegame.android.models.items.passives.BurningCoreItem;
import com.example.rougelikegame.android.models.items.passives.ChaosDiceItem;
import com.example.rougelikegame.android.models.items.passives.DamageUpItem;
import com.example.rougelikegame.android.models.items.passives.DarkBargainItem;
import com.example.rougelikegame.android.models.items.passives.HomingLensItem;
import com.example.rougelikegame.android.models.items.passives.LightningChainItem;
import com.example.rougelikegame.android.models.items.passives.OverclockInjectorItem;
import com.example.rougelikegame.android.models.items.passives.PiggyBankItem;
import com.example.rougelikegame.android.models.items.passives.PoisonItem;
import com.example.rougelikegame.android.models.items.passives.ProtectiveSpellItem;
import com.example.rougelikegame.android.models.items.passives.ShekelBillItem;
import com.example.rougelikegame.android.models.items.passives.TitansCoreItem;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * The ItemRegistry class maintains a registry of all available passive items in the game
 * and provides methods to instantiate them by ID.
 */
public class ItemRegistry {

    private static final Map<Integer, Supplier<PassiveItem>> REGISTRY = new HashMap<>();

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
        register(ProtectiveSpellItem.ID, ProtectiveSpellItem::new);
        register(TitansCoreItem.ID, TitansCoreItem::new);
        register(ChaosDiceItem.ID, ChaosDiceItem::new);
        register(LightningChainItem.ID, LightningChainItem::new);
    }

    /**
     * Registers a new item with its ID and a supplier to create instances.
     *
     * @param id       the unique item ID
     * @param supplier the supplier that creates new instances of the item
     */
    private static void register(int id, Supplier<PassiveItem> supplier) {
        REGISTRY.put(id, supplier);
    }

    /**
     * Creates a new instance of an item based on its ID.
     *
     * @param id the unique item ID
     * @return a new PassiveItem instance
     * @throws IllegalArgumentException if the ID is unknown
     */
    public static PassiveItem create(int id) {
        Supplier<PassiveItem> supplier = REGISTRY.get(id);
        if (supplier == null) {
            throw new IllegalArgumentException("Unknown item id: " + id);
        }
        return supplier.get();
    }

    /**
     * Returns a set of all registered item IDs.
     *
     * @return a set containing all item IDs
     */
    public static Set<Integer> getAllItemIds() {
        return new HashSet<>(REGISTRY.keySet());
    }
}
