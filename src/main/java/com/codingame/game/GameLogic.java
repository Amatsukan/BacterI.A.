package com.codingame.game;

import java.util.ArrayList;
import java.util.List;

/**
 * Lógica pura do jogo BacterI.A. — sem dependências do CodinGame SDK.
 * Pode ser testada com JUnit directamente.
 */
public class GameLogic {

    public static final int MAX_ACTIONS_PER_TURN = 5;
    public static final int EXPAND_COST  = 2;
    public static final int ATTACK_COST  = 2;
    public static final int ATTACK_REWARD = 3;

    // -----------------------------------------------------------------------
    // Tipos de dados imutáveis
    // -----------------------------------------------------------------------

    public enum ActionType { EXPAND, ATTACK, AUTOPHAGY, WAIT }

    public static class Action {
        public final ActionType type;
        public final int x;
        public final int y;

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
            return type == ActionType.WAIT
                ? "WAIT"
                : type + " " + x + " " + y;
        }
    }

    // -----------------------------------------------------------------------
    // Parsing
    // -----------------------------------------------------------------------

    /**
     * Faz parse de uma linha de output do bot.
     * Formato: "CMD [x y][; CMD [x y]]..."
     *
     * @throws IllegalArgumentException se a linha for inválida
     */
    public static List<Action> parseActions(String line) {
        if (line == null || line.isBlank()) {
            throw new IllegalArgumentException("Output do bot está vazio.");
        }

        String[] parts = line.split(";");
        if (parts.length > MAX_ACTIONS_PER_TURN) {
            throw new IllegalArgumentException(
                "Limite de " + MAX_ACTIONS_PER_TURN + " acções excedido (" + parts.length + ")."
            );
        }

        List<Action> actions = new ArrayList<>();
        for (String raw : parts) {
            actions.add(parseSingleAction(raw.trim()));
        }
        return actions;
    }

    private static Action parseSingleAction(String token) {
        String[] p = token.split("\\s+");
        ActionType type;
        switch (p[0].toUpperCase()) {
            case "EXPAND":    type = ActionType.EXPAND;    break;
            case "ATTACK":    type = ActionType.ATTACK;    break;
            case "AUTOPHAGY": type = ActionType.AUTOPHAGY; break;
            case "WAIT":      type = ActionType.WAIT;      break;
            default: throw new IllegalArgumentException("Comando desconhecido: " + p[0]);
        }

        if (type == ActionType.WAIT) {
            return new Action(type);
        }
        if (p.length < 3) {
            throw new IllegalArgumentException(
                "Accao " + type + " requer coordenadas (x y)."
            );
        }
        int x = Integer.parseInt(p[1]);
        int y = Integer.parseInt(p[2]);
        return new Action(type, x, y);
    }

    // -----------------------------------------------------------------------
    // Energia
    // -----------------------------------------------------------------------

    /** Aplica o custo de uma acção e devolve a nova energia. */
    public static int applyEnergyCost(int energy, ActionType type) {
        switch (type) {
            case EXPAND:
            case ATTACK:    return energy - EXPAND_COST;
            case AUTOPHAGY: return energy + 1;
            default:        return energy;
        }
    }

    /** Verifica se o jogador tem energia suficiente para a acção. */
    public static boolean canAfford(int energy, ActionType type) {
        switch (type) {
            case EXPAND:
            case ATTACK: return energy >= EXPAND_COST;
            default:     return true;
        }
    }

    // -----------------------------------------------------------------------
    // Adjacência
    // -----------------------------------------------------------------------

    /** Devolve true se (x2,y2) é ortogonal ou diagonalmente adjacente a (x1,y1). */
    public static boolean isAdjacent(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) <= 1
            && Math.abs(y1 - y2) <= 1
            && !(x1 == x2 && y1 == y2);
    }

    /** Devolve true se (x,y) está dentro de uma grelha [0, size[. */
    public static boolean isInBounds(int x, int y, int size) {
        return x >= 0 && x < size && y >= 0 && y < size;
    }
}
