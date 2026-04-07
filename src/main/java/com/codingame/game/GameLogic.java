package com.codingame.game;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

public class GameLogic {

    public static final int MAX_ACTIONS_PER_TURN = 5;
    public static final int EXPAND_COST  = 2;
    public static final int ATTACK_COST  = 2;
    public static final int ATTACK_REWARD = 3;
    public static final int VISION_RADIUS = 3;
    public static final int MAX_TURNS    = 500;

    // -----------------------------------------------------------------------
    // Action types and parsing
    // -----------------------------------------------------------------------

    public enum ActionType { EXPAND, ATTACK, AUTOPHAGY, WAIT }

    public static class Action {
        public final ActionType type;
        public final int x, y;

        public Action(ActionType type, int x, int y) {
            this.type = type;
            this.x = x;
            this.y = y;
        }

        public Action(ActionType type) { this(type, -1, -1); }

        @Override
        public String toString() {
            return type == ActionType.WAIT ? "WAIT" : type + " " + x + " " + y;
        }
    }

    public static List<Action> parseActions(String line) {
        if (line == null || line.isBlank()) {
            throw new IllegalArgumentException("Output vazio.");
        }
        String[] parts = line.split(";");
        if (parts.length > MAX_ACTIONS_PER_TURN) {
            throw new IllegalArgumentException(
                "Limite de " + MAX_ACTIONS_PER_TURN + " accoes excedido (" + parts.length + ").");
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
        if (type == ActionType.WAIT) return new Action(type);
        if (p.length < 3) {
            throw new IllegalArgumentException("Accao " + type + " requer coordenadas (x y).");
        }
        return new Action(type, Integer.parseInt(p[1]), Integer.parseInt(p[2]));
    }

    // -----------------------------------------------------------------------
    // Energy helpers
    // -----------------------------------------------------------------------

    public static int applyEnergyCost(int energy, ActionType type) {
        switch (type) {
            case EXPAND:
            case ATTACK:    return energy - EXPAND_COST;
            case AUTOPHAGY: return energy + 1;
            default:        return energy;
        }
    }

    public static boolean canAfford(int energy, ActionType type) {
        switch (type) {
            case EXPAND:
            case ATTACK: return energy >= EXPAND_COST;
            default:     return true;
        }
    }

    // -----------------------------------------------------------------------
    // Grid helpers
    // -----------------------------------------------------------------------

    public static boolean isAdjacent(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) <= 1 && Math.abs(y1 - y2) <= 1 && !(x1 == x2 && y1 == y2);
    }

    public static boolean isInBounds(int x, int y, int size) {
        return x >= 0 && x < size && y >= 0 && y < size;
    }

    public static int chebyshevDistance(int x1, int y1, int x2, int y2) {
        return Math.max(Math.abs(x1 - x2), Math.abs(y1 - y2));
    }

