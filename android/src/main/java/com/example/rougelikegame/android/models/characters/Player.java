package com.example.rougelikegame.android.models.characters;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.example.rougelikegame.android.models.input.Joystick;
import com.example.rougelikegame.android.models.items.ItemRegistry;
import com.example.rougelikegame.android.models.items.contexts.BlockChanceContext;
import com.example.rougelikegame.android.models.items.contexts.CooldownContext;
import com.example.rougelikegame.android.models.items.contexts.DamageContext;
import com.example.rougelikegame.android.models.items.PassiveItem;
import com.example.rougelikegame.android.models.world.Obstacle;

import java.util.Objects;
import java.util.Random;

public class Player {

    public enum PlayerClass {
        MELEE,
        RANGED
    }
    public PlayerClass playerClass = PlayerClass.MELEE; // default

    public enum Difficulty {
        EASY,
        NORMAL,
        HARD
    }
    public Difficulty difficulty = Difficulty.NORMAL; // default

    // textures
    private Texture texture;
    public final Texture debugPixel = new Texture("pixel.png");

    // position & size
    public float x, y;
    public final float width = 128;
    public final float height = 128;

    // stats
    public int health = 10;
    public int speed = 400;
    public final int maxSpeed = 600;
    public int coins = 0;
    public int attackBonus = 0;

    private final Array<PassiveItem> passiveItems = new Array<>();
    private final Random randomSource;

    // collisions
    public final Rectangle bounds;
    public final Rectangle attackHitbox;

    // damage + knockback (knockback is enemy -> player)
    public float damageCooldown = 0f;
    public float damageCooldownTime = 0.5f;
    public float knockbackX = 0;
    public float knockbackY = 0;
    public float knockbackTime = 0;
    public float knockbackDuration = 0.25f;
    public float knockbackStrength = 350f;

    // base damage values
    public int meleeBaseDamage = 10;
    public int rangedBaseDamage = 6; // weaker than melee

    // melee attack
    public boolean attacking = false;
    private float attackTime = 0f;

    public float meleeCooldown = 0f;
    public float meleeCooldownTime = 0.5f;

    // ranged attack
    public float rangedCooldown = 0f;
    public float rangedCooldownTime = 0.8f; // longer cooldown

    // charge attack
    private boolean charging = false;
    private float chargeTime = 0f;
    private static final float BASE_BEAM_CHARGE_TIME_SECONDS = 2.0f;
    private static final float MIN_BEAM_CHARGE_TIME_SECONDS = 0.15f;

    // immunity
    private float immunityTimer = 0f;
    private float immunityDuration = 0f;
    private boolean immune = false;

    public Player(float x, float y, Random randomSource) {
        this.texture = new Texture("skins/player_default.png");
        this.randomSource = Objects.requireNonNull(randomSource, "randomSource");

        this.x = x;
        this.y = y;

        this.bounds = new Rectangle(x, y, width, height);
        this.attackHitbox = new Rectangle(x, y, 100, 100);
    }

    public void update(Joystick joystick, float delta) {

        // timers

        if (attacking) {
            attackTime -= delta;
            if (attackTime <= 0) attacking = false;
        }

        if (meleeCooldown > 0) meleeCooldown -= delta;
        if (rangedCooldown > 0) rangedCooldown -= delta;
        if (damageCooldown > 0) damageCooldown -= delta;

        updateCharge(delta);

        // immunity
        if (immune) {
            immunityTimer += delta;
            if (immunityTimer >= immunityDuration) {
                immune = false;
            }
        }

        float dx = joystick.getPercentX();
        float dy = joystick.getPercentY();

        // dead zone (prevents tiny unwanted movement)
        if (Math.abs(dx) < 0.15f) dx = 0f;
        if (Math.abs(dy) < 0.15f) dy = 0f;

        // movement / knockback
        if (knockbackTime > 0) {
            knockbackTime -= delta;
            x += knockbackX * knockbackStrength * delta;
            y += knockbackY * knockbackStrength * delta;
        } else {
            x += dx * speed * delta;
            y += dy * speed * delta;
        }

        x = MathUtils.clamp(x, 0, Gdx.graphics.getWidth() - width);
        y = MathUtils.clamp(y, 0, Gdx.graphics.getHeight() - height);

        bounds.setPosition(x, y);
    }

    public int getCurrentDamage() {
        if (playerClass == PlayerClass.MELEE) {
            return meleeBaseDamage + attackBonus;
        } else {
            return rangedBaseDamage + (attackBonus / 2);
        }
    }

    public void handleObstacleCollision(Array<Obstacle> obstacles) {
        for (Obstacle o : obstacles) {
            if (bounds.overlaps(o.bounds)) {

                float overlapX = Math.min(bounds.x + width - o.bounds.x, o.bounds.x + o.bounds.width - bounds.x);
                float overlapY = Math.min(bounds.y + height - o.bounds.y, o.bounds.y + o.bounds.height - bounds.y);

                // Push out in the direction of the smallest overlap
                if (overlapX < overlapY) {
                    if (bounds.x < o.bounds.x) x -= overlapX;
                    else x += overlapX;
                } else {
                    if (bounds.y < o.bounds.y) y -= overlapY;
                    else y += overlapY;
                }

                bounds.setPosition(x, y);
            }
        }
    }

    // ---------- MELEE ----------
    public boolean canMelee() {
        return meleeCooldown <= 0f;
    }

