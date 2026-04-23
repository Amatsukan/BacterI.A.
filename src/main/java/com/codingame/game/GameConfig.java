package com.codingame.game;

/**
 * Gameplay constants (balance and protocol limits). Visual-only values stay in {@link View}.
 */
public final class GameConfig {

    private GameConfig() {}

    public static final int MAX_ACTIONS_PER_TURN = 5;
    public static final int EXPAND_COST = 2;
    public static final int ATTACK_COST = 2;
    public static final int ATTACK_REWARD = 3;
    public static final int VISION_RADIUS = 3;

    /**
     * Minimum allowed by {@code GameManager.setTurnMaxTime} in gameengine 4.x (values below 50 throw).
     */
    public static final int SDK_TURN_MAX_MS = 50;

    /**
     * Each {@code player.execute()} adds {@link #SDK_TURN_MAX_MS} to an internal total; above this the SDK throws
     * {@code "Total game duration too long (>30000ms)"} (see {@code GameManager.addTurnTime}).
     */
    public static final int SDK_ACCUMULATED_TURN_TIME_BUDGET_MS = 30_000;

    /** Active players per match (1v1). */
    public static final int MULTIPLAYER_PLAYER_COUNT = 2;

    /**
     * Maximum game turns such that {@code MULTIPLAYER_PLAYER_COUNT * MAX_TURNS * SDK_TURN_MAX_MS <= SDK_ACCUMULATED_TURN_TIME_BUDGET_MS}.
     * If you raise this, lower {@link #SDK_TURN_MAX_MS} is not possible below 50 — you must shorten games or fork the engine.
     */
    public static final int MAX_TURNS =
        SDK_ACCUMULATED_TURN_TIME_BUDGET_MS / (MULTIPLAYER_PLAYER_COUNT * SDK_TURN_MAX_MS);

    /** First-turn wall-clock budget for each player (referee), independent of the 30s accumulated budget. */
    public static final int FIRST_TURN_MAX_MS = 1000;

    public static final int MIN_SPOTS = 8;
    public static final int MAX_SPOTS = 12;
    public static final int MIN_SPOT_DISTANCE = 5;
    /** Chebyshev distance from (0,0) for first-half spot candidates; mirror covers P1 corner. */
    public static final int MIN_SPAWN_CLEARANCE = 4;
}
