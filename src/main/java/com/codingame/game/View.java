package com.codingame.game;

import com.codingame.gameengine.core.MultiplayerGameManager;
import com.codingame.gameengine.module.entities.Circle;
import com.codingame.gameengine.module.entities.GraphicEntityModule;
import com.codingame.gameengine.module.entities.Rectangle;
import com.codingame.gameengine.module.entities.Text;

import java.util.HashMap;
import java.util.Map;

public class View {

    private static final int WORLD_W = 1920;
    private static final int WORLD_H = 1080;

    private static final int GRID_ORIGIN_X = 20;
    private static final int GRID_ORIGIN_Y = 28;
    private static final int GRID_PX = 1024;

    private static final int BG_COLOR        = 0x0a0e14;
    private static final int GRID_BG_COLOR   = 0x111820;
    private static final int GRID_BORDER     = 0x30363d;
    private static final int P0_COLOR        = 0x22a1e4;
    private static final int P1_COLOR        = 0xff1d5c;
    private static final int SPOT_S_COLOR    = 0x2ecc71;
    private static final int SPOT_M_COLOR    = 0xf1c40f;
    private static final int SPOT_L_COLOR    = 0xe67e22;
    private static final int DEPLETED_COLOR  = 0x262d38;
    private static final int TEXT_WHITE      = 0xf0f6fc;
    private static final int TEXT_DIM        = 0x8b949e;
    private static final int HUD_PANEL       = 0x161b22;

    private final GraphicEntityModule gem;
    private final Board board;
    private final MultiplayerGameManager<Player> gm;

    private int cellPx;
    private final Map<Long, Rectangle> cellEntities = new HashMap<>();
    private Circle[] spotCircles;
    private Text turnText;
    private Text[] scoreTexts = new Text[2];
    private Text[] energyTexts = new Text[2];
    private Text[] cellCountTexts = new Text[2];
    private Rectangle[] energyBars = new Rectangle[2];

    public View(GraphicEntityModule gem, Board board, MultiplayerGameManager<Player> gm) {
        this.gem = gem;
        this.board = board;
        this.gm = gm;
    }

    public void init() {
        gem.createWorld(WORLD_W, WORLD_H);
        cellPx = GRID_PX / board.size;
        int gridTotal = cellPx * board.size;

        // Background
        gem.createRectangle()
            .setWidth(WORLD_W).setHeight(WORLD_H)
            .setFillColor(BG_COLOR);

        // Grid border
        gem.createRectangle()
            .setX(GRID_ORIGIN_X - 3).setY(GRID_ORIGIN_Y - 3)
            .setWidth(gridTotal + 6).setHeight(gridTotal + 6)
            .setFillColor(GRID_BORDER);

        // Grid background
        gem.createRectangle()
            .setX(GRID_ORIGIN_X).setY(GRID_ORIGIN_Y)
            .setWidth(gridTotal).setHeight(gridTotal)
            .setFillColor(GRID_BG_COLOR);

        // Nutrient spot circles (drawn on the grid background)
        spotCircles = new Circle[board.spots.size()];
        int r = Math.max(cellPx / 3, 3);
        for (int i = 0; i < board.spots.size(); i++) {
            Board.NutrientSpot s = board.spots.get(i);
            spotCircles[i] = gem.createCircle()
                .setX(GRID_ORIGIN_X + s.x * cellPx + cellPx / 2)
                .setY(GRID_ORIGIN_Y + s.y * cellPx + cellPx / 2)
                .setRadius(r)
                .setFillColor(spotColor(s))
                .setAlpha(0.85);
        }

        // Initial player cells
        for (Board.Point p : board.playerCells[0]) {
            showCell(p.x, p.y, P0_COLOR);
        }
        for (Board.Point p : board.playerCells[1]) {
            showCell(p.x, p.y, P1_COLOR);
        }

        initHUD();

        gem.commitWorldState(0);
    }

    private void initHUD() {
        int hudX = GRID_ORIGIN_X + cellPx * board.size + 30;
        int hudW = WORLD_W - hudX - 20;

        gem.createText("BacterI.A.")
            .setX(hudX).setY(24)
            .setFillColor(TEXT_WHITE)
            .setFontSize(36)
            .setFontWeight(Text.FontWeight.BOLD);

        turnText = gem.createText("Turn 0 / " + GameLogic.MAX_TURNS)
            .setX(hudX).setY(72)
            .setFillColor(TEXT_DIM)
            .setFontSize(22);

        drawPlayerPanel(hudX, 130, hudW, 0);
        drawPlayerPanel(hudX, 380, hudW, 1);

        // Legend
        int ly = 640;
        gem.createText("NUTRIENTS")
            .setX(hudX).setY(ly)
            .setFillColor(TEXT_DIM)
            .setFontSize(18)
            .setFontWeight(Text.FontWeight.BOLD);

        ly += 36;
        drawLegendItem(hudX, ly, SPOT_S_COLOR, "Small (10)");
        ly += 32;
        drawLegendItem(hudX, ly, SPOT_M_COLOR, "Medium (30)");
        ly += 32;
        drawLegendItem(hudX, ly, SPOT_L_COLOR, "Large (70)");
        ly += 32;
        drawLegendItem(hudX, ly, DEPLETED_COLOR, "Depleted");
    }

