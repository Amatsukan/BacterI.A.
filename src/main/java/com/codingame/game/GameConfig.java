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
     * Upper bound from the SDK accumulated-time budget: {@code floor(30000 / (2 * 50))} = 300.
     * Current match length is intentionally short for CodinGame IDE stability; raise toward this cap when tuning.
     */
    public static final int SDK_MAX_TURNS_BY_TIME_BUDGET =
        SDK_ACCUMULATED_TURN_TIME_BUDGET_MS / (MULTIPLAYER_PLAYER_COUNT * SDK_TURN_MAX_MS);

    /** Short match + small grid to reduce referee/viewer work (CodinGame IDE 10s budget). */
    public static final int MAX_TURNS = 2;

    /** Square grid side (sent as {@code mapSize} in init). */
    public static final int BOARD_SIZE = 16;

    /** First-turn wall-clock budget for each player (referee), independent of the 30s accumulated budget. */
    public static final int FIRST_TURN_MAX_MS = 1000;

    /** Spot counts for {@link #BOARD_SIZE}; keep modest so {@link MapGenerator} can place pairs on small grids. */
    public static final int MIN_SPOTS = 2;
    public static final int MAX_SPOTS = 5;
    public static final int MIN_SPOT_DISTANCE = 3;
    /** Chebyshev distance from (0,0) for first-half spot candidates; mirror covers P1 corner. */
    public static final int MIN_SPAWN_CLEARANCE = 2;
}
