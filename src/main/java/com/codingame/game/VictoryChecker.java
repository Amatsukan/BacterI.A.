package com.codingame.game;

public final class VictoryChecker {

    private VictoryChecker() {}

    public static int computeScore(Board board, int playerIdx) {
        return board.playerCells[playerIdx].size() * 100 + board.energy[playerIdx];
    }

    /**
     * @return winning player index 0/1, -1 draw or both dead, -2 game continues
     */
    public static int checkGameOver(Board board, int turn) {
        if (board.playerCells[0].isEmpty() && board.playerCells[1].isEmpty()) return -1;
        if (board.playerCells[0].isEmpty()) return 1;
        if (board.playerCells[1].isEmpty()) return 0;
        if (turn >= GameConfig.MAX_TURNS) {
            int s0 = computeScore(board, 0);
            int s1 = computeScore(board, 1);
            if (s0 > s1) return 0;
            if (s1 > s0) return 1;
            if (board.destroyedEnemyCells[0] > board.destroyedEnemyCells[1]) return 0;
            if (board.destroyedEnemyCells[1] > board.destroyedEnemyCells[0]) return 1;
            return -1;
        }
        return -2;
    }
}
