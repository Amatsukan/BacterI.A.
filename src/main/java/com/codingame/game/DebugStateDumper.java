package com.codingame.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class DebugStateDumper {

    private DebugStateDumper() {}

    public static String dump(Board board, int turn) {
        List<String> p0 = new ArrayList<>();
        for (Board.Point c : board.playerCells[0]) {
            p0.add(c.x + "," + c.y);
        }
        List<String> p1 = new ArrayList<>();
        for (Board.Point c : board.playerCells[1]) {
            p1.add(c.x + "," + c.y);
        }
        Collections.sort(p0);
        Collections.sort(p1);

        List<String> spots = new ArrayList<>();
        for (Board.NutrientSpot s : board.spots) {
            spots.add(s.x + "," + s.y + "," + s.type.code + "," + s.remainingEnergy);
        }
        Collections.sort(spots);

        return "DEBUG_STATE {\"turn\":" + turn +
            ",\"energy\":[" + board.energy[0] + "," + board.energy[1] + "]" +
            ",\"p0Cells\":\"" + String.join(";", p0) + "\"" +
            ",\"p1Cells\":\"" + String.join(";", p1) + "\"" +
            ",\"spots\":\"" + String.join(";", spots) + "\"}";
    }
}
