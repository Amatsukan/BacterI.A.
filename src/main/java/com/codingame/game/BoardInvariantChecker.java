package com.codingame.game;

public final class BoardInvariantChecker {

    private BoardInvariantChecker() {}

    public static void assertValid(Board board, String checkpoint) {
        if (board.energy[0] < 0 || board.energy[1] < 0) {
            throw new IllegalStateException(
                "Invalid energy at " + checkpoint + ": p0=" + board.energy[0] + ", p1=" + board.energy[1]);
        }

        int p0Count = 0;
        int p1Count = 0;
        for (int y = 0; y < board.size; y++) {
            for (int x = 0; x < board.size; x++) {
                int owner = board.cells[y][x];
                if (owner < Board.EMPTY || owner > Board.PLAYER1) {
                    throw new IllegalStateException(
                        "Corrupted cell owner " + owner + " at " + x + "," + y + " (" + checkpoint + ")");
                }
                if (owner == Board.PLAYER0) p0Count++;
                if (owner == Board.PLAYER1) p1Count++;
            }
        }

        if (p0Count != board.playerCells[0].size() || p1Count != board.playerCells[1].size()) {
            throw new IllegalStateException(
                "Grid/set mismatch at " + checkpoint +
                    " gridCounts=(" + p0Count + "," + p1Count + ")" +
                    " setCounts=(" + board.playerCells[0].size() + "," + board.playerCells[1].size() + ")");
        }
    }
}
