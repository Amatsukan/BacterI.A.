package com.codingame.game;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GameLogic")
class GameLogicTest {

    // -----------------------------------------------------------------------
    // parseActions
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("parseActions()")
    class ParseActions {

        @Test
        @DisplayName("WAIT único é válido")
        void singleWait() {
            List<GameLogic.Action> actions = GameLogic.parseActions("WAIT");
            assertEquals(1, actions.size());
            assertEquals(GameLogic.ActionType.WAIT, actions.get(0).type);
        }

        @Test
        @DisplayName("EXPAND com coordenadas")
        void expandWithCoords() {
            List<GameLogic.Action> actions = GameLogic.parseActions("EXPAND 10 15");
            assertEquals(1, actions.size());
            GameLogic.Action a = actions.get(0);
            assertEquals(GameLogic.ActionType.EXPAND, a.type);
            assertEquals(10, a.x);
            assertEquals(15, a.y);
        }

        @Test
        @DisplayName("múltiplas acções separadas por ';'")
        void multipleActions() {
            List<GameLogic.Action> actions = GameLogic.parseActions("EXPAND 1 2; ATTACK 3 4; WAIT");
            assertEquals(3, actions.size());
            assertEquals(GameLogic.ActionType.EXPAND,  actions.get(0).type);
            assertEquals(GameLogic.ActionType.ATTACK,  actions.get(1).type);
            assertEquals(GameLogic.ActionType.WAIT,    actions.get(2).type);
        }

        @Test
        @DisplayName("exactamente 5 acções — limite máximo permitido")
        void fiveActionsAllowed() {
            String line = "WAIT;WAIT;WAIT;WAIT;WAIT";
            assertDoesNotThrow(() -> GameLogic.parseActions(line));
            assertEquals(5, GameLogic.parseActions(line).size());
        }

        @Test
        @DisplayName("6 acções lança IllegalArgumentException")
        void sixActionsThrows() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> GameLogic.parseActions("WAIT;WAIT;WAIT;WAIT;WAIT;WAIT")
            );
            assertTrue(ex.getMessage().contains("Limite"));
        }

        @Test
        @DisplayName("comando inválido lança IllegalArgumentException")
        void unknownCommandThrows() {
            assertThrows(
                IllegalArgumentException.class,
                () -> GameLogic.parseActions("DANCE 1 1")
            );
        }

        @Test
        @DisplayName("EXPAND sem coordenadas lança IllegalArgumentException")
        void expandMissingCoordsThrows() {
            assertThrows(
                IllegalArgumentException.class,
                () -> GameLogic.parseActions("EXPAND")
            );
        }

        @Test
        @DisplayName("output vazio lança IllegalArgumentException")
        void emptyOutputThrows() {
            assertThrows(IllegalArgumentException.class, () -> GameLogic.parseActions(""));
            assertThrows(IllegalArgumentException.class, () -> GameLogic.parseActions("   "));
            assertThrows(IllegalArgumentException.class, () -> GameLogic.parseActions(null));
        }
    }

    // -----------------------------------------------------------------------
    // Energia
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("canAfford() / applyEnergyCost()")
    class Energy {

        @Test
        @DisplayName("EXPAND custa 2 de energia")
        void expandCosts2() {
            assertTrue(GameLogic.canAfford(2, GameLogic.ActionType.EXPAND));
            assertFalse(GameLogic.canAfford(1, GameLogic.ActionType.EXPAND));
            assertEquals(8, GameLogic.applyEnergyCost(10, GameLogic.ActionType.EXPAND));
        }

        @Test
        @DisplayName("ATTACK custa 2 de energia")
        void attackCosts2() {
            assertTrue(GameLogic.canAfford(2, GameLogic.ActionType.ATTACK));
            assertFalse(GameLogic.canAfford(0, GameLogic.ActionType.ATTACK));
            assertEquals(3, GameLogic.applyEnergyCost(5, GameLogic.ActionType.ATTACK));
        }

        @Test
        @DisplayName("AUTOPHAGY devolve +1 de energia")
        void autophagyReturns1() {
            assertTrue(GameLogic.canAfford(0, GameLogic.ActionType.AUTOPHAGY));
            assertEquals(6, GameLogic.applyEnergyCost(5, GameLogic.ActionType.AUTOPHAGY));
        }

        @Test
        @DisplayName("WAIT não altera energia")
        void waitFree() {
            assertTrue(GameLogic.canAfford(0, GameLogic.ActionType.WAIT));
            assertEquals(5, GameLogic.applyEnergyCost(5, GameLogic.ActionType.WAIT));
        }
    }

    // -----------------------------------------------------------------------
    // Adjacência / Limites
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("isAdjacent() / isInBounds()")
    class Grid {

        @Test
        @DisplayName("células ortogonalmente adjacentes")
        void orthogonalAdjacency() {
            assertTrue(GameLogic.isAdjacent(5, 5, 5, 6));
            assertTrue(GameLogic.isAdjacent(5, 5, 6, 5));
            assertTrue(GameLogic.isAdjacent(5, 5, 5, 4));
            assertTrue(GameLogic.isAdjacent(5, 5, 4, 5));
        }

        @Test
        @DisplayName("células diagonalmente adjacentes")
        void diagonalAdjacency() {
            assertTrue(GameLogic.isAdjacent(5, 5, 6, 6));
            assertTrue(GameLogic.isAdjacent(5, 5, 4, 4));
            assertTrue(GameLogic.isAdjacent(5, 5, 6, 4));
            assertTrue(GameLogic.isAdjacent(5, 5, 4, 6));
        }

        @Test
        @DisplayName("célula não é adjacente a si mesma")
        void notSelf() {
            assertFalse(GameLogic.isAdjacent(3, 3, 3, 3));
        }

        @Test
        @DisplayName("células distantes não são adjacentes")
        void notAdjacent() {
            assertFalse(GameLogic.isAdjacent(0, 0, 2, 0));
            assertFalse(GameLogic.isAdjacent(0, 0, 0, 5));
        }

        @Test
        @DisplayName("isInBounds numa grelha 64x64")
        void boundsCheck() {
            assertTrue(GameLogic.isInBounds(0, 0, 64));
            assertTrue(GameLogic.isInBounds(63, 63, 64));
            assertFalse(GameLogic.isInBounds(64, 0, 64));
            assertFalse(GameLogic.isInBounds(0, 64, 64));
            assertFalse(GameLogic.isInBounds(-1, 0, 64));
        }
    }
}
