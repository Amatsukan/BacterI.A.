package com.codingame.game;

@FunctionalInterface
public interface TurnLogSink {
    void log(String event);
}
