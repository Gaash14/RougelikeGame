package com.example.rougelikegame.android.models.items.contexts;

/**
 * Data class for holding homing effect information and parameters.
 */
public class HomingContext {
    public boolean enabled;
    public float homingRange;
    public float homingStrength;
    public float maxTurnRateDeg;
    public float meleeAimAssistRange;
    public float meleeAimAssistStrength;

    /**
     * Default constructor for Firebase/JSON serialization.
     */
    public HomingContext() {
        this.enabled = false;
        this.homingRange = 0f;
        this.homingStrength = 0f;
        this.maxTurnRateDeg = 0f;
        this.meleeAimAssistRange = 0f;
        this.meleeAimAssistStrength = 0f;
    }
}
