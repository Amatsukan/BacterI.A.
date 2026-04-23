package com.codingame.game;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Guards the CodinGame SDK constraint that broke full-length simulations before MAX_TURNS was derived from it.
 */
class GameConfigSdkBudgetTest {

    @Test
    void maxTurns_respectsGameManagerAccumulatedTimeBudget() {
        long total = (long) GameConfig.MULTIPLAYER_PLAYER_COUNT
            * GameConfig.MAX_TURNS
            * GameConfig.SDK_TURN_MAX_MS;
        assertTrue(total <= GameConfig.SDK_ACCUMULATED_TURN_TIME_BUDGET_MS,
            "2 * MAX_TURNS * turnMaxTime must stay ≤ " + GameConfig.SDK_ACCUMULATED_TURN_TIME_BUDGET_MS + " ms");
    }
}
