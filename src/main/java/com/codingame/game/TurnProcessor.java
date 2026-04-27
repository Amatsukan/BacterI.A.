package com.codingame.game;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Deterministic turn state machine with explicit phases. */
public final class TurnProcessor {

    private TurnProcessor() {}

    public enum Stage {
        INPUT_GENERATION,
        ACTION_PARSING,
        VALIDATION,
        RESOLUTION,
        POST_EFFECTS,
        VICTORY_CHECK
    }

    public static final class PlayerSubmission {
        public final int playerIndex;
        public final String playerName;
        public final String outputLine;

        public PlayerSubmission(int playerIndex, String playerName, String outputLine) {
            this.playerIndex = playerIndex;
            this.playerName = playerName;
            this.outputLine = outputLine;
        }
    }

    public static final class TurnOutcome {
        public final int victoryResult;

        public TurnOutcome(int victoryResult) {
            this.victoryResult = victoryResult;
        }
    }

    private static final class Candidate {
        final TypedAction action;
        final String playerName;
        final int order;
        ActionValidation validation;
        boolean affordable;

        Candidate(TypedAction action, String playerName, int order) {
            this.action = action;
            this.playerName = playerName;
            this.order = order;
        }
    }

    public static TurnOutcome processTurn(
        Board board,
        int turn,
        List<PlayerSubmission> submissions,
        TurnLogSink logger
    ) {
        BoardInvariantChecker.assertValid(board, "turn_" + turn + "_start");
        logStage(logger, turn, Stage.INPUT_GENERATION);

        logStage(logger, turn, Stage.ACTION_PARSING);
        List<Candidate> parsed = parse(submissions);

        logStage(logger, turn, Stage.VALIDATION);
        GameStateSnapshot validationSnapshot = GameStateSnapshot.fromBoard(board, turn);
        for (Candidate c : parsed) {
            c.validation = c.action.validate(validationSnapshot);
            if (!c.validation.valid) {
                logAction(logger, turn, "VALIDATION", c, false, c.validation.reason, 0, board.energy[c.action.playerIndex()]);
            }
        }

        logStage(logger, turn, Stage.RESOLUTION);
        resolvePhase(board, turn, parsed, ActionParser.ActionType.EXPAND, logger);
        resolvePhase(board, turn, parsed, ActionParser.ActionType.ATTACK, logger);
        resolvePhase(board, turn, parsed, ActionParser.ActionType.AUTOPHAGY, logger);
        resolvePhase(board, turn, parsed, ActionParser.ActionType.WAIT, logger);
        BoardInvariantChecker.assertValid(board, "turn_" + turn + "_post_resolution");

        logStage(logger, turn, Stage.POST_EFFECTS);
        EnergyService.passiveNutrientExtraction(board);
        BoardInvariantChecker.assertValid(board, "turn_" + turn + "_post_effects");

        if (GameConfig.DEBUG) {
            logger.log(DebugStateDumper.dump(board, turn));
        }

        logStage(logger, turn, Stage.VICTORY_CHECK);
        int result = VictoryChecker.checkGameOver(board, turn);
        return new TurnOutcome(result);
    }

    private static List<Candidate> parse(List<PlayerSubmission> submissions) {
        List<Candidate> out = new ArrayList<>();
        for (PlayerSubmission s : submissions) {
            List<TypedAction> actions = ActionParser.parseTypedActions(s.outputLine, s.playerIndex);
            int order = 0;
            for (TypedAction action : actions) {
                out.add(new Candidate(action, s.playerName, order++));
            }
        }
        return out;
    }

    private static void resolvePhase(
        Board board,
        int turn,
        List<Candidate> parsed,
        ActionParser.ActionType type,
        TurnLogSink logger
    ) {
        List<Candidate> phase = new ArrayList<>();
        for (Candidate c : parsed) {
            if (c.action.type() == type) {
                phase.add(c);
            }
        }
        phase.sort(Comparator
            .comparingInt((Candidate c) -> c.action.playerIndex())
            .thenComparingInt(c -> c.order));

        markAffordable(board, phase);
        switch (type) {
            case EXPAND:
                resolveExpand(board, turn, phase, logger);
                break;
            case ATTACK:
                resolveAttack(board, turn, phase, logger);
                break;
            default:
                resolveSimple(board, turn, phase, logger);
                break;
        }
    }

