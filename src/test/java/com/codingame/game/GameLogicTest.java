package com.codingame.game;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GameLogic")
class GameLogicTest {

    // -----------------------------------------------------------------------
    // parseActions
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("parseActions()")
    class ParseActions {

        @Test void singleWait() {
            List<ActionParser.Action> a = GameLogic.parseActions("WAIT");
            assertEquals(1, a.size());
            assertEquals(ActionParser.ActionType.WAIT, a.get(0).type);
        }

        @Test void expandWithCoords() {
            ActionParser.Action a = GameLogic.parseActions("EXPAND 10 15").get(0);
            assertEquals(ActionParser.ActionType.EXPAND, a.type);
            assertEquals(10, a.x);
            assertEquals(15, a.y);
        }

        @Test void multipleActions() {
            List<ActionParser.Action> a = GameLogic.parseActions("EXPAND 1 2; ATTACK 3 4; WAIT");
            assertEquals(3, a.size());
            assertEquals(ActionParser.ActionType.EXPAND, a.get(0).type);
            assertEquals(ActionParser.ActionType.ATTACK, a.get(1).type);
            assertEquals(ActionParser.ActionType.WAIT, a.get(2).type);
        }

        @Test void fiveActionsAllowed() {
            assertEquals(5, GameLogic.parseActions("WAIT;WAIT;WAIT;WAIT;WAIT").size());
        }

        @Test void sixActionsThrows() {
            assertThrows(IllegalArgumentException.class,
                () -> GameLogic.parseActions("WAIT;WAIT;WAIT;WAIT;WAIT;WAIT"));
        }

        @Test void unknownCommand() {
            assertThrows(IllegalArgumentException.class,
                () -> GameLogic.parseActions("DANCE 1 1"));
        }

        @Test void expandMissingCoords() {
            assertThrows(IllegalArgumentException.class,
                () -> GameLogic.parseActions("EXPAND"));
        }

        @Test void emptyOutput() {
            assertThrows(IllegalArgumentException.class, () -> GameLogic.parseActions(""));
            assertThrows(IllegalArgumentException.class, () -> GameLogic.parseActions(null));
        }
    }

    // -----------------------------------------------------------------------
    // Energy
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("canAfford / applyEnergyCost")
    class Energy {

        @Test void expandCosts2() {
            assertTrue(GameLogic.canAfford(2, ActionParser.ActionType.EXPAND));
            assertFalse(GameLogic.canAfford(1, ActionParser.ActionType.EXPAND));
            assertEquals(8, GameLogic.applyEnergyCost(10, ActionParser.ActionType.EXPAND));
        }

        @Test void attackCosts2() {
            assertTrue(GameLogic.canAfford(2, ActionParser.ActionType.ATTACK));
            assertEquals(3, GameLogic.applyEnergyCost(5, ActionParser.ActionType.ATTACK));
        }

        @Test void autophagyReturns1() {
            assertTrue(GameLogic.canAfford(0, ActionParser.ActionType.AUTOPHAGY));
            assertEquals(6, GameLogic.applyEnergyCost(5, ActionParser.ActionType.AUTOPHAGY));
        }

        @Test void waitFree() {
            assertEquals(5, GameLogic.applyEnergyCost(5, ActionParser.ActionType.WAIT));
        }
    }

    // -----------------------------------------------------------------------
    // Grid helpers
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("isAdjacent / isInBounds")
    class Grid {

        @Test void orthogonal() {
            assertTrue(GameLogic.isAdjacent(5, 5, 5, 6));
            assertTrue(GameLogic.isAdjacent(5, 5, 6, 5));
        }

        @Test void diagonal() {
            assertTrue(GameLogic.isAdjacent(5, 5, 6, 6));
            assertTrue(GameLogic.isAdjacent(5, 5, 4, 4));
        }

        @Test void notSelf() {
            assertFalse(GameLogic.isAdjacent(3, 3, 3, 3));
        }

        @Test void notAdjacent() {
            assertFalse(GameLogic.isAdjacent(0, 0, 2, 0));
        }

        @Test void bounds() {
            int s = GameConfig.BOARD_SIZE;
            assertTrue(GameLogic.isInBounds(0, 0, s));
            assertTrue(GameLogic.isInBounds(s - 1, s - 1, s));
            assertFalse(GameLogic.isInBounds(s, 0, s));
            assertFalse(GameLogic.isInBounds(-1, 0, s));
        }
    }

    // -----------------------------------------------------------------------
    // Map generation
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("generateMap")
    class MapGeneration {

        @Test void symmetricSpots() {
            Board b = new Board(GameConfig.BOARD_SIZE);
            GameLogic.generateMap(b, new Random(42), GameConfig.BOARD_SIZE);
            assertTrue(b.spots.size() >= 4); // at least 2 mirrored pairs
            assertTrue(b.spots.size() % 2 == 0);
        }

        @Test void spotsInBounds() {
            Board b = new Board(GameConfig.BOARD_SIZE);
            GameLogic.generateMap(b, new Random(123), GameConfig.BOARD_SIZE);
            for (Board.NutrientSpot s : b.spots) {
                assertTrue(s.x >= 0 && s.x < 64);
                assertTrue(s.y >= 0 && s.y < 64);
            }
        }