    public void meleeAttack(Joystick joystick) {
        if (!canMelee()) return;

        meleeCooldown = getEffectiveMeleeCooldownTime();
        attacking = true;
        attackTime = 0.15f;

        float dx = joystick.getPercentX();
        float dy = joystick.getPercentY();

        // default right if neutral
        if (Math.abs(dx) < 0.15f && Math.abs(dy) < 0.15f) {
            dx = 1;
            dy = 0;
        }

        attackHitbox.setPosition(
            x + dx * width,
            y + dy * height
        );
    }

    // ---------- RANGED ----------
    public boolean canShoot() {
        return rangedCooldown <= 0f;
    }

    public Vector2 getShootDirection(Joystick joystick) {
        float dx = joystick.getPercentX();
        float dy = joystick.getPercentY();

        if (Math.abs(dx) < 0.15f && Math.abs(dy) < 0.15f) {
            return new Vector2(1, 0); // default right
        }

        return new Vector2(dx, dy).nor();
    }

    public void triggerRangedCooldown() {
        rangedCooldown = getEffectiveRangedCooldownTime();
    }

    public void startCharge() {
        charging = true;
        chargeTime = 0f;
    }

    public void updateCharge(float delta) {
        if (!charging) return;
        chargeTime = Math.min(chargeTime + delta, getEffectiveBeamChargeTimeSeconds());
    }

    public float releaseChargePercent() {
        if (!charging) return 0f;

        charging = false;
        float percent = chargeTime / getEffectiveBeamChargeTimeSeconds();
        chargeTime = 0f;
        return MathUtils.clamp(percent, 0f, 1f);
    }

    public float getFinalAttackSpeed() {
        float effectiveRangedCooldown = getEffectiveRangedCooldownTime();
        if (effectiveRangedCooldown <= 0f) {
            return 1f;
        }
        return Math.max(0.01f, rangedCooldownTime / effectiveRangedCooldown);
    }

    public float getEffectiveBeamChargeTimeSeconds() {
        float attackSpeed = getFinalAttackSpeed();
        float effectiveChargeTime = BASE_BEAM_CHARGE_TIME_SECONDS / Math.max(attackSpeed, 0.01f);
        return Math.max(MIN_BEAM_CHARGE_TIME_SECONDS, effectiveChargeTime);
    }

    public float getBeamChargeProgress() {
        float chargeProgress = chargeTime / getEffectiveBeamChargeTimeSeconds();
        return MathUtils.clamp(chargeProgress, 0f, 1f);
    }

    public boolean isChargingBeam() {
        return charging;
    }

    public float getEffectiveMeleeCooldownTime() {
        CooldownContext ctx = new CooldownContext(meleeCooldownTime);

        for (PassiveItem it : passiveItems) {
            it.modifyMeleeCooldown(this, ctx);
        }

        return ctx.cooldown;
    }

    public float getEffectiveRangedCooldownTime() {
        CooldownContext ctx = new CooldownContext(rangedCooldownTime);

        for (PassiveItem it : passiveItems) {
            it.modifyRangedCooldown(this, ctx);
        }

        return ctx.cooldown;
    }

    public void giveImmunity(float seconds) {
        immune = true;
        immunityDuration = seconds;
        immunityTimer = 0f;
    }

    public boolean isImmune() {
        return immune;
    }

    public float getEffectiveBlockChance() {
        BlockChanceContext ctx = new BlockChanceContext(0f);

        for (PassiveItem it : passiveItems) {
            it.modifyBlockChance(this, ctx);
        }

        return MathUtils.clamp(ctx.chance, 0f, 0.5f);
    }

    public int getEffectiveIncomingDamage(int baseDamage) {
        if (MathUtils.random() < getEffectiveBlockChance()) {
            return 0;
        }

        return baseDamage;
    }

    public int applyIncomingDamage(int baseDamage) {
        int finalDamage = getEffectiveIncomingDamage(baseDamage);
        health -= finalDamage;
        return finalDamage;
    }

    public int getDisplayedDamage() {
        DamageContext ctx = new DamageContext(getCurrentDamage());

        for (PassiveItem it : passiveItems) {
            it.modifyStats(this, ctx);
        }

        return ctx.damage;
    }

    public void addCoins(int amount) {
        coins += amount;
    }

    public void addPassiveItem(PassiveItem item) {
        if (item == null) return;

        if (hasItem(item.getItemId()) && !ownsAllItems()) {
            return;
        }

        passiveItems.add(item);
        item.onPickup(this);
    }

    public boolean hasItem(int itemId) {
        for (PassiveItem item : passiveItems) {
            if (item.getItemId() == itemId) {
                return true;
            }
        }

        return false;
    }

    public int getOwnedItemCount() {
        return passiveItems.size;
    }

    public boolean ownsAllItems() {
        return getOwnedItemCount() >= ItemRegistry.getAllItemIds().size();
    }

    public Array<PassiveItem> getPassiveItems() {
        return passiveItems;
    }

    public Random getRandomSource() {
        return randomSource;
    }

    public void draw(SpriteBatch batch) {
        batch.draw(texture, x, y, width, height);
    }

    public void setTexture(Texture newTexture) {
        if (this.texture != null) {
            this.texture.dispose();
        }
        this.texture = newTexture;
    }

    public Texture getTexture() {
        return texture;
    }

    public void dispose() {
        texture.dispose();
        debugPixel.dispose();
    }
}
