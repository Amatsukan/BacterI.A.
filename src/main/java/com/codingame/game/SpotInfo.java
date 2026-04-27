package com.codingame.game;

/** Immutable nutrient snapshot DTO used by protocol and debug dumps. */
public final class SpotInfo {
    public final int x;
    public final int y;
    public final int typeCode;
    public final int remainingEnergy;

    public SpotInfo(int x, int y, int typeCode, int remainingEnergy) {
        this.x = x;
        this.y = y;
        this.typeCode = typeCode;
        this.remainingEnergy = remainingEnergy;
    }
}
