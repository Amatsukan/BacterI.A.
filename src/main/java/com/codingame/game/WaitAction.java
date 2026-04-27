package com.codingame.game;

public final class WaitAction extends AbstractTypedAction {
    public WaitAction(int playerIndex) {
        super(playerIndex, -1, -1);
    }

    @Override
    public ActionParser.ActionType type() {
        return ActionParser.ActionType.WAIT;
    }

    @Override
    public int cost() {
        return 0;
    }

    @Override
    public ActionValidation validate(GameStateSnapshot snapshot) {
        return ActionValidation.ok();
    }

    @Override
    public ActionResult execute(TurnResolutionContext context) {
        return ActionResult.success("WAIT", 0);
    }

    @Override
    public String asCommand() {
        return "WAIT";
    }
}
