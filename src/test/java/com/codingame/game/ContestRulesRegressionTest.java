package com.codingame.game;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Seeded scenarios for contest rules documented in {@code config/statement_en.html}.
 */
class ContestRulesRegressionTest {

    @Test
    void invalidExpandStillConsumesEnergy() {
        Board b = new Board(GameConfig.BOARD_SIZE);
        b.placeCell(0, 0, 0);
        b.energy[0] = 10;
        int before = b.energy[0];
        b.energy[0] = EnergyService.applyEnergyCost(before, ActionParser.ActionType.EXPAND);
        assertFalse(ActionResolver.resolveExpand(b, 0, 5, 5));
        assertEquals(before - GameConfig.EXPAND_COST, b.energy[0]);
    }

    @Test
    void invalidAttackStillConsumesEnergyNoReward() {
        Board b = new Board(GameConfig.BOARD_SIZE);
        b.placeCell(0, 0, 0);
        b.placeCell(1, 10, 10);
        b.energy[0] = 10;
        b.energy[0] = EnergyService.applyEnergyCost(b.energy[0], ActionParser.ActionType.ATTACK);
        int afterPay = b.energy[0];
        int reward = ActionResolver.resolveAttack(b, 0, 10, 10);
        assertEquals(0, reward);
        assertEquals(afterPay, b.energy[0]);
    }

    @Test
    void sameTurnExpandChainIsNotAllowedUnderPhaseSnapshot() {
        Board b = new Board(GameConfig.BOARD_SIZE);
        b.placeCell(0, 5, 5);
        b.energy[0] = 20;
        b.placeCell(1, GameConfig.BOARD_SIZE - 1, GameConfig.BOARD_SIZE - 1);
        b.energy[1] = 20;
        TurnProcessor.processTurn(
            b,
            1,
            java.util.Arrays.asList(
                new TurnProcessor.PlayerSubmission(0, "P0", "EXPAND 5 6;EXPAND 6 6"),
                new TurnProcessor.PlayerSubmission(1, "P1", "WAIT")
            ),
            s -> {}
        );
        assertTrue(b.belongsTo(0, 5, 6));
        assertTrue(b.belongsTo(0, 6, 6));
    }

    @Test
    void sameTurnExpandCannotUseNewlyCreatedChainOnlyCell() {
        Board b = new Board(GameConfig.BOARD_SIZE);
        b.placeCell(0, 5, 5);
        b.placeCell(1, GameConfig.BOARD_SIZE - 1, GameConfig.BOARD_SIZE - 1);
        b.energy[0] = 20;
        b.energy[1] = 20;
        TurnProcessor.processTurn(
            b,
            1,
            java.util.Arrays.asList(
                new TurnProcessor.PlayerSubmission(0, "P0", "EXPAND 6 6;EXPAND 7 7"),
                new TurnProcessor.PlayerSubmission(1, "P1", "WAIT")
            ),
            s -> {}
        );
        assertTrue(b.belongsTo(0, 6, 6));
        assertFalse(b.belongsTo(0, 7, 7), "second expand cannot chain from same-turn new cell");
    }

    @Test
    void mapGenerationDeterministicForSeed() {
        Board a = new Board(GameConfig.BOARD_SIZE);
        Board c = new Board(GameConfig.BOARD_SIZE);
        MapGenerator.generateMap(a, new Random(42), GameConfig.BOARD_SIZE);
        MapGenerator.generateMap(c, new Random(42), GameConfig.BOARD_SIZE);
        assertEquals(a.spots.size(), c.spots.size());
        for (int i = 0; i < a.spots.size(); i++) {
            assertEquals(a.spots.get(i).x, c.spots.get(i).x);
            assertEquals(a.spots.get(i).y, c.spots.get(i).y);
            assertEquals(a.spots.get(i).type, c.spots.get(i).type);
        }
    }

    @Test
    void spotsRespectMinSpawnChebyshevFromP0Corner() {
        Board b = new Board(GameConfig.BOARD_SIZE);
        MapGenerator.generateMap(b, new Random(99), GameConfig.BOARD_SIZE);
        int half = b.size / 2;
        for (Board.NutrientSpot s : b.spots) {
            if (s.x < half) {
                assertTrue(
                    GridUtils.chebyshevDistance(s.x, s.y, 0, 0) >= GameConfig.MIN_SPAWN_CLEARANCE,
                    () -> "spot " + s.x + "," + s.y);
            }
        }
    }
}
