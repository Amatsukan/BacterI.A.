package com.codingame.game;

public final class AttackAction extends AbstractTypedAction {
    public AttackAction(int playerIndex, int x, int y) {
        super(playerIndex, x, y);
    }

    @Override
    public ActionParser.ActionType type() {
        return ActionParser.ActionType.ATTACK;
    }

    @Override
    public int cost() {
        return GameConfig.ATTACK_COST;
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
        int reward = ActionResolver.resolveAttackFromSnapshot(
            context.board, context.phaseSnapshot, playerIndex(), x(), y());
        if (reward > 0) {
            context.gain(playerIndex(), reward);
            return ActionResult.success("ATTACK_OK", -cost() + reward);
        }
        return ActionResult.failure("ATTACK_FAILED", -cost());
    }

    @Override
    public String asCommand() {
        return "ATTACK " + x() + " " + y();
    }
}
