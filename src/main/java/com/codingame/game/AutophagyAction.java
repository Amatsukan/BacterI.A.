package com.codingame.game;

public final class AutophagyAction extends AbstractTypedAction {
    public AutophagyAction(int playerIndex, int x, int y) {
        super(playerIndex, x, y);
    }

    @Override
    public ActionParser.ActionType type() {
        return ActionParser.ActionType.AUTOPHAGY;
    }

    @Override
    public int cost() {
        return 0;
    }

    @Override
    public ActionValidation validate(GameStateSnapshot snapshot) {
        if (!snapshot.inBounds(x(), y())) {
            return ActionValidation.fail("OUT_OF_BOUNDS");
        }
        return ActionValidation.ok();
    }

    @Override
    public ActionResult execute(TurnResolutionContext context) {
        if (!ActionResolver.resolveAutophagy(context.board, playerIndex(), x(), y())) {
            return ActionResult.failure("INVALID_AUTOPHAGY", 0);
        }
        context.gain(playerIndex(), 1);
        return ActionResult.success("AUTOPHAGY_OK", 1);
    }

    @Override
    public String asCommand() {
        return "AUTOPHAGY " + x() + " " + y();
    }
}
