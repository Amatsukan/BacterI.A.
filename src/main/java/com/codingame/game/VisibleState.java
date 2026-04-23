package com.codingame.game;

import java.util.ArrayList;
import java.util.List;

/** Per-turn fog-of-war snapshot for one player. */
public class VisibleState {
    public final List<Board.Point> myCells = new ArrayList<>();
    public final List<Board.Point> oppCells = new ArrayList<>();
    public final List<Board.NutrientSpot> visibleSpots = new ArrayList<>();
}