    private void drawPlayerPanel(int x, int y, int w, int pIdx) {
        int pColor = pIdx == 0 ? P0_COLOR : P1_COLOR;

        gem.createRectangle()
            .setX(x - 8).setY(y)
            .setWidth(w + 8).setHeight(200)
            .setFillColor(HUD_PANEL)
            .setLineWidth(1).setLineColor(GRID_BORDER);

        gem.createRectangle()
            .setX(x - 8).setY(y)
            .setWidth(4).setHeight(200)
            .setFillColor(pColor);

        gem.createText(gm.getPlayer(pIdx).getNicknameToken())
            .setX(x + 8).setY(y + 12)
            .setFillColor(TEXT_WHITE)
            .setFontSize(26)
            .setFontWeight(Text.FontWeight.BOLD);

        gem.createText("PLAYER " + pIdx)
            .setX(x + 8).setY(y + 48)
            .setFillColor(pColor)
            .setFontSize(14)
            .setFontWeight(Text.FontWeight.BOLD);

        scoreTexts[pIdx] = gem.createText("Score: " + GameLogic.computeScore(board, pIdx))
            .setX(x + 8).setY(y + 78)
            .setFillColor(TEXT_WHITE)
            .setFontSize(22);

        energyTexts[pIdx] = gem.createText("Energy: " + board.energy[pIdx])
            .setX(x + 8).setY(y + 112)
            .setFillColor(TEXT_DIM)
            .setFontSize(18);

        int barMaxW = Math.max(w - 20, 1);
        energyBars[pIdx] = gem.createRectangle()
            .setX(x + 8).setY(y + 140)
            .setWidth(Math.max(Math.min(board.energy[pIdx] * 3, barMaxW), 1))
            .setHeight(8)
            .setFillColor(pColor)
            .setAlpha(0.7);

        cellCountTexts[pIdx] = gem.createText("Cells: " + board.playerCells[pIdx].size())
            .setX(x + 8).setY(y + 160)
            .setFillColor(TEXT_DIM)
            .setFontSize(18);
    }

    private void drawLegendItem(int x, int y, int color, String label) {
        gem.createCircle()
            .setX(x + 8).setY(y + 10)
            .setRadius(7)
            .setFillColor(color)
            .setAlpha(color == DEPLETED_COLOR ? 0.5 : 0.85);
        gem.createText(label)
            .setX(x + 26).setY(y)
            .setFillColor(TEXT_DIM)
            .setFontSize(16);
    }

    public void update(Board board, int turn) {
        // Hide all tracked cell entities
        for (Rectangle r : cellEntities.values()) {
            r.setAlpha(0.0);
        }

        // Show cells for both players
        for (Board.Point p : board.playerCells[0]) {
            showCell(p.x, p.y, P0_COLOR);
        }
        for (Board.Point p : board.playerCells[1]) {
            showCell(p.x, p.y, P1_COLOR);
        }

        // Update nutrient spot overlays
        for (int i = 0; i < board.spots.size(); i++) {
            Board.NutrientSpot s = board.spots.get(i);
            if (s.isDepleted()) {
                spotCircles[i].setFillColor(DEPLETED_COLOR).setAlpha(0.3);
            } else {
                spotCircles[i].setFillColor(spotColor(s)).setAlpha(0.85);
            }
        }

        // Update HUD
        turnText.setText("Turn " + turn + " / " + GameLogic.MAX_TURNS);
        int barMaxW = WORLD_W - (GRID_ORIGIN_X + cellPx * board.size + 30) - 30;
        for (int p = 0; p < 2; p++) {
            scoreTexts[p].setText("Score: " + GameLogic.computeScore(board, p));
            energyTexts[p].setText("Energy: " + board.energy[p]);
            cellCountTexts[p].setText("Cells: " + board.playerCells[p].size());
            energyBars[p].setWidth(Math.max(Math.min(board.energy[p] * 3, barMaxW), 1));
        }

        gem.commitWorldState(1.0);
    }

    private void showCell(int x, int y, int color) {
        long key = (long) y * board.size + x;
        Rectangle r = cellEntities.get(key);
        if (r == null) {
            r = gem.createRectangle()
                .setX(GRID_ORIGIN_X + x * cellPx)
                .setY(GRID_ORIGIN_Y + y * cellPx)
                .setWidth(cellPx - 1)
                .setHeight(cellPx - 1);
            cellEntities.put(key, r);
        }
        r.setFillColor(color).setAlpha(1.0);
    }

    private static int spotColor(Board.NutrientSpot s) {
        switch (s.type) {
            case SMALL:  return SPOT_S_COLOR;
            case MEDIUM: return SPOT_M_COLOR;
            case LARGE:  return SPOT_L_COLOR;
            default:     return DEPLETED_COLOR;
        }
    }
}
