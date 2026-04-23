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
     * Hard-capped by the game engine: each player {@code execute()} adds at least 50ms to an internal
     * total (see {@code GameManager.addTurnTime}); with two players, {@code 2 * MAX_TURNS * 50} must stay ≤ 30000.
     */
    public static final int MAX_TURNS = 300;

    public static final int MIN_SPOTS = 8;
    public static final int MAX_SPOTS = 12;
    public static final int MIN_SPOT_DISTANCE = 5;
    /** Chebyshev distance from (0,0) for first-half spot candidates; mirror covers P1 corner. */
    public static final int MIN_SPAWN_CLEARANCE = 4;
}
