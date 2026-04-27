package com.codingame.game;

import java.util.List;
import java.util.Random;

/** Facade delegating to {@link GameConfig}, {@link TurnProtocol}, and contest services. */
public final class GameLogic {

    private GameLogic() {}

    public static final int MAX_ACTIONS_PER_TURN = GameConfig.MAX_ACTIONS_PER_TURN;
    public static final int EXPAND_COST = GameConfig.EXPAND_COST;
    public static final int ATTACK_COST = GameConfig.ATTACK_COST;
    public static final int ATTACK_REWARD = GameConfig.ATTACK_REWARD;
    public static final int VISION_RADIUS = GameConfig.VISION_RADIUS;
    public static final int MAX_TURNS = GameConfig.MAX_TURNS;

    public static List<ActionParser.Action> parseActions(String line) {
        return ActionParser.parseActions(line);
    }

    public static List<TypedAction> parseTypedActions(String line, int playerIndex) {
        return ActionParser.parseTypedActions(line, playerIndex);
    }

    public static int applyEnergyCost(int energy, ActionParser.ActionType type) {
        return EnergyService.applyEnergyCost(energy, type);
    }

    public static boolean canAfford(int energy, ActionParser.ActionType type) {
        return EnergyService.canAfford(energy, type);
    }

    public static boolean isAdjacent(int x1, int y1, int x2, int y2) {
        return GridUtils.isAdjacent(x1, y1, x2, y2);
    }

    public static boolean isInBounds(int x, int y, int size) {
        return GridUtils.isInBounds(x, y, size);
    }

    public static int chebyshevDistance(int x1, int y1, int x2, int y2) {
        return GridUtils.chebyshevDistance(x1, y1, x2, y2);
    }

    public static boolean hasAdjacentOwnCell(Board board, int playerIdx, int x, int y) {
        return GridUtils.hasAdjacentOwnCell(board, playerIdx, x, y);
    }

    public static int countNeighbors(Board board, int playerIdx, int x, int y) {
        return GridUtils.countNeighbors(board, playerIdx, x, y);
    }

    public static void generateMap(Board board, Random rng, int size) {
        MapGenerator.generateMap(board, rng, size);
    }

    public static int computeStartingEnergy(Board board, int startX, int startY) {
        return MapGenerator.computeStartingEnergy(board, startX, startY);
    }

    public static VisibleState getVisibleEntities(Board board, int playerIdx) {
        return FogOfWarService.getVisibleEntities(board, playerIdx);
    }

    public static List<String> buildTurnInputLines(TurnInput input) {
        return TurnProtocol.buildTurnInputLines(input);
    }

    public static List<String> buildInitInputLines(GameStateSnapshot snapshot, int playerIdx) {
        return TurnProtocol.buildInitInputLines(snapshot, playerIdx);
    }

    public static boolean resolveExpand(Board board, int playerIdx, int x, int y) {
        return ActionResolver.resolveExpand(board, playerIdx, x, y);
    }

    public static int resolveAttack(Board board, int attackerIdx, int tx, int ty) {
        return ActionResolver.resolveAttack(board, attackerIdx, tx, ty);
    }

    public static boolean resolveAutophagy(Board board, int playerIdx, int x, int y) {
        return ActionResolver.resolveAutophagy(board, playerIdx, x, y);
    }

    public static void passiveExtraction(Board board) {
        EnergyService.passiveNutrientExtraction(board);
    }

    public static int computeScore(Board board, int playerIdx) {
        return VictoryChecker.computeScore(board, playerIdx);
    }

    public static int checkGameOver(Board board, int turn) {
        return VictoryChecker.checkGameOver(board, turn);
    }
}
