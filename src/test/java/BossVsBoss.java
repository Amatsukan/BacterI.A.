import com.codingame.gameengine.runner.MultiplayerGameRunner;

import java.nio.file.Path;
import java.nio.file.Paths;

public class BossVsBoss {

    public static void main(String[] args) {
        MultiplayerGameRunner runner = new MultiplayerGameRunner();

        Path bossScript = Paths.get("config", "Boss.py3").toAbsolutePath();
        String python = "C:\\Python313\\python.exe";
        String[] bossCmd = { python, bossScript.toString() };

        runner.addAgent(bossCmd, "Boss-1", null);
        runner.addAgent(bossCmd, "Boss-2", null);

        System.out.println("Boss vs Boss  —  http://localhost:8888");
        runner.start(8888);
    }
}
