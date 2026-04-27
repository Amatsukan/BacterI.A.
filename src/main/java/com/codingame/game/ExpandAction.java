package com.codingame.game;

public final class ExpandAction extends AbstractTypedAction {
    public ExpandAction(int playerIndex, int x, int y) {
        super(playerIndex, x, y);
    }

    @Override
    public ActionParser.ActionType type() {
        return ActionParser.ActionType.EXPAND;
    }

    @Override
    public int cost() {
        return GameConfig.EXPAND_COST;
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
        if (!context.canAfford(playerIndex(), cost())) {
            return ActionResult.failure("INSUFFICIENT_ENERGY", 0);
        }
        context.spend(playerIndex(), cost());
        if (!ActionResolver.resolveExpandFromSnapshot(context.board, context.phaseSnapshot, playerIndex(), x(), y())) {
            return ActionResult.failure("INVALID_EXPAND", -cost());
        }
        return ActionResult.success("EXPAND_OK", -cost());
    }

    @Override
    public String asCommand() {
        return "EXPAND " + x() + " " + y();
    }
}
