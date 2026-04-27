package com.codingame.game;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Ensures turn input matches {@code config/stub.txt} / {@code WaitBot} parsing
 * so local runs and CodinGame do not deadlock (referee waiting on {@code execute()},
 * player blocked on {@code nextInt()}).
 */
class TurnInputProtocolTest {

    @Test
    void buildTurnInputLines_canBeReadLikeWaitBot() {
        Board b = new Board(GameConfig.BOARD_SIZE);
        b.placeCell(0, 1, 2);
        b.placeCell(1, 10, 11);
        b.spots.add(new Board.NutrientSpot(5, 5, Board.SpotType.SMALL));

        b.energy[0] = 7;
        b.energy[1] = 9;

        GameStateSnapshot snapshot = GameStateSnapshot.fromBoard(b, 1);
        PlayerView view = FogOfWarService.buildPlayerView(snapshot, 0);
        List<String> body = GameLogic.buildTurnInputLines(view.toTurnInput());

        List<String> all = new ArrayList<>();
        all.add(b.energy[0] + " " + b.energy[1]);
        all.addAll(body);

        String joined = String.join("\n", all) + "\n";
        try (Scanner in = new Scanner(joined)) {
            int myEnergy = in.nextInt();
            int oppEnergy = in.nextInt();
            assertEquals(7, myEnergy);
            assertEquals(9, oppEnergy);

            int myCellCount = in.nextInt();
            for (int i = 0; i < myCellCount; i++) {
                in.nextInt();
                in.nextInt();
            }
            int oppCellCount = in.nextInt();
            for (int i = 0; i < oppCellCount; i++) {
                in.nextInt();
                in.nextInt();
            }
            int visSpotCount = in.nextInt();
            for (int i = 0; i < visSpotCount; i++) {
                in.nextInt();
                in.nextInt();
                in.nextInt();
                in.nextInt();
            }

            assertFalse(in.hasNextInt());
        }
    }

    @Test
    void initInputLines_matchStubAndWaitBotInit() {
        Board b = new Board(64);
        b.spots.add(new Board.NutrientSpot(10, 20, Board.SpotType.SMALL));
        b.spots.add(new Board.NutrientSpot(53, 43, Board.SpotType.MEDIUM));
        GameStateSnapshot snapshot = GameStateSnapshot.fromBoard(b, 0);

        for (int myIndex : new int[] {0, 1}) {
            List<String> lines = GameLogic.buildInitInputLines(snapshot, myIndex);
            String joined = String.join("\n", lines) + "\n";
            try (Scanner in = new Scanner(joined)) {
                int mapSize = in.nextInt();
                int idx = in.nextInt();
                assertEquals(64, mapSize);
                assertEquals(myIndex, idx);

                int spotCount = in.nextInt();
                assertEquals(1, spotCount);
                int sx = in.nextInt();
                int sy = in.nextInt();
                int st = in.nextInt();
                if (myIndex == 0) {
                    assertEquals(10, sx);
                    assertEquals(20, sy);
                    assertEquals(1, st);
                } else {
                    assertEquals(53, sx);
                    assertEquals(43, sy);
                    assertEquals(2, st);
                }
                assertFalse(in.hasNextInt());
            }
        }
    }

    @Test
    void buildTurnInputLines_emptyCellsAndSpots_canBeReadLikeWaitBot() {
        Board b = new Board(GameConfig.BOARD_SIZE);
        b.placeCell(0, 0, 0);
        b.energy[0] = 1;
        b.energy[1] = 2;

        GameStateSnapshot snapshot = GameStateSnapshot.fromBoard(b, 1);
        PlayerView view = FogOfWarService.buildPlayerView(snapshot, 0);
        List<String> body = GameLogic.buildTurnInputLines(view.toTurnInput());

        List<String> all = new ArrayList<>();
        all.add(b.energy[0] + " " + b.energy[1]);
        all.addAll(body);

        String joined = String.join("\n", all) + "\n";
        try (Scanner in = new Scanner(joined)) {
            assertEquals(1, in.nextInt());
            assertEquals(2, in.nextInt());
            assertEquals(1, in.nextInt());
            in.nextInt();
            in.nextInt();
            assertEquals(0, in.nextInt());
            assertEquals(0, in.nextInt());
            assertFalse(in.hasNextInt());
        }
    }
}
