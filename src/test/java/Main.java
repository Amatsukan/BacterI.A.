import com.codingame.gameengine.runner.MultiplayerGameRunner;

import java.nio.file.Paths;

/**
 * Ponto de entrada para simulações locais.
 * Usa o MultiplayerGameRunner do CodinGame SDK para lançar o Referee
 * contra dois WaitBots num servidor local e abrir o replay no browser.
 *
 * Execução:  mvn test-compile exec:java
 *            (ou via scripts\run-local.bat)
 */
public class Main {

    public static void main(String[] args) {
        MultiplayerGameRunner runner = new MultiplayerGameRunner();

        // Passa o classpath da JVM actual para os processos filhos,
        // assim o WaitBot encontra todas as classes compiladas.
        String javaExe = Paths.get(System.getProperty("java.home"), "bin", "java").toString();
        String classpath = System.getProperty("java.class.path");
        String[] botCmd = { javaExe, "-cp", classpath, "WaitBot" };

        runner.addAgent(botCmd, "WaitBot-1", null);
        runner.addAgent(botCmd, "WaitBot-2", null);

        // Abre http://localhost:8888 no browser com o replay
        System.out.println("A iniciar servidor local em http://localhost:8888 ...");
        runner.start(8888);
    }
}
