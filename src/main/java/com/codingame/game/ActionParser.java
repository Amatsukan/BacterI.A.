package com.codingame.game;

import java.util.ArrayList;
import java.util.List;

public final class ActionParser {

    private ActionParser() {}

    public enum ActionType {
        EXPAND,
        ATTACK,
        AUTOPHAGY,
        WAIT
    }

    public static final class Action {
        public final ActionType type;
        public final int x, y;

        public Action(ActionType type, int x, int y) {
            this.type = type;
            this.x = x;
            this.y = y;
        }

        public Action(ActionType type) {
            this(type, -1, -1);
        }

        @Override
        public String toString() {
            return type == ActionType.WAIT ? "WAIT" : type + " " + x + " " + y;
        }
    }

    public static List<Action> parseActions(String line) {
        if (line == null || line.isBlank()) {
            throw new IllegalArgumentException("Empty output line.");
        }
        String[] parts = line.split(";");
        if (parts.length > GameConfig.MAX_ACTIONS_PER_TURN) {
            throw new IllegalArgumentException(
                "At most " + GameConfig.MAX_ACTIONS_PER_TURN + " actions per turn (" + parts.length + " given).");
        }
        List<Action> actions = new ArrayList<>();
        for (String raw : parts) {
            actions.add(parseSingleAction(raw.trim()));
        }
        return actions;
    }

    public static List<TypedAction> parseTypedActions(String line, int playerIndex) {
        List<Action> parsed = parseActions(line);
        List<TypedAction> actions = new ArrayList<>(parsed.size());
        for (Action a : parsed) {
            switch (a.type) {
                case EXPAND:
                    actions.add(new ExpandAction(playerIndex, a.x, a.y));
                    break;
                case ATTACK:
                    actions.add(new AttackAction(playerIndex, a.x, a.y));
                    break;
                case AUTOPHAGY:
                    actions.add(new AutophagyAction(playerIndex, a.x, a.y));
                    break;
                case WAIT:
                    actions.add(new WaitAction(playerIndex));
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported action type: " + a.type);
            }
        }
        return actions;
    }

    private static Action parseSingleAction(String token) {
        String[] p = token.split("\\s+");
        ActionType type;
        switch (p[0].toUpperCase()) {
            case "EXPAND":
                type = ActionType.EXPAND;
                break;
            case "ATTACK":
                type = ActionType.ATTACK;
                break;
            case "AUTOPHAGY":
                type = ActionType.AUTOPHAGY;
                break;
            case "WAIT":
                type = ActionType.WAIT;
                break;
            default:
                throw new IllegalArgumentException("Unknown action: " + p[0]);
        }
        if (type == ActionType.WAIT) {
            return new Action(type);
        }
        if (p.length < 3) {
            throw new IllegalArgumentException("Action " + type + " requires x y coordinates.");
        }
        return new Action(type, Integer.parseInt(p[1]), Integer.parseInt(p[2]));
    }
}
