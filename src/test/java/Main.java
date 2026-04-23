import com.codingame.gameengine.runner.MultiplayerGameRunner;

/**
 * Ponto de entrada para simulações locais.
 * Dois Boss ({@code config/Boss.py3}) como P0 e P1.
 *
 * Execução:  mvn test-compile exec:java
 *            (recomendado com replay: mvn test-compile exec:exec@forked-local)
 *            ou scripts/run-local.bat
 *
 * Python: define {@code PYTHON_EXE} se não usar o default do {@link BossVsBoss}.
 */
public class Main {

    public static void main(String[] args) {
        MultiplayerGameRunner runner = new MultiplayerGameRunner();

        String[] bossCmd = BossVsBoss.bossAgentCommand();
        runner.addAgent(bossCmd, "Boss-P0", null);
        runner.addAgent(bossCmd, "Boss-P1", null);

        System.out.println("A iniciar servidor local (Boss vs Boss) em http://localhost:8888 ...");
        runner.start(8888);
    }
}
