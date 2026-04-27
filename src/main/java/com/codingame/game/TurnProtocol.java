package com.codingame.game;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds init and per-turn input lines. Order must match {@code config/stub.txt}.
 */
public final class TurnProtocol {

    private TurnProtocol() {}

    public static List<String> buildTurnInputLines(TurnInput input) {
        List<String> lines = new ArrayList<>();
        lines.add(String.valueOf(input.myCells.size()));
        for (Coord c : input.myCells) {
            lines.add(c.x + " " + c.y);
        }
        lines.add(String.valueOf(input.oppCells.size()));
        for (Coord c : input.oppCells) {
            lines.add(c.x + " " + c.y);
        }
        lines.add(String.valueOf(input.visibleSpots.size()));
        for (SpotInfo s : input.visibleSpots) {
            lines.add(s.x + " " + s.y + " " + s.typeCode + " " + s.remainingEnergy);
        }
        return lines;
    }

    public static List<String> buildInitInputLines(GameStateSnapshot snapshot, int playerIdx) {
        int half = snapshot.boardSize / 2;
        boolean isP0 = (playerIdx == 0);
        List<String> lines = new ArrayList<>();
        lines.add(snapshot.boardSize + " " + playerIdx);
        List<SpotInfo> ownHalfSpots = new ArrayList<>();
        for (SpotInfo s : snapshot.spots) {
            boolean ownHalf = isP0 ? (s.x < half) : (s.x >= half);
            if (ownHalf) {
                ownHalfSpots.add(s);
            }
        }
        lines.add(String.valueOf(ownHalfSpots.size()));
        for (SpotInfo s : ownHalfSpots) {
            lines.add(s.x + " " + s.y + " " + s.typeCode);
        }
        return lines;
    }
}
