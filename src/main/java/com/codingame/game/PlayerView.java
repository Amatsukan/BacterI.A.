package com.codingame.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Immutable player-facing view generated from {@link GameStateSnapshot}. */
public final class PlayerView {
    public final int playerIndex;
    public final List<Coord> myCells;
    public final List<Coord> oppCells;
    public final List<SpotInfo> visibleSpots;
    public final int myEnergy;
    public final int oppEnergy;

    public PlayerView(
        int playerIndex,
        List<Coord> myCells,
        List<Coord> oppCells,
        List<SpotInfo> visibleSpots,
        int myEnergy,
        int oppEnergy
    ) {
        this.playerIndex = playerIndex;
        this.myCells = Collections.unmodifiableList(new ArrayList<>(myCells));
        this.oppCells = Collections.unmodifiableList(new ArrayList<>(oppCells));
        this.visibleSpots = Collections.unmodifiableList(new ArrayList<>(visibleSpots));
        this.myEnergy = myEnergy;
        this.oppEnergy = oppEnergy;
    }

    public TurnInput toTurnInput() {
        return new TurnInput(myEnergy, oppEnergy, myCells, oppCells, visibleSpots);
    }
}
