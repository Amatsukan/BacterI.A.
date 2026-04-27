package com.codingame.game;

/** Structured action resolution result for logs and audits. */
public final class ActionResult {
    public final boolean success;
    public final String status;
    public final int energyDelta;

    public ActionResult(boolean success, String status, int energyDelta) {
        this.success = success;
        this.status = status;
        this.energyDelta = energyDelta;
    }

    public static ActionResult success(String status, int energyDelta) {
        return new ActionResult(true, status, energyDelta);
    }

    public static ActionResult failure(String status, int energyDelta) {
        return new ActionResult(false, status, energyDelta);
    }
}