        @Test void noOverlappingSpots() {
            Board b = new Board(GameConfig.BOARD_SIZE);
            GameLogic.generateMap(b, new Random(7), GameConfig.BOARD_SIZE);
            long unique = b.spots.stream()
                .map(s -> s.x + "," + s.y)
                .distinct().count();
            assertEquals(b.spots.size(), unique);
        }

        @Test void playersPlaced() {
            int s = GameConfig.BOARD_SIZE;
            Board b = new Board(GameConfig.BOARD_SIZE);
            GameLogic.generateMap(b, new Random(1), GameConfig.BOARD_SIZE);
            assertTrue(b.belongsTo(0, 0, 0));
            assertTrue(b.belongsTo(1, s - 1, s - 1));
        }

        @Test void startingEnergyReasonable() {
            Board b = new Board(GameConfig.BOARD_SIZE);
            GameLogic.generateMap(b, new Random(1), GameConfig.BOARD_SIZE);
            int e = GameLogic.computeStartingEnergy(b, 0, 0);
            assertTrue(e >= 10 && e <= 60, "starting energy should be 10-60, got " + e);
        }
    }

    // -----------------------------------------------------------------------
    // Fog of War
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Fog of War")
    class FogOfWar {

        private Board b;

        @BeforeEach void setup() {
            b = new Board(GameConfig.BOARD_SIZE);
            b.placeCell(0, 5, 5);
            b.placeCell(1, 10, 10);
            b.spots.add(new Board.NutrientSpot(2, 2, Board.SpotType.SMALL)); // p0 half
            b.spots.add(new Board.NutrientSpot(13, 13, Board.SpotType.SMALL)); // p1 half
        }

        @Test void ownCellAlwaysVisible() {
            VisibleState vs = GameLogic.getVisibleEntities(b, 0);
            assertEquals(1, vs.myCells.size());
            assertEquals(5, vs.myCells.get(0).x);
        }

        @Test void enemyAtDistance3Visible() {
            b.placeCell(1, 8, 5); // Chebyshev distance 3 from (5,5)
            VisibleState vs = GameLogic.getVisibleEntities(b, 0);
            assertEquals(1, vs.oppCells.size());
        }

        @Test void enemyAtDistance4Invisible() {
            b.placeCell(1, 9, 5); // distance 4 from (5,5)
            VisibleState vs = GameLogic.getVisibleEntities(b, 0);
            assertEquals(0, vs.oppCells.size());
        }

        @Test void ownHalfSpotsAlwaysVisible() {
            VisibleState vs = GameLogic.getVisibleEntities(b, 0);
            boolean seesOwnSpot = vs.visibleSpots.stream()
                .anyMatch(s -> s.x == 2 && s.y == 2);
            assertTrue(seesOwnSpot);
        }

        @Test void enemyHalfSpotInvisibleIfFar() {
            VisibleState vs = GameLogic.getVisibleEntities(b, 0);
            boolean seesEnemySpot = vs.visibleSpots.stream()
                .anyMatch(s -> s.x == 13 && s.y == 13);
            assertFalse(seesEnemySpot);
        }
    }

    // -----------------------------------------------------------------------
    // Expansion
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("resolveExpand")
    class Expansion {

        private Board b;

        @BeforeEach void setup() {
            b = new Board(GameConfig.BOARD_SIZE);
            b.placeCell(0, 5, 5);
        }

        @Test void validExpand() {
            assertTrue(GameLogic.resolveExpand(b, 0, 6, 5));
            assertTrue(b.belongsTo(0, 6, 5));
        }

        @Test void rejectOccupied() {
            b.placeCell(1, 6, 5);
            assertFalse(GameLogic.resolveExpand(b, 0, 6, 5));
        }

        @Test void rejectNotAdjacent() {
            assertFalse(GameLogic.resolveExpand(b, 0, 8, 8));
        }

        @Test void rejectOutOfBounds() {
            b.placeCell(0, 0, 0);
            assertFalse(GameLogic.resolveExpand(b, 0, -1, 0));
        }
    }

    // -----------------------------------------------------------------------
    // Combat
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("resolveAttack")
    class Combat {

        private Board b;

        @BeforeEach void setup() {
            b = new Board(GameConfig.BOARD_SIZE);
        }

        @Test void attackSucceedsWithMajority() {
            b.placeCell(0, 5, 5);
            b.placeCell(0, 5, 6);
            b.placeCell(0, 6, 6);
            b.placeCell(1, 6, 5); // enemy, surrounded by 3 p0 neighbors
            int reward = GameLogic.resolveAttack(b, 0, 6, 5);
            assertEquals(GameLogic.ATTACK_REWARD, reward);
            assertTrue(b.belongsTo(0, 6, 5));
        }

