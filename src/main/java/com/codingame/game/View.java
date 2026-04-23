package com.codingame.game;

import com.codingame.gameengine.core.MultiplayerGameManager;
import com.codingame.gameengine.module.entities.Circle;
import com.codingame.gameengine.module.entities.GraphicEntityModule;
import com.codingame.gameengine.module.entities.Rectangle;
import com.codingame.gameengine.module.entities.Text;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * Sparse, delta-based renderer.  Only creates/moves Rectangle entities for
 * cells that are actually occupied, and only touches the SDK on frames where
 * something changed.  Entity objects are pooled and recycled so the total
 * entity count in the SDK stays close to the number of *active* cells.
 */
public class View {

    private static final int WORLD_W = 1920;
    private static final int WORLD_H = 1080;

    private static final int GRID_X = 20;
    private static final int GRID_Y = 28;
    private static final int GRID_PX = 1024;

    private static final int BG        = 0x0a0e14;
    private static final int GRID_BG   = 0x111820;
    private static final int BORDER    = 0x30363d;
    private static final int P0        = 0x22a1e4;
    private static final int P1        = 0xff1d5c;
    private static final int SPOT_S    = 0x2ecc71;
    private static final int SPOT_M    = 0xf1c40f;
    private static final int SPOT_L    = 0xe67e22;
    private static final int DEPLETED  = 0x262d38;
    private static final int TXT       = 0xf0f6fc;
    private static final int DIM       = 0x8b949e;
    private static final int PANEL     = 0x161b22;

    private final GraphicEntityModule gem;
    private final Board board;
    private final MultiplayerGameManager<Player> gm;

    private int cellPx;

    // Cell rendering: key = y * boardSize + x
    private final Map<Integer, Rectangle> liveEntities = new HashMap<>();
    private final Deque<Rectangle> pool = new ArrayDeque<>();

    // Previous frame state for delta detection
    private int[][] prevCells;
    private boolean[] prevSpotDepleted;

    private Circle[] spotCircles;
    private Text turnText;
    private Text[] scoreTexts = new Text[2];
    private Text[] energyTexts = new Text[2];
    private Text[] cellTexts = new Text[2];
    private Rectangle[] bars = new Rectangle[2];

    public View(GraphicEntityModule gem, Board board, MultiplayerGameManager<Player> gm) {
        this.gem = gem;
        this.board = board;
        this.gm = gm;
    }

