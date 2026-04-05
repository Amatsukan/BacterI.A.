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
            List<GameLogic.Action> a = GameLogic.parseActions("WAIT");
            assertEquals(1, a.size());
            assertEquals(GameLogic.ActionType.WAIT, a.get(0).type);
        }

        @Test void expandWithCoords() {
            GameLogic.Action a = GameLogic.parseActions("EXPAND 10 15").get(0);
            assertEquals(GameLogic.ActionType.EXPAND, a.type);
            assertEquals(10, a.x);
            assertEquals(15, a.y);
        }

        @Test void multipleActions() {
            List<GameLogic.Action> a = GameLogic.parseActions("EXPAND 1 2; ATTACK 3 4; WAIT");
            assertEquals(3, a.size());
            assertEquals(GameLogic.ActionType.EXPAND, a.get(0).type);
            assertEquals(GameLogic.ActionType.ATTACK, a.get(1).type);
            assertEquals(GameLogic.ActionType.WAIT, a.get(2).type);
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
            assertTrue(GameLogic.canAfford(2, GameLogic.ActionType.EXPAND));
            assertFalse(GameLogic.canAfford(1, GameLogic.ActionType.EXPAND));
            assertEquals(8, GameLogic.applyEnergyCost(10, GameLogic.ActionType.EXPAND));
        }

        @Test void attackCosts2() {
            assertTrue(GameLogic.canAfford(2, GameLogic.ActionType.ATTACK));
            assertEquals(3, GameLogic.applyEnergyCost(5, GameLogic.ActionType.ATTACK));
        }

        @Test void autophagyReturns1() {
            assertTrue(GameLogic.canAfford(0, GameLogic.ActionType.AUTOPHAGY));
            assertEquals(6, GameLogic.applyEnergyCost(5, GameLogic.ActionType.AUTOPHAGY));
        }

        @Test void waitFree() {
            assertEquals(5, GameLogic.applyEnergyCost(5, GameLogic.ActionType.WAIT));
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
            assertTrue(GameLogic.isInBounds(0, 0, 64));
            assertTrue(GameLogic.isInBounds(63, 63, 64));
            assertFalse(GameLogic.isInBounds(64, 0, 64));
            assertFalse(GameLogic.isInBounds(-1, 0, 64));
        }
    }

    // -----------------------------------------------------------------------
    // Map generation
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("generateMap")
    class MapGeneration {

        @Test void symmetricSpots() {
            Board b = new Board(64);
            GameLogic.generateMap(b, new Random(42), 64);
            assertTrue(b.spots.size() >= 16); // at least 8 pairs
            assertTrue(b.spots.size() % 2 == 0);
        }

        @Test void spotsInBounds() {
            Board b = new Board(64);
            GameLogic.generateMap(b, new Random(123), 64);
            for (Board.NutrientSpot s : b.spots) {
                assertTrue(s.x >= 0 && s.x < 64);
                assertTrue(s.y >= 0 && s.y < 64);
            }
        }

        @Test void noOverlappingSpots() {
            Board b = new Board(64);
            GameLogic.generateMap(b, new Random(7), 64);
            long unique = b.spots.stream()
                .map(s -> s.x + "," + s.y)
                .distinct().count();
            assertEquals(b.spots.size(), unique);
        }

        @Test void playersPlaced() {
            Board b = new Board(64);
            GameLogic.generateMap(b, new Random(1), 64);
            assertTrue(b.belongsTo(0, 0, 0));
            assertTrue(b.belongsTo(1, 63, 63));
        }

        @Test void startingEnergyReasonable() {
            Board b = new Board(64);
            GameLogic.generateMap(b, new Random(1), 64);
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
            b = new Board(64);
            b.placeCell(0, 10, 10);
            b.placeCell(1, 50, 50);
            b.spots.add(new Board.NutrientSpot(5, 5, Board.SpotType.SMALL)); // p0 half
            b.spots.add(new Board.NutrientSpot(55, 55, Board.SpotType.SMALL)); // p1 half
        }

        @Test void ownCellAlwaysVisible() {
            GameLogic.VisibleState vs = GameLogic.getVisibleEntities(b, 0);
            assertEquals(1, vs.myCells.size());
            assertEquals(10, vs.myCells.get(0).x);
        }

        @Test void enemyAtDistance3Visible() {
            b.placeCell(1, 13, 10); // distance 3 from (10,10)
            GameLogic.VisibleState vs = GameLogic.getVisibleEntities(b, 0);
            assertEquals(1, vs.oppCells.size());
        }

        @Test void enemyAtDistance4Invisible() {
            b.placeCell(1, 14, 10); // distance 4
            GameLogic.VisibleState vs = GameLogic.getVisibleEntities(b, 0);
            assertEquals(0, vs.oppCells.size());
        }

        @Test void ownHalfSpotsAlwaysVisible() {
            GameLogic.VisibleState vs = GameLogic.getVisibleEntities(b, 0);
            boolean seesOwnSpot = vs.visibleSpots.stream()
                .anyMatch(s -> s.x == 5 && s.y == 5);
            assertTrue(seesOwnSpot);
        }

        @Test void enemyHalfSpotInvisibleIfFar() {
            GameLogic.VisibleState vs = GameLogic.getVisibleEntities(b, 0);
            boolean seesEnemySpot = vs.visibleSpots.stream()
                .anyMatch(s -> s.x == 55 && s.y == 55);
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
            b = new Board(64);
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
            b = new Board(64);
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

        @Test void cellsDestroyedIncremented() {
            b.placeCell(0, 5, 5);
            b.placeCell(0, 5, 6);
            b.placeCell(1, 6, 5);
            GameLogic.resolveAttack(b, 0, 6, 5);
            assertEquals(1, b.cellsDestroyed[0]);
        }
    }

    // -----------------------------------------------------------------------
    // Autophagy
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("resolveAutophagy")
    class Autophagy {

        @Test void validAutophagy() {
            Board b = new Board(64);
            b.placeCell(0, 3, 3);
            assertTrue(GameLogic.resolveAutophagy(b, 0, 3, 3));
            assertTrue(b.isEmpty(3, 3));
        }

        @Test void rejectEmpty() {
            Board b = new Board(64);
            assertFalse(GameLogic.resolveAutophagy(b, 0, 3, 3));
        }

        @Test void rejectEnemyCell() {
            Board b = new Board(64);
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
            Board b = new Board(64);
            b.placeCell(0, 5, 5);
            b.spots.add(new Board.NutrientSpot(5, 5, Board.SpotType.SMALL));
            b.energy[0] = 10;
            GameLogic.passiveExtraction(b);
            assertEquals(11, b.energy[0]);
            assertEquals(9, b.spots.get(0).remainingEnergy);
        }

        @Test void depletedSpotGivesNothing() {
            Board b = new Board(64);
            b.placeCell(0, 5, 5);
            Board.NutrientSpot s = new Board.NutrientSpot(5, 5, Board.SpotType.SMALL);
            s.remainingEnergy = 0;
            b.spots.add(s);
            b.energy[0] = 10;
            GameLogic.passiveExtraction(b);
            assertEquals(10, b.energy[0]);
        }

        @Test void emptyCellNoExtraction() {
            Board b = new Board(64);
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
            Board b = new Board(64);
            b.placeCell(1, 10, 10);
            // player 0 has no cells
            assertEquals(1, GameLogic.checkGameOver(b, 50));
        }

        @Test void knockoutPlayer0Wins() {
            Board b = new Board(64);
            b.placeCell(0, 10, 10);
            assertEquals(0, GameLogic.checkGameOver(b, 50));
        }

        @Test void gameNotOverMidGame() {
            Board b = new Board(64);
            b.placeCell(0, 0, 0);
            b.placeCell(1, 63, 63);
            assertEquals(-2, GameLogic.checkGameOver(b, 50));
        }

        @Test void turnLimitScoringWorks() {
            Board b = new Board(64);
            b.placeCell(0, 0, 0);
            b.placeCell(0, 1, 0);
            b.placeCell(1, 63, 63);
            b.energy[0] = 10;
            b.energy[1] = 5;
            // p0: 2*100+10=210, p1: 1*100+5=105
            assertEquals(0, GameLogic.checkGameOver(b, 500));
        }

        @Test void tiebreakByCellsDestroyed() {
            Board b = new Board(64);
            b.placeCell(0, 0, 0);
            b.placeCell(1, 63, 63);
            b.energy[0] = 0;
            b.energy[1] = 0;
            // equal score, p1 destroyed more
            b.cellsDestroyed[1] = 3;
            assertEquals(1, GameLogic.checkGameOver(b, 500));
        }
    }
}