        @Test void attackFailsWithoutMajority() {
            b.placeCell(0, 5, 5);
            b.placeCell(1, 6, 5);
            b.placeCell(1, 6, 6); // defender has 1 neighbor, attacker has 1 neighbor
            int reward = GameLogic.resolveAttack(b, 0, 6, 5);
            assertEquals(0, reward);
            assertTrue(b.belongsTo(1, 6, 5)); // still enemy
        }

        @Test void attackOnEmptyCellFails() {
            b.placeCell(0, 5, 5);
            assertEquals(0, GameLogic.resolveAttack(b, 0, 6, 5));
        }

        @Test void destroyedEnemyCellsIncremented() {
            b.placeCell(0, 5, 5);
            b.placeCell(0, 5, 6);
            b.placeCell(1, 6, 5);
            GameLogic.resolveAttack(b, 0, 6, 5);
            assertEquals(1, b.destroyedEnemyCells[0]);
        }
    }

    // -----------------------------------------------------------------------
    // Autophagy
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("resolveAutophagy")
    class Autophagy {

        @Test void validAutophagy() {
            Board b = new Board(GameConfig.BOARD_SIZE);
            b.placeCell(0, 3, 3);
            assertTrue(GameLogic.resolveAutophagy(b, 0, 3, 3));
            assertTrue(b.isEmpty(3, 3));
        }

        @Test void rejectEmpty() {
            Board b = new Board(GameConfig.BOARD_SIZE);
            assertFalse(GameLogic.resolveAutophagy(b, 0, 3, 3));
        }

        @Test void rejectEnemyCell() {
            Board b = new Board(GameConfig.BOARD_SIZE);
            b.placeCell(1, 3, 3);
            assertFalse(GameLogic.resolveAutophagy(b, 0, 3, 3));
        }
    }

    // -----------------------------------------------------------------------
    // Passive extraction
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("passiveExtraction")
    class PassiveExtraction {

        @Test void cellOnSpotGainsEnergy() {
            Board b = new Board(GameConfig.BOARD_SIZE);
            b.placeCell(0, 5, 5);
            b.spots.add(new Board.NutrientSpot(5, 5, Board.SpotType.SMALL));
            b.energy[0] = 10;
            GameLogic.passiveExtraction(b);
            assertEquals(11, b.energy[0]);
            assertEquals(9, b.spots.get(0).remainingEnergy);
        }

        @Test void depletedSpotGivesNothing() {
            Board b = new Board(GameConfig.BOARD_SIZE);
            b.placeCell(0, 5, 5);
            Board.NutrientSpot s = new Board.NutrientSpot(5, 5, Board.SpotType.SMALL);
            s.remainingEnergy = 0;
            b.spots.add(s);
            b.energy[0] = 10;
            GameLogic.passiveExtraction(b);
            assertEquals(10, b.energy[0]);
        }

        @Test void emptyCellNoExtraction() {
            Board b = new Board(GameConfig.BOARD_SIZE);
            b.spots.add(new Board.NutrientSpot(5, 5, Board.SpotType.SMALL));
            b.energy[0] = 10;
            GameLogic.passiveExtraction(b);
            assertEquals(10, b.energy[0]);
        }
    }

    // -----------------------------------------------------------------------
    // Victory
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("checkGameOver")
    class Victory {

        @Test void knockoutPlayer1Wins() {
            Board b = new Board(GameConfig.BOARD_SIZE);
            b.placeCell(1, 10, 10);
            // player 0 has no cells
            assertEquals(1, GameLogic.checkGameOver(b, 50));
        }

        @Test void knockoutPlayer0Wins() {
            Board b = new Board(GameConfig.BOARD_SIZE);
            b.placeCell(0, 10, 10);
            assertEquals(0, GameLogic.checkGameOver(b, 50));
        }

        @Test void gameNotOverMidGame() {
            int s = GameConfig.BOARD_SIZE;
            Board b = new Board(GameConfig.BOARD_SIZE);
            b.placeCell(0, 0, 0);
            b.placeCell(1, s - 1, s - 1);
            assertEquals(-2, GameLogic.checkGameOver(b, 1));
        }

        @Test void turnLimitScoringWorks() {
            int s = GameConfig.BOARD_SIZE;
            Board b = new Board(GameConfig.BOARD_SIZE);
            b.placeCell(0, 0, 0);
            b.placeCell(0, 1, 0);
            b.placeCell(1, s - 1, s - 1);
            b.energy[0] = 10;
            b.energy[1] = 5;
            // p0: 2*100+10=210, p1: 1*100+5=105
            assertEquals(0, GameLogic.checkGameOver(b, GameConfig.MAX_TURNS));
        }

        @Test void tiebreakByCellsDestroyed() {
            int s = GameConfig.BOARD_SIZE;
            Board b = new Board(GameConfig.BOARD_SIZE);
            b.placeCell(0, 0, 0);
            b.placeCell(1, s - 1, s - 1);
            b.energy[0] = 0;
            b.energy[1] = 0;
            // equal score, p1 destroyed more
            b.destroyedEnemyCells[1] = 3;
            assertEquals(1, GameLogic.checkGameOver(b, GameConfig.MAX_TURNS));
        }
    }
}
