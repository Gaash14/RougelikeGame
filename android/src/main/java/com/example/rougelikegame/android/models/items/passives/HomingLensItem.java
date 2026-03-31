package com.example.rougelikegame.android.models.items.passives;

import com.example.rougelikegame.android.models.characters.Player;
import com.example.rougelikegame.android.models.items.ItemTier;
import com.example.rougelikegame.android.models.items.PassiveItem;
import com.example.rougelikegame.android.models.items.contexts.HomingContext;

/**
 * The Homing Lens item enables and configures homing behavior for both ranged and melee attacks.
 */
public class HomingLensItem implements PassiveItem {

    public static final int ID = 12;

    public static final float HOMING_RANGE = 360f;
    public static final float HOMING_STRENGTH = 2.8f;
    public static final float MAX_TURN_RATE_DEG = 220f;

    public static final float MELEE_AIM_ASSIST_RANGE = 240f;
    public static final float MELEE_AIM_ASSIST_STRENGTH = 0.25f;

    @Override
    public int getItemId() {
        return ID;
    }

    @Override
    public String getKey() {
        return "homing_lens";
    }

    @Override
    public String getDisplayName() {
        return "Homing Lens";
    }

    @Override
    public String getDescription() {
        return "Projectiles and melee attacks gain homing assist.";
    }

    @Override
    public String getIconPath() {
        return "items/homing_lens.png";
    }

    @Override
    public ItemTier getTier() {
        return ItemTier.S;
    }

    @Override
    public void modifyHoming(Player player, HomingContext ctx) {
        ctx.enabled = true;
        ctx.homingRange = Math.max(ctx.homingRange, HOMING_RANGE);
        ctx.homingStrength = Math.max(ctx.homingStrength, HOMING_STRENGTH);
        ctx.maxTurnRateDeg = Math.max(ctx.maxTurnRateDeg, MAX_TURN_RATE_DEG);
        ctx.meleeAimAssistRange = Math.max(ctx.meleeAimAssistRange, MELEE_AIM_ASSIST_RANGE);
        ctx.meleeAimAssistStrength = Math.max(ctx.meleeAimAssistStrength, MELEE_AIM_ASSIST_STRENGTH);
    }
}
