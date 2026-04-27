package com.codingame.game;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SpecSyncTest {

    @Test
    void statementAndStubDescribePhaseBasedResolution() throws IOException {
        String stub = new String(Files.readAllBytes(Paths.get("config", "stub.txt")));
        String statement = new String(Files.readAllBytes(Paths.get("config", "statement_en.html")));

        assertTrue(stub.contains("EXPAND -> ATTACK -> AUTOPHAGY -> WAIT"),
            "stub must document phase order");
        assertTrue(statement.contains("EXPAND</b> &rarr; <b>ATTACK</b> &rarr; <b>AUTOPHAGY</b> &rarr; <b>WAIT"),
            "statement must document phase order");
        assertTrue(statement.contains("Invalid action examples"),
            "statement must include explicit invalid action examples");
    }
}
