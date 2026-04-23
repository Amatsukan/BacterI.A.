package com.codingame.game;

public final class EnergyService {

    private EnergyService() {}

    public static int applyEnergyCost(int energy, ActionParser.ActionType type) {
        switch (type) {
            case EXPAND:
                return energy - GameConfig.EXPAND_COST;
            case ATTACK:
                return energy - GameConfig.ATTACK_COST;
            case AUTOPHAGY:
                return energy + 1;
            default:
                return energy;
        }
    }

    public static boolean canAfford(int energy, ActionParser.ActionType type) {
        switch (type) {
            case EXPAND:
                return energy >= GameConfig.EXPAND_COST;
            case ATTACK:
                return energy >= GameConfig.ATTACK_COST;
            default:
                return true;
        }
    }

    public static void passiveNutrientExtraction(Board board) {
        for (Board.NutrientSpot spot : board.spots) {
            if (spot.isDepleted()) continue;
            int owner = board.getOwner(spot.x, spot.y);
            if (owner == Board.EMPTY) continue;
            int playerIdx = owner - 1;
            spot.remainingEnergy--;
            board.energy[playerIdx]++;
        }
    }
}
