package com.codingame.game;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TurnProcessorFuzzTest {

    @Test
    void randomInvalidActionsNeverCorruptBoardOrEnergy() {
        int seeds = Integer.getInteger("bacteria.fuzz.seeds", 80);
        int turns = Integer.getInteger("bacteria.fuzz.turns", 150);

        for (int seed = 0; seed < seeds; seed++) {
            Board board = EngineSimulation.initBoard(seed);
            Random rng = new Random(seed * 17L + 3);
            for (int turn = 1; turn <= turns; turn++) {
                List<TurnProcessor.PlayerSubmission> submissions = new ArrayList<>();
                submissions.add(new TurnProcessor.PlayerSubmission(0, "P0", randomLine(rng, board.size)));
                submissions.add(new TurnProcessor.PlayerSubmission(1, "P1", randomLine(rng, board.size)));
                TurnProcessor.processTurn(board, turn, submissions, s -> {});
                assertTrue(board.energy[0] >= 0, "p0 energy negative seed=" + seed + " turn=" + turn);
                assertTrue(board.energy[1] >= 0, "p1 energy negative seed=" + seed + " turn=" + turn);
            }
        }
    }

    private static String randomLine(Random rng, int size) {
        int count = 1 + rng.nextInt(GameConfig.MAX_ACTIONS_PER_TURN);
        List<String> actions = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int t = rng.nextInt(4);
            int x = rng.nextInt(size + 10) - 5;
            int y = rng.nextInt(size + 10) - 5;
            if (t == 0) actions.add("EXPAND " + x + " " + y);
            if (t == 1) actions.add("ATTACK " + x + " " + y);
            if (t == 2) actions.add("AUTOPHAGY " + x + " " + y);
            if (t == 3) actions.add("WAIT");
        }
        return String.join(";", actions);
    }
}
