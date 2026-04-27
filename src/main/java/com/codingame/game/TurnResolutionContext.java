package com.codingame.game;

/** Mutable context used while resolving one phase of a turn. */
public final class TurnResolutionContext {
    public final Board board;
    public final int turn;
    public final GameStateSnapshot phaseSnapshot;

    public TurnResolutionContext(Board board, int turn, GameStateSnapshot phaseSnapshot) {
        this.board = board;
        this.turn = turn;
        this.phaseSnapshot = phaseSnapshot;
    }

    public boolean canAfford(int playerIdx, int cost) {
        return board.energy[playerIdx] >= cost;
    }

    public void spend(int playerIdx, int amount) {
        board.energy[playerIdx] -= amount;
    }

    public void gain(int playerIdx, int amount) {
        board.energy[playerIdx] += amount;
    }
}