    public void init() {
        int size = board.size;
        cellPx = GRID_PX / size;
        int gridTotal = cellPx * size;

        gem.createWorld(WORLD_W, WORLD_H);

        gem.createRectangle().setWidth(WORLD_W).setHeight(WORLD_H).setFillColor(BG);
        gem.createRectangle()
            .setX(GRID_X - 3).setY(GRID_Y - 3)
            .setWidth(gridTotal + 6).setHeight(gridTotal + 6)
            .setFillColor(BORDER);
        gem.createRectangle()
            .setX(GRID_X).setY(GRID_Y)
            .setWidth(gridTotal).setHeight(gridTotal)
            .setFillColor(GRID_BG);

        // Nutrient spots
        spotCircles = new Circle[board.spots.size()];
        prevSpotDepleted = new boolean[board.spots.size()];
        int r = Math.max(cellPx / 3, 3);
        for (int i = 0; i < board.spots.size(); i++) {
            Board.NutrientSpot s = board.spots.get(i);
            spotCircles[i] = gem.createCircle()
                .setX(GRID_X + s.x * cellPx + cellPx / 2)
                .setY(GRID_Y + s.y * cellPx + cellPx / 2)
                .setRadius(r)
                .setFillColor(spotColor(s)).setAlpha(0.85);
        }

        // Snapshot initial board state and render occupied cells
        prevCells = new int[size][size];
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                int owner = board.cells[y][x];
                prevCells[y][x] = owner;
                if (owner != Board.EMPTY) {
                    acquire(x, y, owner == Board.PLAYER0 ? P0 : P1);
                }
            }
        }

        buildHUD();
        // GraphicEntityModule expects a normalized instant in [0, 1], not a turn index.
        gem.commitWorldState(0);
    }

    /** Normalized frame key for {@link GraphicEntityModule#commitWorldState(double)} (must stay in [0, 1]). */
    private double frameInstantForTurn(int turn) {
        int max = Math.max(1, gm.getMaxTurns());
        return Math.min(1.0, Math.max(0.0, (double) turn / max));
    }

    /* ---- delta update --------------------------------------------------- */

    public void update(Board board, int turn) {
        int size = board.size;

        // 1. Only touch cells that actually changed since last frame
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                int cur = board.cells[y][x];
                if (cur != prevCells[y][x]) {
                    if (cur == Board.EMPTY) {
                        recycle(key(x, y));
                    } else {
                        acquire(x, y, cur == Board.PLAYER0 ? P0 : P1);
                    }
                    prevCells[y][x] = cur;
                }
            }
        }

        // 2. Only update spots whose depletion state flipped
        for (int i = 0; i < board.spots.size(); i++) {
            boolean dep = board.spots.get(i).isDepleted();
            if (dep != prevSpotDepleted[i]) {
                spotCircles[i].setFillColor(DEPLETED).setAlpha(0.3);
                prevSpotDepleted[i] = dep;
            }
        }

        // 3. HUD (cheap text updates)
        turnText.setText("Turn " + turn + " / " + GameConfig.MAX_TURNS);
        int maxBar = WORLD_W - (GRID_X + cellPx * size + 60);
        for (int p = 0; p < 2; p++) {
            scoreTexts[p].setText("Score: " + VictoryChecker.computeScore(board, p));
            energyTexts[p].setText("Energy: " + board.energy[p]);
            cellTexts[p].setText("Cells: " + board.playerCells[p].size());
            bars[p].setWidth(Math.max(1, Math.min(board.energy[p] * 3, maxBar)));
        }
        gem.commitWorldState(frameInstantForTurn(turn));
    }

    /* ---- entity pool ---------------------------------------------------- */

    private void acquire(int x, int y, int color) {
        int k = key(x, y);
        Rectangle r = liveEntities.get(k);
        if (r != null) {
            r.setFillColor(color).setAlpha(1.0);
            return;
        }
        r = pool.pollFirst();
        if (r == null) {
            r = gem.createRectangle()
                .setWidth(cellPx - 1).setHeight(cellPx - 1);
        }
        r.setX(GRID_X + x * cellPx)
         .setY(GRID_Y + y * cellPx)
         .setFillColor(color).setAlpha(1.0);
        liveEntities.put(k, r);
    }

    private void recycle(int k) {
        Rectangle r = liveEntities.remove(k);
        if (r != null) {
            r.setAlpha(0.0);
            pool.addLast(r);
        }
    }

    private int key(int x, int y) {
        return y * board.size + x;
    }

    /* ---- HUD ------------------------------------------------------------ */

    private void buildHUD() {
        int hx = GRID_X + cellPx * board.size + 30;
        int hw = WORLD_W - hx - 20;

        gem.createText("BacterI.A.").setX(hx).setY(24)
            .setFillColor(TXT).setFontSize(36).setFontWeight(Text.FontWeight.BOLD);
        turnText = gem.createText("Turn 0 / " + GameConfig.MAX_TURNS)
            .setX(hx).setY(72).setFillColor(DIM).setFontSize(22);

        playerPanel(hx, 130, hw, 0);
        playerPanel(hx, 380, hw, 1);

        int ly = 640;
        gem.createText("NUTRIENTS").setX(hx).setY(ly)
            .setFillColor(DIM).setFontSize(18).setFontWeight(Text.FontWeight.BOLD);
        ly += 36; legend(hx, ly, SPOT_S, "Small (10)");
        ly += 32; legend(hx, ly, SPOT_M, "Medium (30)");
        ly += 32; legend(hx, ly, SPOT_L, "Large (70)");
        ly += 32; legend(hx, ly, DEPLETED, "Depleted");
    }

    private void playerPanel(int x, int y, int w, int p) {
        int c = p == 0 ? P0 : P1;
        gem.createRectangle().setX(x - 8).setY(y).setWidth(w + 8).setHeight(200)
            .setFillColor(PANEL).setLineWidth(1).setLineColor(BORDER);
        gem.createRectangle().setX(x - 8).setY(y).setWidth(4).setHeight(200).setFillColor(c);
        gem.createText(gm.getPlayer(p).getNicknameToken())
            .setX(x + 8).setY(y + 12).setFillColor(TXT).setFontSize(26)
            .setFontWeight(Text.FontWeight.BOLD);
        gem.createText("PLAYER " + p).setX(x + 8).setY(y + 48)
            .setFillColor(c).setFontSize(14).setFontWeight(Text.FontWeight.BOLD);

        scoreTexts[p] = gem.createText("Score: " + VictoryChecker.computeScore(board, p))
            .setX(x + 8).setY(y + 78).setFillColor(TXT).setFontSize(22);
        energyTexts[p] = gem.createText("Energy: " + board.energy[p])
            .setX(x + 8).setY(y + 112).setFillColor(DIM).setFontSize(18);
        bars[p] = gem.createRectangle().setX(x + 8).setY(y + 140)
            .setWidth(Math.max(1, Math.min(board.energy[p] * 3, w - 20)))
            .setHeight(8).setFillColor(c).setAlpha(0.7);
        cellTexts[p] = gem.createText("Cells: " + board.playerCells[p].size())
            .setX(x + 8).setY(y + 160).setFillColor(DIM).setFontSize(18);
    }

    private void legend(int x, int y, int c, String label) {
        gem.createCircle().setX(x + 8).setY(y + 10).setRadius(7)
            .setFillColor(c).setAlpha(c == DEPLETED ? 0.5 : 0.85);
        gem.createText(label).setX(x + 26).setY(y).setFillColor(DIM).setFontSize(16);
    }

    private static int spotColor(Board.NutrientSpot s) {
        switch (s.type) {
            case SMALL:  return SPOT_S;
            case MEDIUM: return SPOT_M;
            case LARGE:  return SPOT_L;
            default:     return DEPLETED;
        }
    }
}