    private static void markAffordable(Board board, List<Candidate> phase) {
        int[] virtualEnergy = new int[] {board.energy[0], board.energy[1]};
        for (Candidate c : phase) {
            if (!c.validation.valid) {
                c.affordable = false;
                continue;
            }
            int p = c.action.playerIndex();
            int cost = c.action.cost();
            if (virtualEnergy[p] >= cost) {
                c.affordable = true;
                virtualEnergy[p] -= cost;
            } else {
                c.affordable = false;
            }
        }
    }

    private static void resolveExpand(Board board, int turn, List<Candidate> phase, TurnLogSink logger) {
        GameStateSnapshot snapshot = GameStateSnapshot.fromBoard(board, turn);
        Map<String, List<Candidate>> byTarget = new HashMap<>();
        for (Candidate c : phase) {
            if (!c.validation.valid) continue;
            if (!c.affordable) {
                logAction(logger, turn, "EXPAND", c, false, "INSUFFICIENT_ENERGY", 0, board.energy[c.action.playerIndex()]);
                continue;
            }
            int before = board.energy[c.action.playerIndex()];
            board.energy[c.action.playerIndex()] -= c.action.cost();
            int after = board.energy[c.action.playerIndex()];
            if (!snapshot.inBounds(c.action.x(), c.action.y())
                || snapshot.ownerAt(c.action.x(), c.action.y()) != Board.EMPTY
                || !snapshot.hasAdjacentOwnCell(c.action.playerIndex(), c.action.x(), c.action.y())) {
                logAction(logger, turn, "EXPAND", c, false, "INVALID_EXPAND", before, after);
                continue;
            }
            String key = c.action.x() + "," + c.action.y();
            byTarget.computeIfAbsent(key, k -> new ArrayList<>()).add(c);
        }

        Set<Candidate> winners = new HashSet<>();
        for (List<Candidate> conflict : byTarget.values()) {
            Candidate winner = conflict.get(0);
            for (int i = 1; i < conflict.size(); i++) {
                Candidate challenger = conflict.get(i);
                if (compareExpandPriority(snapshot, challenger, winner) < 0) {
                    winner = challenger;
                }
            }
            winners.add(winner);
        }

        for (List<Candidate> conflict : byTarget.values()) {
            for (Candidate c : conflict) {
                int p = c.action.playerIndex();
                if (!winners.contains(c)) {
                    logAction(logger, turn, "EXPAND", c, false, "CONFLICT_LOST", board.energy[p] + c.action.cost(), board.energy[p]);
                    continue;
                }
                if (!board.isEmpty(c.action.x(), c.action.y())) {
                    logAction(logger, turn, "EXPAND", c, false, "TARGET_ALREADY_FILLED", board.energy[p] + c.action.cost(), board.energy[p]);
                    continue;
                }
                board.placeCell(p, c.action.x(), c.action.y());
                logAction(logger, turn, "EXPAND", c, true, "EXPAND_OK", board.energy[p] + c.action.cost(), board.energy[p]);
            }
        }
    }

    private static int compareExpandPriority(GameStateSnapshot snapshot, Candidate a, Candidate b) {
        int aSupport = snapshot.countNeighbors(a.action.playerIndex(), a.action.x(), a.action.y());
        int bSupport = snapshot.countNeighbors(b.action.playerIndex(), b.action.x(), b.action.y());
        if (aSupport != bSupport) {
            return Integer.compare(bSupport, aSupport); // lower comparator value = stronger support
        }
        if (a.action.playerIndex() != b.action.playerIndex()) {
            return Integer.compare(a.action.playerIndex(), b.action.playerIndex());
        }
        return Integer.compare(a.order, b.order);
    }

