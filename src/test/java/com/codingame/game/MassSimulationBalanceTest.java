package com.codingame.game;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MassSimulationBalanceTest {

    @Test
    void randomVsRandom_hasNoStrongStartingBias() {
        int games = Integer.getInteger("bacteria.mass.games", 1000);
        int p0Wins = 0;
        int p1Wins = 0;
        int draws = 0;
        long totalTurns = 0;

        for (int i = 0; i < games; i++) {
            EngineSimulation.MatchResult r = EngineSimulation.run(
                i + 1L,
                100,
                EngineSimulation.randomBot(111),
                EngineSimulation.randomBot(222)
            );
            totalTurns += r.turns;
            if (r.winner == 0) p0Wins++;
            else if (r.winner == 1) p1Wins++;
            else draws++;
        }

        int decisive = p0Wins + p1Wins;
        double bias = decisive == 0 ? 0.0 : Math.abs((double) p0Wins / decisive - 0.5);
        double avgTurns = (double) totalTurns / games;

        assertTrue(bias <= 0.08, "player bias too strong, bias=" + bias + ", p0=" + p0Wins + ", p1=" + p1Wins + ", draws=" + draws);
        assertTrue(avgTurns >= 50 && avgTurns <= 100, "average duration out of target range: " + avgTurns);
    }
}
