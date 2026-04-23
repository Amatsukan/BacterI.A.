package com.codingame.game;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds init and per-turn input lines. Order must match {@code config/stub.txt}.
 */
public final class TurnProtocol {

    private TurnProtocol() {}

    public static List<String> buildTurnInputLines(VisibleState vs) {
        List<String> lines = new ArrayList<>();
        lines.add(String.valueOf(vs.myCells.size()));
        for (Board.Point c : vs.myCells) {
            lines.add(c.x + " " + c.y);
        }
        lines.add(String.valueOf(vs.oppCells.size()));
        for (Board.Point c : vs.oppCells) {
            lines.add(c.x + " " + c.y);
        }
        lines.add(String.valueOf(vs.visibleSpots.size()));
        for (Board.NutrientSpot s : vs.visibleSpots) {
            lines.add(s.x + " " + s.y + " " + s.type.code + " " + s.remainingEnergy);
        }
        return lines;
    }

    public static List<String> buildInitInputLines(Board board, int playerIdx) {
        int half = board.size / 2;
        boolean isP0 = (playerIdx == 0);
        List<String> lines = new ArrayList<>();
        lines.add(board.size + " " + playerIdx);
        List<Board.NutrientSpot> ownHalfSpots = new ArrayList<>();
        for (Board.NutrientSpot s : board.spots) {
            boolean ownHalf = isP0 ? (s.x < half) : (s.x >= half);
            if (ownHalf) {
                ownHalfSpots.add(s);
            }
        }
        lines.add(String.valueOf(ownHalfSpots.size()));
        for (Board.NutrientSpot s : ownHalfSpots) {
            lines.add(s.x + " " + s.y + " " + s.type.code);
        }
        return lines;
    }
}
