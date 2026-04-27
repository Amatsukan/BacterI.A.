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
        GameStateSnapshot snapshot = GameStateSnapshot.fromBoard(board, 0);
        for (Player player : sortedByIndex(gameManager.getPlayers())) {
            int idx = player.getIndex();
            for (String line : TurnProtocol.buildInitInputLines(snapshot, idx)) {
                player.sendInputLine(line);
            }
        }
    }

    @Override
    public void init() {
        int size = GameConfig.BOARD_SIZE;
        board = new Board(size);

        GameLogic.generateMap(board, gameManager.getRandom(), size);

        int e0 = GameLogic.computeStartingEnergy(board, 0, 0);
        int e1 = GameLogic.computeStartingEnergy(board, size - 1, size - 1);
        board.energy[0] = e0;
        board.energy[1] = e1;
        BoardInvariantChecker.assertValid(board, "init");

        gameManager.setMaxTurns(GameConfig.MAX_TURNS);
        gameManager.setTurnMaxTime(GameConfig.SDK_TURN_MAX_MS);
        gameManager.setFirstTurnMaxTime(GameConfig.FIRST_TURN_MAX_MS);
        gameManager.setFrameDuration(400);

        sendInitInput();

        view = new View(graphicEntityModule, board, gameManager);
        view.init();
    }

    @Override
    public void gameTurn(int turn) {
        // 1) Input generation
        GameStateSnapshot preTurnSnapshot = GameStateSnapshot.fromBoard(board, turn);
        for (Player player : sortedByIndex(gameManager.getActivePlayers())) {
            int idx = player.getIndex();
            PlayerView playerView = FogOfWarService.buildPlayerView(preTurnSnapshot, idx);
            TurnInput input = playerView.toTurnInput();
            player.sendInputLine(input.myEnergy + " " + input.oppEnergy);
            for (String line : TurnProtocol.buildTurnInputLines(input)) {
                player.sendInputLine(line);
            }
        }

        // 2) Players execute
        for (Player player : sortedByIndex(gameManager.getActivePlayers())) {
            player.execute();
        }

        // 3) Collect raw outputs
        List<TurnProcessor.PlayerSubmission> submissions = new ArrayList<>();
        for (Player player : sortedByIndex(gameManager.getActivePlayers())) {
            int idx = player.getIndex();
            try {
                List<String> outputs = player.getOutputs();
                if (outputs.isEmpty()) {
                    gameManager.addToGameSummary(player.getNicknameToken() + ": no output line (expected 1).");
                    player.deactivate("No output");
                    player.setScore(-1);
                    gameManager.endGame();
                    return;
                }
                submissions.add(new TurnProcessor.PlayerSubmission(
                    idx, player.getNicknameToken(), outputs.get(0)));
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

        // 4-6) Parse, validate, resolve, post-effects, victory
        TurnProcessor.TurnOutcome outcome;
        try {
            outcome = TurnProcessor.processTurn(board, turn, submissions, gameManager::addToGameSummary);
        } catch (Exception e) {
            gameManager.addToGameSummary("Referee fail-fast: " + e.getMessage());
            gameManager.endGame();
            return;
        }

        view.update(board, turn);

        if (outcome.victoryResult != -2) {
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
