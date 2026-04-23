import com.codingame.gameengine.runner.MultiplayerGameRunner;
import com.codingame.gameengine.runner.simulate.GameResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Headless game run (no browser). Catches referee deadlocks or pathological slowness
 * that would surface on CodinGame as "Referee failed to provide output in the allotted 10000ms".
 */
class RefereeSimulateSmokeTest {

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
        assertTrue(ms < 10_000, "simulate took " + ms + "ms (expected well under CG 10s referee budget)");
    }
}
