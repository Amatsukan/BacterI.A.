import com.codingame.gameengine.runner.MultiplayerGameRunner;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

public class BossVsBoss {

    /**
     * Command line for one Boss agent ({@code config/Boss.py3}).
     * Use {@code -Dpython.exe=/path/to/python} on Maven, or environment variable {@code PYTHON_EXE}.
     */
    public static String[] bossAgentCommand() {
        Path bossScript = Paths.get("config", "Boss.py3").toAbsolutePath();
        String python = resolvePythonExecutable();
        return new String[]{python, bossScript.toString()};
    }

    private static String resolvePythonExecutable() {
        String fromProp = System.getProperty("python.exe");
        if (fromProp != null && !fromProp.isBlank()) {
            return fromProp;
        }
        String env = System.getenv("PYTHON_EXE");
        if (env != null && !env.isBlank()) {
            return env;
        }
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (os.contains("win")) {
            return "python";
        }
        return "python3";
    }

    public static void main(String[] args) {
        MultiplayerGameRunner runner = new MultiplayerGameRunner();

        String[] bossCmd = bossAgentCommand();

        runner.addAgent(bossCmd, "Boss-1", null);
        runner.addAgent(bossCmd, "Boss-2", null);

        System.out.println("Boss vs Boss  —  http://localhost:8888");
        runner.start(8888);
    }
}
