package com.codingame.game;

public final class ActionResolver {

    private ActionResolver() {}

    public static boolean resolveExpand(Board board, int playerIdx, int x, int y) {
        if (!GridUtils.isInBounds(x, y, board.size)) return false;
        if (!board.isEmpty(x, y)) return false;
        if (!GridUtils.hasAdjacentOwnCell(board, playerIdx, x, y)) return false;
        board.placeCell(playerIdx, x, y);
        return true;
    }

    /** Returns energy gained (ATTACK_REWARD on success, 0 on fail). Cost already deducted by caller. */
    public static int resolveAttack(Board board, int attackerIdx, int tx, int ty) {
        if (!GridUtils.isInBounds(tx, ty, board.size)) return 0;
        int defenderIdx = 1 - attackerIdx;
        if (!board.belongsTo(defenderIdx, tx, ty)) return 0;
        if (!GridUtils.hasAdjacentOwnCell(board, attackerIdx, tx, ty)) return 0;

        int attackerNeighbors = GridUtils.countNeighbors(board, attackerIdx, tx, ty);
        int defenderNeighbors = GridUtils.countNeighbors(board, defenderIdx, tx, ty);

        if (attackerNeighbors > defenderNeighbors) {
            board.removeCell(defenderIdx, tx, ty);
            board.placeCell(attackerIdx, tx, ty);
            board.destroyedEnemyCells[attackerIdx]++;
            return GameConfig.ATTACK_REWARD;
        }
        return 0;
    }

    public static boolean resolveAutophagy(Board board, int playerIdx, int x, int y) {
        if (!GridUtils.isInBounds(x, y, board.size)) return false;
        if (!board.belongsTo(playerIdx, x, y)) return false;
        board.removeCell(playerIdx, x, y);
        return true;
    }
}