    public static boolean hasAdjacentOwnCell(Board board, int playerIdx, int x, int y) {
        int owner = playerIdx + 1;
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                if (dx == 0 && dy == 0) continue;
                int nx = x + dx, ny = y + dy;
                if (isInBounds(nx, ny, board.size) && board.cells[ny][nx] == owner) {
                    return true;
                }
            }
        }
        return false;
    }

    public static int countNeighbors(Board board, int playerIdx, int x, int y) {
        int owner = playerIdx + 1;
        int count = 0;
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                if (dx == 0 && dy == 0) continue;
                int nx = x + dx, ny = y + dy;
                if (isInBounds(nx, ny, board.size) && board.cells[ny][nx] == owner) {
                    count++;
                }
            }
        }
        return count;
    }

    // -----------------------------------------------------------------------
    // Map generation
    // -----------------------------------------------------------------------

    private static final int MIN_SPOTS = 8;
    private static final int MAX_SPOTS = 12;
    private static final int MIN_SPOT_DISTANCE = 5;

    public static void generateMap(Board board, Random rng, int size) {
        int spotCount = MIN_SPOTS + rng.nextInt(MAX_SPOTS - MIN_SPOTS + 1);
        int half = size / 2;
        List<Board.NutrientSpot> firstHalf = new ArrayList<>();

        int attempts = 0;
        while (firstHalf.size() < spotCount && attempts < 1000) {
            attempts++;
            int x = 1 + rng.nextInt(half - 2);
            int y = 1 + rng.nextInt(size - 2);
            if (x <= 2 && y <= 2) continue; // avoid spawn corner
            boolean tooClose = false;
            for (Board.NutrientSpot existing : firstHalf) {
                if (chebyshevDistance(x, y, existing.x, existing.y) < MIN_SPOT_DISTANCE) {
                    tooClose = true;
                    break;
                }
            }
            if (tooClose) continue;

            Board.SpotType type = pickSpotType(rng);
            firstHalf.add(new Board.NutrientSpot(x, y, type));
        }

        for (Board.NutrientSpot s : firstHalf) {
            board.spots.add(s);
            int mx = size - 1 - s.x;
            int my = size - 1 - s.y;
            board.spots.add(new Board.NutrientSpot(mx, my, s.type));
        }

        board.placeCell(0, 0, 0);
        board.placeCell(1, size - 1, size - 1);
    }

    private static Board.SpotType pickSpotType(Random rng) {
        int roll = rng.nextInt(10);
        if (roll < 5) return Board.SpotType.SMALL;
        if (roll < 8) return Board.SpotType.MEDIUM;
        return Board.SpotType.LARGE;
    }

    // -----------------------------------------------------------------------
    // Starting energy via BFS
    // -----------------------------------------------------------------------

    public static int computeStartingEnergy(Board board, int startX, int startY) {
        int minDist = bfsToNearestSpot(board, startX, startY);
        return Math.max(minDist + 5, 10);
    }

    private static int bfsToNearestSpot(Board board, int sx, int sy) {
        boolean[][] visited = new boolean[board.size][board.size];
        Queue<int[]> queue = new LinkedList<>();
        queue.add(new int[]{sx, sy, 0});
        visited[sy][sx] = true;

        while (!queue.isEmpty()) {
            int[] cur = queue.poll();
            int x = cur[0], y = cur[1], dist = cur[2];
            for (Board.NutrientSpot spot : board.spots) {
                if (spot.x == x && spot.y == y) return dist;
            }
            for (int dy = -1; dy <= 1; dy++) {
                for (int dx = -1; dx <= 1; dx++) {
                    if (dx == 0 && dy == 0) continue;
                    int nx = x + dx, ny = y + dy;
                    if (isInBounds(nx, ny, board.size) && !visited[ny][nx]) {
                        visited[ny][nx] = true;
                        queue.add(new int[]{nx, ny, dist + 1});
                    }
                }
            }
        }
        return 20; // fallback
    }

    // -----------------------------------------------------------------------
    // Fog of War
    // -----------------------------------------------------------------------

    public static class VisibleState {
        public final List<Board.Point> myCells = new ArrayList<>();
        public final List<Board.Point> oppCells = new ArrayList<>();
        public final List<Board.NutrientSpot> visibleSpots = new ArrayList<>();
    }

    public static VisibleState getVisibleEntities(Board board, int playerIdx) {
        VisibleState vs = new VisibleState();
        Set<Board.Point> own = board.playerCells[playerIdx];
        int oppIdx = 1 - playerIdx;
        int half = board.size / 2;
        int size = board.size;

        // Build visibility bitmap: O(ownCells * (2R+1)^2) instead of O(own*opp)
        boolean[][] visible = new boolean[size][size];
        for (Board.Point c : own) {
            int xMin = Math.max(0, c.x - VISION_RADIUS);
            int xMax = Math.min(size - 1, c.x + VISION_RADIUS);
            int yMin = Math.max(0, c.y - VISION_RADIUS);
            int yMax = Math.min(size - 1, c.y + VISION_RADIUS);
            for (int vy = yMin; vy <= yMax; vy++) {
                for (int vx = xMin; vx <= xMax; vx++) {
                    visible[vy][vx] = true;
                }
            }
        }

        vs.myCells.addAll(own);

        for (Board.Point opp : board.playerCells[oppIdx]) {
            if (visible[opp.y][opp.x]) {
                vs.oppCells.add(opp);
            }
        }

        boolean isPlayer0 = (playerIdx == 0);
        for (Board.NutrientSpot spot : board.spots) {
            boolean ownHalf = isPlayer0 ? (spot.x < half) : (spot.x >= half);
            if (ownHalf || visible[spot.y][spot.x]) {
                vs.visibleSpots.add(spot);
            }
        }

        return vs;
    }

    // -----------------------------------------------------------------------
    // Action resolution
    // -----------------------------------------------------------------------

    public static boolean resolveExpand(Board board, int playerIdx, int x, int y) {
        if (!isInBounds(x, y, board.size)) return false;
        if (!board.isEmpty(x, y)) return false;
        if (!hasAdjacentOwnCell(board, playerIdx, x, y)) return false;
        board.placeCell(playerIdx, x, y);
        return true;
    }

    /** Returns energy gained (ATTACK_REWARD on success, 0 on fail). Energy cost already deducted by caller. */
    public static int resolveAttack(Board board, int attackerIdx, int tx, int ty) {
        if (!isInBounds(tx, ty, board.size)) return 0;
        int defenderIdx = 1 - attackerIdx;
        if (!board.belongsTo(defenderIdx, tx, ty)) return 0;
        if (!hasAdjacentOwnCell(board, attackerIdx, tx, ty)) return 0;

        int attackerNeighbors = countNeighbors(board, attackerIdx, tx, ty);
        int defenderNeighbors = countNeighbors(board, defenderIdx, tx, ty);

        if (attackerNeighbors > defenderNeighbors) {
            board.removeCell(defenderIdx, tx, ty);
            board.placeCell(attackerIdx, tx, ty);
            board.cellsDestroyed[attackerIdx]++;
            return ATTACK_REWARD;
        }
        return 0;
    }

    public static boolean resolveAutophagy(Board board, int playerIdx, int x, int y) {
        if (!isInBounds(x, y, board.size)) return false;
        if (!board.belongsTo(playerIdx, x, y)) return false;
        board.removeCell(playerIdx, x, y);
        return true;
    }

    // -----------------------------------------------------------------------
    // Passive extraction
    // -----------------------------------------------------------------------

    public static void passiveExtraction(Board board) {
        for (Board.NutrientSpot spot : board.spots) {
            if (spot.isDepleted()) continue;
            int owner = board.getOwner(spot.x, spot.y);
            if (owner == Board.EMPTY) continue;
            int playerIdx = owner - 1;
            spot.remainingEnergy--;
            board.energy[playerIdx]++;
        }
    }

    // -----------------------------------------------------------------------
    // Scoring and victory
    // -----------------------------------------------------------------------

    public static int computeScore(Board board, int playerIdx) {
        return board.playerCells[playerIdx].size() * 100 + board.energy[playerIdx];
    }

    public static int checkGameOver(Board board, int turn) {
        if (board.playerCells[0].isEmpty() && board.playerCells[1].isEmpty()) return -1;
        if (board.playerCells[0].isEmpty()) return 1;
        if (board.playerCells[1].isEmpty()) return 0;
        if (turn >= MAX_TURNS) {
            int s0 = computeScore(board, 0);
            int s1 = computeScore(board, 1);
            if (s0 > s1) return 0;
            if (s1 > s0) return 1;
            if (board.cellsDestroyed[0] > board.cellsDestroyed[1]) return 0;
            if (board.cellsDestroyed[1] > board.cellsDestroyed[0]) return 1;
            return -1; // true draw
        }
        return -2; // game not over
    }
}
