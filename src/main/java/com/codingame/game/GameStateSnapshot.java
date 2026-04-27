package com.codingame.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Immutable full-state snapshot used by validators, actions, and deterministic replays. */
public final class GameStateSnapshot {
    public final int turn;
    public final int boardSize;
    public final int[] energyByPlayer;
    public final List<Coord>[] cellsByPlayer;
    public final List<SpotInfo> spots;
    private final int[][] ownerGrid;

    @SuppressWarnings("unchecked")
    private GameStateSnapshot(
        int turn,
        int boardSize,
        int[] energyByPlayer,
        List<Coord>[] cellsByPlayer,
        List<SpotInfo> spots,
        int[][] ownerGrid
    ) {
        this.turn = turn;
        this.boardSize = boardSize;
        this.energyByPlayer = new int[] {energyByPlayer[0], energyByPlayer[1]};
        this.cellsByPlayer = new List[] {
            Collections.unmodifiableList(new ArrayList<>(cellsByPlayer[0])),
            Collections.unmodifiableList(new ArrayList<>(cellsByPlayer[1]))
        };
        this.spots = Collections.unmodifiableList(new ArrayList<>(spots));
        this.ownerGrid = ownerGrid;
    }

    public static GameStateSnapshot fromBoard(Board board, int turn) {
        List<Coord> p0 = new ArrayList<>();
        for (Board.Point c : board.playerCells[0]) {
            p0.add(new Coord(c.x, c.y));
        }
        List<Coord> p1 = new ArrayList<>();
        for (Board.Point c : board.playerCells[1]) {
            p1.add(new Coord(c.x, c.y));
        }
        Collections.sort(p0);
        Collections.sort(p1);

        List<SpotInfo> spots = new ArrayList<>();
        for (Board.NutrientSpot s : board.spots) {
            spots.add(new SpotInfo(s.x, s.y, s.type.code, s.remainingEnergy));
        }
        spots.sort((a, b) -> {
            int byY = Integer.compare(a.y, b.y);
            if (byY != 0) return byY;
            return Integer.compare(a.x, b.x);
        });

        List<Coord>[] cells = new List[] {p0, p1};
        int[][] ownerGrid = new int[board.size][board.size];
        for (Coord c : p0) {
            ownerGrid[c.y][c.x] = Board.PLAYER0;
        }
        for (Coord c : p1) {
            ownerGrid[c.y][c.x] = Board.PLAYER1;
        }
        return new GameStateSnapshot(turn, board.size, board.energy, cells, spots, ownerGrid);
    }

    public boolean inBounds(int x, int y) {
        return x >= 0 && x < boardSize && y >= 0 && y < boardSize;
    }

    public int ownerAt(int x, int y) {
        return ownerGrid[y][x];
    }

    public boolean belongsTo(int playerIdx, int x, int y) {
        return ownerGrid[y][x] == playerIdx + 1;
    }

    public boolean hasAdjacentOwnCell(int playerIdx, int x, int y) {
        int owner = playerIdx + 1;
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                if (dx == 0 && dy == 0) continue;
                int nx = x + dx;
                int ny = y + dy;
                if (!inBounds(nx, ny)) continue;
                if (ownerGrid[ny][nx] == owner) {
                    return true;
                }
            }
        }
        return false;
    }

    public int countNeighbors(int playerIdx, int x, int y) {
        int owner = playerIdx + 1;
        int count = 0;
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                if (dx == 0 && dy == 0) continue;
                int nx = x + dx;
                int ny = y + dy;
                if (!inBounds(nx, ny)) continue;
                if (ownerGrid[ny][nx] == owner) {
                    count++;
                }
            }
        }
        return count;
    }
}
