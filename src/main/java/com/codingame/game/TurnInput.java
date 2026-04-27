package com.codingame.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Immutable per-turn protocol DTO consumed by {@link TurnProtocol}. */
public final class TurnInput {
    public final int myEnergy;
    public final int oppEnergy;
    public final List<Coord> myCells;
    public final List<Coord> oppCells;
    public final List<SpotInfo> visibleSpots;

    public TurnInput(
        int myEnergy,
        int oppEnergy,
        List<Coord> myCells,
        List<Coord> oppCells,
        List<SpotInfo> visibleSpots
    ) {
        this.myEnergy = myEnergy;
        this.oppEnergy = oppEnergy;
        this.myCells = Collections.unmodifiableList(new ArrayList<>(myCells));
        this.oppCells = Collections.unmodifiableList(new ArrayList<>(oppCells));
        this.visibleSpots = Collections.unmodifiableList(new ArrayList<>(visibleSpots));
    }
}
