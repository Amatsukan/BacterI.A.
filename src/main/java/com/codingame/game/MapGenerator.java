package com.codingame.game;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

public final class MapGenerator {

    private MapGenerator() {}

    public static void generateMap(Board board, Random rng, int size) {
        int spotCount = GameConfig.MIN_SPOTS + rng.nextInt(GameConfig.MAX_SPOTS - GameConfig.MIN_SPOTS + 1);
        int half = size / 2;
        List<Board.NutrientSpot> firstHalf = new ArrayList<>();

        int attempts = 0;
        while (firstHalf.size() < spotCount && attempts < 1000) {
            attempts++;
            int x = 1 + rng.nextInt(half - 2);
            int y = 1 + rng.nextInt(size - 2);
            if (GridUtils.chebyshevDistance(x, y, 0, 0) < GameConfig.MIN_SPAWN_CLEARANCE) {
                continue;
            }
            boolean tooClose = false;
            for (Board.NutrientSpot existing : firstHalf) {
                if (GridUtils.chebyshevDistance(x, y, existing.x, existing.y) < GameConfig.MIN_SPOT_DISTANCE) {
                    tooClose = true;
                    break;
                }
            }
            if (tooClose) continue;

            Board.SpotType type = pickSpotType(rng);
            firstHalf.add(new Board.NutrientSpot(x, y, type));
        }

        for (Board.NutrientSpot s : firstHalf) {
            board.spots.add(s);
            int mx = size - 1 - s.x;
            int my = size - 1 - s.y;
            board.spots.add(new Board.NutrientSpot(mx, my, s.type));
        }

        board.placeCell(0, 0, 0);
        board.placeCell(1, size - 1, size - 1);
    }

    private static Board.SpotType pickSpotType(Random rng) {
        int roll = rng.nextInt(10);
        if (roll < 5) return Board.SpotType.SMALL;
        if (roll < 8) return Board.SpotType.MEDIUM;
        return Board.SpotType.LARGE;
    }

    public static int computeStartingEnergy(Board board, int startX, int startY) {
        int minDist = bfsToNearestSpot(board, startX, startY);
        return Math.max(minDist + 5, 10);
    }

    private static int bfsToNearestSpot(Board board, int sx, int sy) {
        boolean[][] visited = new boolean[board.size][board.size];
        Queue<int[]> queue = new LinkedList<>();
        queue.add(new int[]{sx, sy, 0});
        visited[sy][sx] = true;

        while (!queue.isEmpty()) {
            int[] cur = queue.poll();
            int x = cur[0], y = cur[1], dist = cur[2];
            for (Board.NutrientSpot spot : board.spots) {
                if (spot.x == x && spot.y == y) return dist;
            }
            for (int dy = -1; dy <= 1; dy++) {
                for (int dx = -1; dx <= 1; dx++) {
                    if (dx == 0 && dy == 0) continue;
                    int nx = x + dx, ny = y + dy;
                    if (GridUtils.isInBounds(nx, ny, board.size) && !visited[ny][nx]) {
                        visited[ny][nx] = true;
                        queue.add(new int[]{nx, ny, dist + 1});
                    }
                }
            }
        }
        return 20;
    }
}
