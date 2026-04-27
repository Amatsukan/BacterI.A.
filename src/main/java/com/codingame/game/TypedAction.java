package com.codingame.game;

public interface TypedAction {
    ActionParser.ActionType type();
    int playerIndex();
    int x();
    int y();
    int cost();
    ActionValidation validate(GameStateSnapshot snapshot);
    ActionResult execute(TurnResolutionContext context);
    String asCommand();
}
