package com.example.rougelikegame.android.models.world;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.example.rougelikegame.android.models.characters.Enemy;
import com.example.rougelikegame.android.models.core.TargetingHelper;

/**
 * Represents a projectile fired by the player or an enemy.
 */
public class Projectile implements Pool.Poolable {
    public float x;
    public float y;
    public float speed = 800f;
    public int damage;
    public boolean alive = true;

    private float life = 1.2f; // seconds
    private final Vector2 dir = new Vector2();
    private final Rectangle bounds = new Rectangle();

    private boolean homingEnabled;
    private float homingRange;
    private float homingStrength;
    private float maxTurnRateDeg;

    private Enemy homingTarget;
    private float retargetTimer = 0f;
    private final Vector2 homingDesired = new Vector2();
    private final Vector2 homingSteered = new Vector2();

    private static final float RETARGET_INTERVAL_SECONDS = 0.1f;

    private static Texture tex; // shared texture

    /**
     * Constructs a basic Projectile.
     */
    public Projectile() {
        if (tex == null) {
            tex = new Texture("pixel.png");
        }
        bounds.set(0, 0, 16, 16);
    }

    /**
     * Initializes a Projectile with optional homing capabilities.
     */
    public void init(float x, float y, float dirX, float dirY, int damage,
                      boolean homingEnabled, float homingRange, float homingStrength, float maxTurnRateDeg) {
        this.damage = damage;
        this.x = x;
        this.y = y;
        this.dir.set(dirX, dirY).nor();

        this.homingEnabled = homingEnabled;
        this.homingRange = Math.max(0f, homingRange);
        this.homingStrength = Math.max(0f, homingStrength);
        this.maxTurnRateDeg = Math.max(0f, maxTurnRateDeg);
        this.alive = true;
        this.life = 1.2f;

        bounds.setPosition(x, y);
    }

    @Override
    public void reset() {
        this.x = 0;
        this.y = 0;
        this.speed = 800f;
        this.damage = 0;
        this.alive = false;
        this.life = 0;
        this.dir.set(0, 0);
        this.bounds.set(0, 0, 16, 16);
        this.homingEnabled = false;
        this.homingRange = 0;
        this.homingStrength = 0;
        this.maxTurnRateDeg = 0;
        this.homingTarget = null;
        this.retargetTimer = 0;
        this.homingDesired.set(0, 0);
        this.homingSteered.set(0, 0);
    }

    /**
     * Constructs a basic Projectile.
     * @deprecated Use pool and init() instead.
     */
    @Deprecated
    public Projectile(float x, float y, float dirX, float dirY, int damage) {
        this(x, y, dirX, dirY, damage, false, 0f, 0f, 0f);
    }

    /**
     * Constructs a Projectile with optional homing capabilities.
     * @deprecated Use pool and init() instead.
     */
    @Deprecated
    public Projectile(float x, float y, float dirX, float dirY, int damage,
                      boolean homingEnabled, float homingRange, float homingStrength, float maxTurnRateDeg) {
        this();
        init(x, y, dirX, dirY, damage, homingEnabled, homingRange, homingStrength, maxTurnRateDeg);
    }

    /**
     * Updates the projectile's position and life.
     * @param delta The time since the last frame.
     */
    public void update(float delta) {
        update(delta, null);
    }

    /**
     * Updates the projectile's position, life, and homing direction.
     * @param delta The time since the last frame.
     * @param enemies The list of potential homing targets.
     */
    public void update(float delta, Array<Enemy> enemies) {
        if (homingEnabled && enemies != null) {
            updateHomingDirection(delta, enemies);
        }

        x += dir.x * speed * delta;
        y += dir.y * speed * delta;

        bounds.setPosition(x, y);

        life -= delta;
        if (life <= 0) {
            alive = false;
        }
    }

    /**
     * Recalculates the direction for homing.
     */
    private void updateHomingDirection(float delta, Array<Enemy> enemies) {
        if (homingRange <= 0f || maxTurnRateDeg <= 0f || homingStrength <= 0f) {
            return;
        }

        retargetTimer -= delta;
        if (retargetTimer <= 0f || !isValidTarget(homingTarget)) {
            homingTarget = TargetingHelper.getNearestEnemyWithinRange(getCenterX(), getCenterY(), homingRange, enemies);
            retargetTimer = RETARGET_INTERVAL_SECONDS;
        }

        if (!isValidTarget(homingTarget)) {
            return;
        }

        homingDesired.set(
            homingTarget.getX() + homingTarget.width * 0.5f - getCenterX(),
            homingTarget.getY() + homingTarget.height * 0.5f - getCenterY()
        );

        if (homingDesired.isZero(0.01f)) {
            return;
        }

        homingDesired.nor();
        homingSteered.set(dir).lerp(homingDesired, MathUtils.clamp(homingStrength * delta, 0f, 1f)).nor();

        float cross = dir.crs(homingSteered);
        float dot = dir.dot(homingSteered);
        float requestedTurnRad = (float) Math.atan2(cross, dot);

        float maxTurnRad = maxTurnRateDeg * MathUtils.degreesToRadians * delta;
        float clampedTurnRad = MathUtils.clamp(requestedTurnRad, -maxTurnRad, maxTurnRad);

        dir.rotateRad(clampedTurnRad).nor();
    }

    /**
     * Checks if an enemy is a valid target for homing.
     */
    private boolean isValidTarget(Enemy enemy) {
        if (enemy == null || !enemy.alive) {
            return false;
        }

        float enemyCenterX = enemy.getX() + enemy.width * 0.5f;
        float enemyCenterY = enemy.getY() + enemy.height * 0.5f;
        float distSq = Vector2.dst2(getCenterX(), getCenterY(), enemyCenterX, enemyCenterY);
        return distSq <= homingRange * homingRange;
    }

    private float getCenterX() {
        return x + bounds.width * 0.5f;
    }

    private float getCenterY() {
        return y + bounds.height * 0.5f;
    }

    /**
     * Draws the projectile.
     * @param batch The Batch to draw with.
     */
    public void draw(Batch batch) {
        if (tex != null) {
            batch.draw(tex, x, y, 16, 16);
        }
    }

    public Rectangle getBounds() {
        return bounds;
    }

    /**
     * Returns the shared projectile texture.
     * @return The Texture instance.
     */
    public static Texture getTexture() {
        if (tex == null) {
            tex = new Texture("pixel.png");
        }
        return tex;
    }

    /**
     * Disposes of the shared projectile texture.
     */
    public static void disposeTexture() {
        if (tex != null) {
            tex.dispose();
            tex = null;
        }
    }
}
