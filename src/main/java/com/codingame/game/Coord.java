package com.codingame.game;

import java.util.Objects;

/** Immutable coordinate DTO used by turn contracts. */
public final class Coord implements Comparable<Coord> {
    public final int x;
    public final int y;

    public Coord(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Coord)) return false;
        Coord coord = (Coord) o;
        return x == coord.x && y == coord.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public int compareTo(Coord other) {
        int byY = Integer.compare(this.y, other.y);
        if (byY != 0) return byY;
        return Integer.compare(this.x, other.x);
    }

    @Override
    public String toString() {
        return x + "," + y;
    }
}
