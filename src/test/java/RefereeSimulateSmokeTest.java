import com.codingame.gameengine.runner.MultiplayerGameRunner;
import com.codingame.gameengine.runner.simulate.GameResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Headless game run (no browser). Catches referee deadlocks or pathological slowness
 * that would surface on CodinGame as "Referee failed to provide output in the allotted 10000ms".
 */
class RefereeSimulateSmokeTest {

    private static String summarizeErrors(Map<String, List<String>> errors) {
        if (errors == null || errors.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder(" errors=");
        errors.forEach((k, v) -> {
            sb.append('[').append(k).append("]=");
            if (v != null) {
                for (String s : v) {
                    if (s != null && !s.isBlank()) {
                        String t = s.length() > 800 ? s.substring(0, 800) + "…" : s;
                        sb.append(t.replace("\n", "\\n"));
                    }
                }
            }
            sb.append("; ");
        });
        return sb.toString();
    }

    @Test
    @Timeout(25)
    void twoWaitBots_simulateCompletesQuickly() {
        MultiplayerGameRunner runner = new MultiplayerGameRunner();
        runner.setSeed(42L);
        runner.addAgent(WaitBot.class);
        runner.addAgent(WaitBot.class);
        long t0 = System.currentTimeMillis();
        GameResult result = runner.simulate();
        long ms = System.currentTimeMillis() - t0;
        assertNotNull(result);
        assertNull(
            result.failCause,
            () -> "failCause=" + result.failCause + summarizeErrors(result.errors));
        assertTrue(ms < 10_000, "simulate took " + ms + "ms (expected well under CG 10s referee budget)");
    }
}
