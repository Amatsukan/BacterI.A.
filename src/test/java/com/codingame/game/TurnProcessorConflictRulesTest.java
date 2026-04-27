package com.codingame.game;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TurnProcessorConflictRulesTest {

    @Test
    void sameCellExpand_conflictResolvedDeterministically() {
        Board b = new Board(8);
        b.placeCell(0, 2, 2);
        b.placeCell(1, 4, 4);
        b.energy[0] = 10;
        b.energy[1] = 10;

        List<TurnProcessor.PlayerSubmission> submissions = new ArrayList<>();
        submissions.add(new TurnProcessor.PlayerSubmission(0, "P0", "EXPAND 3 3"));
        submissions.add(new TurnProcessor.PlayerSubmission(1, "P1", "EXPAND 3 3"));

        TurnProcessor.processTurn(b, 1, submissions, s -> {});
        assertTrue(b.belongsTo(0, 3, 3), "tie must resolve to player index order");
        assertEquals(8, b.energy[0]);
        assertEquals(8, b.energy[1]);
    }

    @Test
    void simultaneousAttack_resolvesFromPhaseSnapshot() {
        Board b = new Board(8);
        b.placeCell(0, 2, 2);
        b.placeCell(0, 2, 3);
        b.placeCell(1, 5, 5);
        b.placeCell(1, 5, 4);
        b.placeCell(0, 4, 5);
        b.placeCell(1, 3, 2);
        b.energy[0] = 20;
        b.energy[1] = 20;

        List<TurnProcessor.PlayerSubmission> submissions = new ArrayList<>();
        submissions.add(new TurnProcessor.PlayerSubmission(0, "P0", "ATTACK 3 2"));
        submissions.add(new TurnProcessor.PlayerSubmission(1, "P1", "ATTACK 4 5"));

        TurnProcessor.processTurn(b, 1, submissions, s -> {});
        assertTrue(b.belongsTo(0, 3, 2));
        assertTrue(b.belongsTo(1, 4, 5));
    }
}
