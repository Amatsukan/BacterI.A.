package com.codingame.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;

/** Lightweight deterministic simulator for stress tests without GameRunner overhead. */
final class EngineSimulation {

    interface Bot extends BiFunction<GameStateSnapshot, Integer, String> {}

    private EngineSimulation() {}

    static final class MatchResult {
        final int winner;
        final int turns;
        final String finalHash;

        MatchResult(int winner, int turns, String finalHash) {
            this.winner = winner;
            this.turns = turns;
            this.finalHash = finalHash;
        }
    }

    static Board initBoard(long seed) {
        int size = GameConfig.BOARD_SIZE;
        Board board = new Board(size);
        Random rng = new Random(seed);
        GameLogic.generateMap(board, rng, size);
        board.energy[0] = GameLogic.computeStartingEnergy(board, 0, 0);
        board.energy[1] = GameLogic.computeStartingEnergy(board, size - 1, size - 1);
        BoardInvariantChecker.assertValid(board, "sim_init");
        return board;
    }

    static MatchResult run(long seed, int maxTurns, Bot p0, Bot p1) {
        Board board = initBoard(seed);
        int winner = -2;
        int turn = 1;
        for (; turn <= maxTurns; turn++) {
            GameStateSnapshot before = GameStateSnapshot.fromBoard(board, turn);
            String o0 = p0.apply(before, 0);
            String o1 = p1.apply(before, 1);
            List<TurnProcessor.PlayerSubmission> submissions = new ArrayList<>();
            submissions.add(new TurnProcessor.PlayerSubmission(0, "P0", o0));
            submissions.add(new TurnProcessor.PlayerSubmission(1, "P1", o1));
            winner = TurnProcessor.processTurn(board, turn, submissions, s -> {}).victoryResult;
            if (winner != -2) {
                break;
            }
        }
        int usedTurns = Math.min(turn, maxTurns);
        return new MatchResult(winner, usedTurns, hashState(board));
    }

    static Bot randomBot(long seedOffset) {
        return (snapshot, playerIdx) -> {
            long seed = (snapshot.turn * 1315423911L) ^ (playerIdx * 2654435761L) ^ seedOffset;
            Random rng = new Random(seed);
            int actionCount = 1 + rng.nextInt(GameConfig.MAX_ACTIONS_PER_TURN);
            List<String> actions = new ArrayList<>();
            for (int i = 0; i < actionCount; i++) {
                int roll = rng.nextInt(100);
                if (roll < 30) {
                    actions.add("WAIT");
                } else if (roll < 60) {
                    actions.add("EXPAND " + rng.nextInt(snapshot.boardSize) + " " + rng.nextInt(snapshot.boardSize));
                } else if (roll < 90) {
                    actions.add("ATTACK " + rng.nextInt(snapshot.boardSize) + " " + rng.nextInt(snapshot.boardSize));
                } else {
                    actions.add("AUTOPHAGY " + rng.nextInt(snapshot.boardSize) + " " + rng.nextInt(snapshot.boardSize));
                }
            }
            return String.join(";", actions);
        };
    }

    static String hashState(Board board) {
        long hash = 1125899906842597L;
        for (int y = 0; y < board.size; y++) {
            for (int x = 0; x < board.size; x++) {
                hash = 31 * hash + board.cells[y][x];
            }
        }
        hash = 31 * hash + board.energy[0];
        hash = 31 * hash + board.energy[1];
        for (Board.NutrientSpot s : board.spots) {
            hash = 31 * hash + s.x;
            hash = 31 * hash + s.y;
            hash = 31 * hash + s.type.code;
            hash = 31 * hash + s.remainingEnergy;
        }
        return Long.toHexString(hash);
    }
}
