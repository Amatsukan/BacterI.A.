package com.codingame.game;

import com.codingame.gameengine.core.AbstractPlayer.TimeoutException;
import com.codingame.gameengine.core.AbstractReferee;
import com.codingame.gameengine.core.MultiplayerGameManager;
import com.codingame.gameengine.module.entities.GraphicEntityModule;
import com.google.inject.Inject;

import java.util.List;

public class Referee extends AbstractReferee {

    @Inject private MultiplayerGameManager<Player> gameManager;
    @Inject private GraphicEntityModule graphicEntityModule;

    private Board board;
    private View view;

    @Override
    public void init() {
        int size = 64;
        board = new Board(size);
        GameLogic.generateMap(board, gameManager.getRandom(), size);

        int e0 = GameLogic.computeStartingEnergy(board, 0, 0);
        int e1 = GameLogic.computeStartingEnergy(board, size - 1, size - 1);
        board.energy[0] = e0;
        board.energy[1] = e1;

        gameManager.setMaxTurns(GameLogic.MAX_TURNS);
        gameManager.setTurnMaxTime(50);
        gameManager.setFirstTurnMaxTime(1000);
        gameManager.setFrameDuration(400);

        sendInitInput();

        view = new View(graphicEntityModule, board, gameManager);
        view.init();
    }

    private void sendInitInput() {
        for (Player player : gameManager.getPlayers()) {
            int idx = player.getIndex();
            int half = board.size / 2;
            boolean isP0 = (idx == 0);

            player.sendInputLine(board.size + " " + idx);

            List<Board.NutrientSpot> ownHalfSpots = new java.util.ArrayList<>();
            for (Board.NutrientSpot s : board.spots) {
                boolean ownHalf = isP0 ? (s.x < half) : (s.x >= half);
                if (ownHalf) ownHalfSpots.add(s);
            }

            player.sendInputLine(String.valueOf(ownHalfSpots.size()));
            for (Board.NutrientSpot s : ownHalfSpots) {
                player.sendInputLine(s.x + " " + s.y + " " + s.type.code);
            }
        }
    }

    @Override
    public void gameTurn(int turn) {
        // Send turn input
        for (Player player : gameManager.getActivePlayers()) {
            int idx = player.getIndex();
            int oppIdx = 1 - idx;
            GameLogic.VisibleState vs = GameLogic.getVisibleEntities(board, idx);

            player.sendInputLine(board.energy[idx] + " " + board.energy[oppIdx]);

            int entityCount = vs.myCells.size() + vs.oppCells.size() + vs.visibleSpots.size();
            player.sendInputLine(String.valueOf(entityCount));

            for (Board.Point c : vs.myCells) {
                player.sendInputLine("MYCELL " + c.x + " " + c.y);
            }
            for (Board.Point c : vs.oppCells) {
                player.sendInputLine("OPPCELL " + c.x + " " + c.y);
            }
            for (Board.NutrientSpot s : vs.visibleSpots) {
                player.sendInputLine("SPOT " + s.x + " " + s.y + " " + s.type.code + " " + s.remainingEnergy);
            }

            player.execute();
        }

        // Collect and resolve actions
        for (Player player : gameManager.getActivePlayers()) {
            int idx = player.getIndex();
            try {
                List<String> outputs = player.getOutputs();
                List<GameLogic.Action> actions = GameLogic.parseActions(outputs.get(0));

                for (GameLogic.Action action : actions) {
                    if (!GameLogic.canAfford(board.energy[idx], action.type)) {
                        gameManager.addToGameSummary(
                            player.getNicknameToken() + ": insufficient energy for " + action.type);
                        continue;
                    }

                    switch (action.type) {
                        case EXPAND:
                            board.energy[idx] = GameLogic.applyEnergyCost(board.energy[idx], action.type);
                            if (!GameLogic.resolveExpand(board, idx, action.x, action.y)) {
                                gameManager.addToGameSummary(
                                    player.getNicknameToken() + ": invalid EXPAND " + action.x + " " + action.y);
                            }
                            break;
                        case ATTACK:
                            board.energy[idx] = GameLogic.applyEnergyCost(board.energy[idx], action.type);
                            int reward = GameLogic.resolveAttack(board, idx, action.x, action.y);
                            board.energy[idx] += reward;
                            break;
                        case AUTOPHAGY:
                            if (GameLogic.resolveAutophagy(board, idx, action.x, action.y)) {
                                board.energy[idx] = GameLogic.applyEnergyCost(board.energy[idx], action.type);
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

        GameLogic.passiveExtraction(board);
        view.update(board, turn);

        int result = GameLogic.checkGameOver(board, turn);
        if (result != -2) {
            gameManager.endGame();
        }
    }

    @Override
    public void onEnd() {
        int s0 = GameLogic.computeScore(board, 0);
        int s1 = GameLogic.computeScore(board, 1);
        gameManager.getPlayer(0).setScore(s0);
        gameManager.getPlayer(1).setScore(s1);
        gameManager.addToGameSummary("Final scores — P0: " + s0 + "  P1: " + s1);
    }
}
