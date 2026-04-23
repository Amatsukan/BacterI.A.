package com.codingame.game;

import java.util.Set;

public final class FogOfWarService {

    private FogOfWarService() {}

    public static VisibleState getVisibleEntities(Board board, int playerIdx) {
        VisibleState vs = new VisibleState();
        Set<Board.Point> own = board.playerCells[playerIdx];
        int oppIdx = 1 - playerIdx;
        int half = board.size / 2;
        int size = board.size;

        boolean[][] visible = new boolean[size][size];
        for (Board.Point c : own) {
            int xMin = Math.max(0, c.x - GameConfig.VISION_RADIUS);
            int xMax = Math.min(size - 1, c.x + GameConfig.VISION_RADIUS);
            int yMin = Math.max(0, c.y - GameConfig.VISION_RADIUS);
            int yMax = Math.min(size - 1, c.y + GameConfig.VISION_RADIUS);
            for (int vy = yMin; vy <= yMax; vy++) {
                for (int vx = xMin; vx <= xMax; vx++) {
                    visible[vy][vx] = true;
                }
            }
        }

        vs.myCells.addAll(own);

        for (Board.Point opp : board.playerCells[oppIdx]) {
            if (visible[opp.y][opp.x]) {
                vs.oppCells.add(opp);
            }
        }

        boolean isPlayer0 = (playerIdx == 0);
        for (Board.NutrientSpot spot : board.spots) {
            boolean ownHalf = isPlayer0 ? (spot.x < half) : (spot.x >= half);
            if (ownHalf || visible[spot.y][spot.x]) {
                vs.visibleSpots.add(spot);
            }
        }

        return vs;
    }
}
