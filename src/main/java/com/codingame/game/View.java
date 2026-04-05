package com.codingame.game;

import com.codingame.gameengine.core.MultiplayerGameManager;
import com.codingame.gameengine.module.entities.Circle;
import com.codingame.gameengine.module.entities.GraphicEntityModule;
import com.codingame.gameengine.module.entities.Rectangle;
import com.codingame.gameengine.module.entities.Text;

public class View {

    private static final int WORLD_W = 1920;
    private static final int WORLD_H = 1080;

    // Grid area: square, centered vertically, left-aligned with margin
    private static final int GRID_MARGIN_X = 40;
    private static final int GRID_MARGIN_Y = 60;
    private static final int GRID_PX = 960; // total grid pixel size

    // Colors
    private static final int BG_COLOR       = 0x0d1117;
    private static final int EMPTY_COLOR    = 0x161b22;
    private static final int P0_COLOR       = 0x22a1e4;
    private static final int P1_COLOR       = 0xff1d5c;
    private static final int SPOT_S_COLOR   = 0x2ecc71;
    private static final int SPOT_M_COLOR   = 0xf1c40f;
    private static final int SPOT_L_COLOR   = 0xe67e22;
    private static final int DEPLETED_COLOR = 0x30363d;

    private final GraphicEntityModule gem;
    private final Board board;
    private final MultiplayerGameManager<Player> gm;

    private int cellPx;
    private Rectangle[][] tiles;
    private Circle[] spotCircles;
    private Text turnText;
    private Text[] energyTexts;
    private Text[] cellCountTexts;

    public View(GraphicEntityModule gem, Board board, MultiplayerGameManager<Player> gm) {
        this.gem = gem;
        this.board = board;
        this.gm = gm;
    }

    public void init() {
        gem.createWorld(WORLD_W, WORLD_H);
        cellPx = GRID_PX / board.size;

        // Background
        gem.createRectangle()
            .setWidth(WORLD_W).setHeight(WORLD_H)
            .setFillColor(BG_COLOR);

        // Grid tiles
        tiles = new Rectangle[board.size][board.size];
        for (int y = 0; y < board.size; y++) {
            for (int x = 0; x < board.size; x++) {
                tiles[y][x] = gem.createRectangle()
                    .setX(GRID_MARGIN_X + x * cellPx)
                    .setY(GRID_MARGIN_Y + y * cellPx)
                    .setWidth(cellPx - 1)
                    .setHeight(cellPx - 1)
                    .setFillColor(EMPTY_COLOR);
            }
        }

        // Nutrient spot overlays
        spotCircles = new Circle[board.spots.size()];
        int r = Math.max(cellPx / 3, 2);
        for (int i = 0; i < board.spots.size(); i++) {
            Board.NutrientSpot s = board.spots.get(i);
            spotCircles[i] = gem.createCircle()
                .setX(GRID_MARGIN_X + s.x * cellPx + cellPx / 2)
                .setY(GRID_MARGIN_Y + s.y * cellPx + cellPx / 2)
                .setRadius(r)
                .setFillColor(spotColor(s))
                .setAlpha(1.0);
        }

        // Color initial cells
        colorCell(0, 0, P0_COLOR);
        colorCell(board.size - 1, board.size - 1, P1_COLOR);

        // HUD
        int hudX = GRID_MARGIN_X + GRID_PX + 40;
        int hudW = WORLD_W - hudX - 20;
        int col = hudX + hudW / 2;

        turnText = gem.createText("Turn 1")
            .setX(hudX).setY(20)
            .setFillColor(0xffffff)
            .setFontSize(28);

        // Player 0 HUD
        gem.createRectangle().setX(hudX).setY(90).setWidth(24).setHeight(24).setFillColor(P0_COLOR);
        gem.createText(gm.getPlayer(0).getNicknameToken())
            .setX(hudX + 32).setY(90).setFillColor(0xffffff).setFontSize(22);

        energyTexts = new Text[2];
        cellCountTexts = new Text[2];

        energyTexts[0] = gem.createText("Energy: " + board.energy[0])
            .setX(hudX).setY(125).setFillColor(0xc9d1d9).setFontSize(20);
        cellCountTexts[0] = gem.createText("Cells: " + board.playerCells[0].size())
            .setX(hudX).setY(155).setFillColor(0xc9d1d9).setFontSize(20);

        // Player 1 HUD
        gem.createRectangle().setX(hudX).setY(220).setWidth(24).setHeight(24).setFillColor(P1_COLOR);
        gem.createText(gm.getPlayer(1).getNicknameToken())
            .setX(hudX + 32).setY(220).setFillColor(0xffffff).setFontSize(22);

        energyTexts[1] = gem.createText("Energy: " + board.energy[1])
            .setX(hudX).setY(255).setFillColor(0xc9d1d9).setFontSize(20);
        cellCountTexts[1] = gem.createText("Cells: " + board.playerCells[1].size())
            .setX(hudX).setY(285).setFillColor(0xc9d1d9).setFontSize(20);

        // Legend
        int ly = 370;
        gem.createText("Nutrients")
            .setX(hudX).setY(ly).setFillColor(0xffffff).setFontSize(20);
        ly += 35;
        gem.createCircle().setX(hudX + 8).setY(ly + 8).setRadius(6).setFillColor(SPOT_S_COLOR);
        gem.createText("Small (10)").setX(hudX + 24).setY(ly).setFillColor(0xc9d1d9).setFontSize(16);
        ly += 28;
        gem.createCircle().setX(hudX + 8).setY(ly + 8).setRadius(6).setFillColor(SPOT_M_COLOR);
        gem.createText("Medium (30)").setX(hudX + 24).setY(ly).setFillColor(0xc9d1d9).setFontSize(16);
        ly += 28;
        gem.createCircle().setX(hudX + 8).setY(ly + 8).setRadius(6).setFillColor(SPOT_L_COLOR);
        gem.createText("Large (70)").setX(hudX + 24).setY(ly).setFillColor(0xc9d1d9).setFontSize(16);
    }

    public void update(Board board, int turn) {
        // Update tiles
        for (int y = 0; y < board.size; y++) {
            for (int x = 0; x < board.size; x++) {
                int owner = board.cells[y][x];
                int color;
                switch (owner) {
                    case Board.PLAYER0: color = P0_COLOR; break;
                    case Board.PLAYER1: color = P1_COLOR; break;
                    default:            color = EMPTY_COLOR; break;
                }
                tiles[y][x].setFillColor(color);
            }
        }

        // Update nutrient spot overlays
        for (int i = 0; i < board.spots.size(); i++) {
            Board.NutrientSpot s = board.spots.get(i);
            if (s.isDepleted()) {
                spotCircles[i].setFillColor(DEPLETED_COLOR).setAlpha(0.3);
            } else {
                spotCircles[i].setFillColor(spotColor(s)).setAlpha(1.0);
            }
        }

        // Update HUD
        turnText.setText("Turn " + turn);
        for (int p = 0; p < 2; p++) {
            energyTexts[p].setText("Energy: " + board.energy[p]);
            cellCountTexts[p].setText("Cells: " + board.playerCells[p].size());
        }

        gem.commitWorldState(1.0);
    }

    private void colorCell(int x, int y, int color) {
        tiles[y][x].setFillColor(color);
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