    private static void resolveAttack(Board board, int turn, List<Candidate> phase, TurnLogSink logger) {
        GameStateSnapshot snapshot = GameStateSnapshot.fromBoard(board, turn);
        Set<String> seenByPlayerTarget = new HashSet<>();
        List<Candidate> successful = new ArrayList<>();

        for (Candidate c : phase) {
            int p = c.action.playerIndex();
            if (!c.validation.valid) continue;
            if (!c.affordable) {
                logAction(logger, turn, "ATTACK", c, false, "INSUFFICIENT_ENERGY", 0, board.energy[p]);
                continue;
            }
            int before = board.energy[p];
            board.energy[p] -= c.action.cost();
            int after = board.energy[p];

            String dedupeKey = p + ":" + c.action.x() + "," + c.action.y();
            if (seenByPlayerTarget.contains(dedupeKey)) {
                logAction(logger, turn, "ATTACK", c, false, "DUPLICATE_TARGET", before, after);
                continue;
            }
            seenByPlayerTarget.add(dedupeKey);

            int defender = 1 - p;
            if (!snapshot.belongsTo(defender, c.action.x(), c.action.y())) {
                logAction(logger, turn, "ATTACK", c, false, "NO_ENEMY_TARGET", before, after);
                continue;
            }
            if (!snapshot.hasAdjacentOwnCell(p, c.action.x(), c.action.y())) {
                logAction(logger, turn, "ATTACK", c, false, "NOT_ADJACENT", before, after);
                continue;
            }
            int myN = snapshot.countNeighbors(p, c.action.x(), c.action.y());
            int oppN = snapshot.countNeighbors(defender, c.action.x(), c.action.y());
            if (myN <= oppN) {
                logAction(logger, turn, "ATTACK", c, false, "NO_NUMERICAL_SUPERIORITY", before, after);
                continue;
            }
            successful.add(c);
        }

        successful.sort(Comparator
            .comparingInt((Candidate c) -> c.action.y())
            .thenComparingInt(c -> c.action.x())
            .thenComparingInt(c -> c.action.playerIndex())
            .thenComparingInt(c -> c.order));

        for (Candidate c : successful) {
            int attacker = c.action.playerIndex();
            int defender = 1 - attacker;
            int before = board.energy[attacker] + c.action.cost();
            if (!board.belongsTo(defender, c.action.x(), c.action.y())) {
                logAction(logger, turn, "ATTACK", c, false, "TARGET_CHANGED", before, board.energy[attacker]);
                continue;
            }
            board.removeCell(defender, c.action.x(), c.action.y());
            board.placeCell(attacker, c.action.x(), c.action.y());
            board.destroyedEnemyCells[attacker]++;
            board.energy[attacker] += GameConfig.ATTACK_REWARD;
            logAction(
                logger,
                turn,
                "ATTACK",
                c,
                true,
                "ATTACK_OK",
                before,
                board.energy[attacker]
            );
        }
    }

    private static void resolveSimple(Board board, int turn, List<Candidate> phase, TurnLogSink logger) {
        GameStateSnapshot snapshot = GameStateSnapshot.fromBoard(board, turn);
        TurnResolutionContext context = new TurnResolutionContext(board, turn, snapshot);
        for (Candidate c : phase) {
            int p = c.action.playerIndex();
            if (!c.validation.valid) continue;
            if (!c.affordable) {
                logAction(logger, turn, c.action.type().name(), c, false, "INSUFFICIENT_ENERGY", 0, board.energy[p]);
                continue;
            }
            int before = board.energy[p];
            ActionResult result = c.action.execute(context);
            logAction(logger, turn, c.action.type().name(), c, result.success, result.status, before, board.energy[p]);
        }
    }

    private static void logStage(TurnLogSink logger, int turn, Stage stage) {
        logger.log("TURN_STAGE {\"turn\":" + turn + ",\"stage\":\"" + stage.name() + "\"}");
    }

    private static void logAction(
        TurnLogSink logger,
        int turn,
        String phase,
        Candidate c,
        boolean success,
        String status,
        int energyBefore,
        int energyAfter
    ) {
        logger.log(
            "TURN_EVENT {\"turn\":" + turn +
                ",\"phase\":\"" + phase + "\"" +
                ",\"player\":" + c.action.playerIndex() +
                ",\"name\":\"" + c.playerName + "\"" +
                ",\"action\":\"" + c.action.asCommand() + "\"" +
                ",\"order\":" + c.order +
                ",\"success\":" + success +
                ",\"status\":\"" + status + "\"" +
                ",\"energyBefore\":" + energyBefore +
                ",\"energyAfter\":" + energyAfter + "}"
        );
    }
}
