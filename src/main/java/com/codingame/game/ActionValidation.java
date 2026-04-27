package com.codingame.game;

/** Validation result used by typed actions before resolution. */
public final class ActionValidation {
    public final boolean valid;
    public final String reason;

    private ActionValidation(boolean valid, String reason) {
        this.valid = valid;
        this.reason = reason;
    }

    public static ActionValidation ok() {
        return new ActionValidation(true, "OK");
    }

    public static ActionValidation fail(String reason) {
        return new ActionValidation(false, reason);
    }
}
