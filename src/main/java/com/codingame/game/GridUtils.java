package com.codingame.game;

public final class GridUtils {

    private GridUtils() {}

    public static boolean isAdjacent(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) <= 1 && Math.abs(y1 - y2) <= 1 && !(x1 == x2 && y1 == y2);
    }

    public static boolean isInBounds(int x, int y, int size) {
        return x >= 0 && x < size && y >= 0 && y < size;
    }

    public static int chebyshevDistance(int x1, int y1, int x2, int y2) {
        return Math.max(Math.abs(x1 - x2), Math.abs(y1 - y2));
    }

    public static boolean hasAdjacentOwnCell(Board board, int playerIdx, int x, int y) {
        int owner = playerIdx + 1;
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                if (dx == 0 && dy == 0) continue;
                int nx = x + dx, ny = y + dy;
                if (isInBounds(nx, ny, board.size) && board.cells[ny][nx] == owner) {
                    return true;
                }
            }
        }
        return false;
    }

    public static int countNeighbors(Board board, int playerIdx, int x, int y) {
        int owner = playerIdx + 1;
        int count = 0;
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                if (dx == 0 && dy == 0) continue;
                int nx = x + dx, ny = y + dy;
                if (isInBounds(nx, ny, board.size) && board.cells[ny][nx] == owner) {
                    count++;
                }
            }
        }
        return count;
    }
}
