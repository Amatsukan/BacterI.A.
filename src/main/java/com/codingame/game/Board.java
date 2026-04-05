package com.codingame.game;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class Board {

    public static final int EMPTY = 0;
    public static final int PLAYER0 = 1;
    public static final int PLAYER1 = 2;

    public final int size;
    public final int[][] cells;
    public final List<NutrientSpot> spots = new ArrayList<>();
    @SuppressWarnings("unchecked")
    public final Set<Point>[] playerCells = new Set[]{new HashSet<>(), new HashSet<>()};
    public final int[] energy = new int[2];
    public final int[] cellsDestroyed = new int[2];

    public Board(int size) {
        this.size = size;
        this.cells = new int[size][size];
    }

    public int getOwner(int x, int y) {
        return cells[y][x];
    }

    public void setOwner(int x, int y, int owner) {
        cells[y][x] = owner;
    }

    public void placeCell(int playerIdx, int x, int y) {
        int owner = playerIdx + 1;
        cells[y][x] = owner;
        playerCells[playerIdx].add(new Point(x, y));
    }

    public void removeCell(int playerIdx, int x, int y) {
        cells[y][x] = EMPTY;
        playerCells[playerIdx].remove(new Point(x, y));
    }

    public boolean isEmpty(int x, int y) {
        return cells[y][x] == EMPTY;
    }

    public boolean belongsTo(int playerIdx, int x, int y) {
        return cells[y][x] == playerIdx + 1;
    }

    public NutrientSpot getSpotAt(int x, int y) {
        for (NutrientSpot s : spots) {
            if (s.x == x && s.y == y) return s;
        }
        return null;
    }

    // -----------------------------------------------------------------------
    // Inner types
    // -----------------------------------------------------------------------

    public enum SpotType {
        SMALL(1, 10),
        MEDIUM(2, 30),
        LARGE(3, 70);

        public final int code;
        public final int maxEnergy;

        SpotType(int code, int maxEnergy) {
            this.code = code;
            this.maxEnergy = maxEnergy;
        }

        public static SpotType fromCode(int code) {
            for (SpotType t : values()) {
                if (t.code == code) return t;
            }
            throw new IllegalArgumentException("Unknown spot code: " + code);
        }
    }

    public static class NutrientSpot {
        public final int x, y;
        public final SpotType type;
        public int remainingEnergy;

        public NutrientSpot(int x, int y, SpotType type) {
            this.x = x;
            this.y = y;
            this.type = type;
            this.remainingEnergy = type.maxEnergy;
        }

        public boolean isDepleted() {
            return remainingEnergy <= 0;
        }
    }

    public static class Point {
        public final int x, y;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Point)) return false;
            Point p = (Point) o;
            return x == p.x && y == p.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }

        @Override
        public String toString() {
            return "(" + x + "," + y + ")";
        }
    }
}
