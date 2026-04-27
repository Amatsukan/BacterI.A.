package com.codingame.game;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TurnProcessorDeterminismTest {

    @Test
    void fixedSeedsProduceIdenticalReplayHashes() {
        List<String> runA = new ArrayList<>();
        List<String> runB = new ArrayList<>();

        for (int seed = 1; seed <= 50; seed++) {
            EngineSimulation.MatchResult a = EngineSimulation.run(
                seed,
                120,
                EngineSimulation.randomBot(10),
                EngineSimulation.randomBot(99)
            );
            EngineSimulation.MatchResult b = EngineSimulation.run(
                seed,
                120,
                EngineSimulation.randomBot(10),
                EngineSimulation.randomBot(99)
            );
            runA.add(seed + ":" + a.finalHash + ":" + a.winner + ":" + a.turns);
            runB.add(seed + ":" + b.finalHash + ":" + b.winner + ":" + b.turns);
        }

        assertEquals(runA, runB, "replay snapshots must be deterministic for fixed seeds");
    }
}
