package com.codingame.game;

abstract class AbstractTypedAction implements TypedAction {
    private final int playerIndex;
    private final int x;
    private final int y;

    protected AbstractTypedAction(int playerIndex, int x, int y) {
        this.playerIndex = playerIndex;
        this.x = x;
        this.y = y;
    }

    @Override
    public final int playerIndex() {
        return playerIndex;
    }

    @Override
    public final int x() {
        return x;
    }

    @Override
    public final int y() {
        return y;
    }
}
