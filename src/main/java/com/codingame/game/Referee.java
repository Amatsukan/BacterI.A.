package com.codingame.game;

import com.codingame.gameengine.core.AbstractPlayer.TimeoutException;
import com.codingame.gameengine.core.AbstractReferee;
import com.codingame.gameengine.core.MultiplayerGameManager;
import com.codingame.gameengine.module.entities.GraphicEntityModule;
import com.google.inject.Inject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Referee extends AbstractReferee {

    @Inject private MultiplayerGameManager<Player> gameManager;
    @Inject private GraphicEntityModule graphicEntityModule;

    private Board board;
    private View view;

    private static List<Player> sortedByIndex(List<Player> players) {
        List<Player> list = new ArrayList<>(players);
        list.sort(Comparator.comparingInt(Player::getIndex));
        return list;
    }

    /**
     * Sends initialization lines in the exact order defined by {@code config/stub.txt}.
     */
    private void sendInitInput() {
        for (Player player : sortedByIndex(gameManager.getPlayers())) {
            int idx = player.getIndex();
            for (String line : TurnProtocol.buildInitInputLines(board, idx)) {
                player.sendInputLine(line);
            }
        }
    }

    @Override
    public void init() {
        int size = 64;
        board = new Board(size);

        GameLogic.generateMap(board, gameManager.getRandom(), size);

        int e0 = GameLogic.computeStartingEnergy(board, 0, 0);
        int e1 = GameLogic.computeStartingEnergy(board, size - 1, size - 1);
        board.energy[0] = e0;
        board.energy[1] = e1;

        gameManager.setMaxTurns(GameConfig.MAX_TURNS);
        gameManager.setTurnMaxTime(50);
        gameManager.setFirstTurnMaxTime(1000);
        gameManager.setFrameDuration(400);

        sendInitInput();

        view = new View(graphicEntityModule, board, gameManager);
        view.init();
    }

    @Override
    public void gameTurn(int turn) {
        // Per-turn lines after the first line match config/stub.txt (see TurnProtocol.buildTurnInputLines).
        for (Player player : sortedByIndex(gameManager.getActivePlayers())) {
            int idx = player.getIndex();
            int oppIdx = 1 - idx;
            VisibleState vs = FogOfWarService.getVisibleEntities(board, idx);
            player.sendInputLine(board.energy[idx] + " " + board.energy[oppIdx]);
            for (String line : TurnProtocol.buildTurnInputLines(vs)) {
                player.sendInputLine(line);
            }
        }
        for (Player player : sortedByIndex(gameManager.getActivePlayers())) {
            player.execute();
        }

        // Resolve in ascending player index: player 0's full action list, then player 1's (deterministic).
        for (Player player : sortedByIndex(gameManager.getActivePlayers())) {
            int idx = player.getIndex();
            try {
                List<String> outputs = player.getOutputs();
                List<ActionParser.Action> actions = ActionParser.parseActions(outputs.get(0));

                for (ActionParser.Action action : actions) {
                    if (!EnergyService.canAfford(board.energy[idx], action.type)) {
                        gameManager.addToGameSummary(
                            player.getNicknameToken() + ": insufficient energy for " + action.type);
                        continue;
                    }

                    switch (action.type) {
                        case EXPAND:
                            board.energy[idx] = EnergyService.applyEnergyCost(board.energy[idx], action.type);
                            if (!ActionResolver.resolveExpand(board, idx, action.x, action.y)) {
                                gameManager.addToGameSummary(
                                    player.getNicknameToken() + ": invalid EXPAND " + action.x + " " + action.y);
                            }
                            break;
                        case ATTACK:
                            board.energy[idx] = EnergyService.applyEnergyCost(board.energy[idx], action.type);
                            int reward = ActionResolver.resolveAttack(board, idx, action.x, action.y);
                            board.energy[idx] += reward;
                            break;
                        case AUTOPHAGY:
                            if (ActionResolver.resolveAutophagy(board, idx, action.x, action.y)) {
                                board.energy[idx] = EnergyService.applyEnergyCost(board.energy[idx], action.type);
                            } else {
                                gameManager.addToGameSummary(
                                    player.getNicknameToken() + ": invalid AUTOPHAGY " + action.x + " " + action.y);
                            }
                            break;
                        case WAIT:
                            break;
                    }
                }
            } catch (TimeoutException e) {
                player.deactivate("Timeout!");
                player.setScore(-1);
                gameManager.addToGameSummary(player.getNicknameToken() + " timed out.");
                gameManager.endGame();
                return;
            } catch (Exception e) {
                player.deactivate("Error: " + e.getMessage());
                player.setScore(-1);
                gameManager.addToGameSummary(player.getNicknameToken() + " error: " + e.getMessage());
                gameManager.endGame();
                return;
            }
        }

        EnergyService.passiveNutrientExtraction(board);
        view.update(board, turn);

        int result = VictoryChecker.checkGameOver(board, turn);
        if (result != -2) {
            gameManager.endGame();
        }
    }

    @Override
    public void onEnd() {
        int s0 = VictoryChecker.computeScore(board, 0);
        int s1 = VictoryChecker.computeScore(board, 1);
        gameManager.getPlayer(0).setScore(s0);
        gameManager.getPlayer(1).setScore(s1);
        gameManager.addToGameSummary("Final scores — P0: " + s0 + "  P1: " + s1);
    }
}
