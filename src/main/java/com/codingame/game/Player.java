package com.codingame.game;

import com.codingame.gameengine.core.AbstractMultiplayerPlayer;

public class Player extends AbstractMultiplayerPlayer {
    public int energy;

    @Override
    public int getExpectedOutputLines() {
        return 1;
    }
}
