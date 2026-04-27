package com.codingame.game;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class EnginePerformanceBaselineTest {

    @Test
    void thousandsOfGamesRunWithoutDegradation() {
        int games = Integer.getInteger("bacteria.perf.games", 1200);
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < games; i++) {
            EngineSimulation.run(
                10_000L + i,
                80,
                EngineSimulation.randomBot(7),
                EngineSimulation.randomBot(9)
            );
        }
        long elapsed = System.currentTimeMillis() - t0;
        assertTrue(elapsed < 45_000, "performance baseline exceeded: " + elapsed + "ms for " + games + " games");
    }
}
