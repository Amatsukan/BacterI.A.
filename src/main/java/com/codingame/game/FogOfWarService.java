package com.codingame.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class FogOfWarService {

    private FogOfWarService() {}

    public static PlayerView buildPlayerView(GameStateSnapshot snapshot, int playerIdx) {
        int oppIdx = 1 - playerIdx;
        int half = snapshot.boardSize / 2;
        int size = snapshot.boardSize;
        List<Coord> myCells = new ArrayList<>(snapshot.cellsByPlayer[playerIdx]);
        List<Coord> oppCells = new ArrayList<>();
        List<SpotInfo> visibleSpots = new ArrayList<>();

        boolean[][] visible = new boolean[size][size];
        for (Coord c : myCells) {
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

        for (Coord opp : snapshot.cellsByPlayer[oppIdx]) {
            if (visible[opp.y][opp.x]) {
                oppCells.add(opp);
            }
        }

        boolean isPlayer0 = (playerIdx == 0);
        for (SpotInfo spot : snapshot.spots) {
            boolean ownHalf = isPlayer0 ? (spot.x < half) : (spot.x >= half);
            if (ownHalf || visible[spot.y][spot.x]) {
                visibleSpots.add(spot);
            }
        }

        Collections.sort(myCells);
        Collections.sort(oppCells);
        visibleSpots.sort((a, b) -> {
            int byY = Integer.compare(a.y, b.y);
            if (byY != 0) return byY;
            return Integer.compare(a.x, b.x);
        });
        return new PlayerView(
            playerIdx,
            myCells,
            oppCells,
            visibleSpots,
            snapshot.energyByPlayer[playerIdx],
            snapshot.energyByPlayer[oppIdx]
        );
    }

    /** Legacy adapter kept for existing tests and wrappers. */
    public static VisibleState getVisibleEntities(Board board, int playerIdx) {
        GameStateSnapshot snapshot = GameStateSnapshot.fromBoard(board, 0);
        PlayerView view = buildPlayerView(snapshot, playerIdx);
        VisibleState vs = new VisibleState();
        for (Coord c : view.myCells) {
            vs.myCells.add(new Board.Point(c.x, c.y));
        }
        for (Coord c : view.oppCells) {
            vs.oppCells.add(new Board.Point(c.x, c.y));
        }
        for (SpotInfo s : view.visibleSpots) {
            Board.NutrientSpot spot = new Board.NutrientSpot(s.x, s.y, Board.SpotType.fromCode(s.typeCode));
            spot.remainingEnergy = s.remainingEnergy;
            vs.visibleSpots.add(spot);
        }
        return vs;
    }
}
