package com.example.rougelikegame.android.models.items.passives;

import com.badlogic.gdx.Gdx;
import com.example.rougelikegame.android.models.characters.Player;
import com.example.rougelikegame.android.models.items.ItemTier;
import com.example.rougelikegame.android.models.items.PassiveItem;
import com.example.rougelikegame.android.screens.menu.MainActivity;

/**
 * Item that gives a chance to spawn coins when the player takes damage.
 */
public class PiggyBankItem implements PassiveItem {

    public static final int ID = 10;
    private static final float COIN_SPAWN_CHANCE = 0.20f;

    @Override
    public int getItemId() {
        return ID;
    }

    @Override
    public String getKey() {
        return "piggy_bank";
    }

    @Override
    public String getDisplayName() {
        return "Piggy Bank";
    }

    @Override
    public String getDescription() {
        return "Taking damage can spawn extra coins.";
    }

    @Override
    public String getIconPath() {
        return "items/piggy_bank.png";
    }

    @Override
    public ItemTier getTier() {
        return ItemTier.C;
    }

    @Override
    public void onPlayerDamaged(Player player, int damageTaken) {
        if (damageTaken <= 0) {
            return;
        }

        if (player.getRandomSource().nextFloat() < COIN_SPAWN_CHANCE) {
            if (Gdx.app.getApplicationListener() instanceof MainActivity) {
                MainActivity game = (MainActivity) Gdx.app.getApplicationListener();
                game.spawnCoinAtPlayerPosition();
            }
        }
    }
}
