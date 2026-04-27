# BacterI.A.

BacterI.A. is a 1v1 multiplayer CodinGame-style contest. Two programs read the same text protocol, send one line of actions per turn, and the referee resolves both outputs through a deterministic multi-phase pipeline.

## What is in this repository

- Java referee and game rules (CodinGame gameengine 4.x)
- Graphic viewer for local replay
- Config: statement, stub, tests and validator JSON, reference Boss in Python
- Maven build: normal pom for development, pom-plugin.xml for packaging referee.jar like CodinGame CI

## Quick start

Run all unit tests:

```
mvn test
```

Build the shaded referee JAR the same way as CodinGame CI (uses pom-plugin.xml):

```
mvn -f pom-plugin.xml clean package
```

Local Boss vs Boss with browser replay (forked JVM):

```
mvn test-compile exec:exec@forked-local -Dexec.mainClass=BossVsBoss
```

On Windows, scripts in the scripts folder wrap these flows (run-tests.bat, run-local.bat, run-boss-vs-boss.bat).

## Python for Boss.py3

The reference boss needs Python 3. The executable is chosen in this order:

1. JVM property python.exe (example: add -Dpython.exe=C:\Python312\python.exe to your Maven command)
2. Environment variable PYTHON_EXE
3. Default: python on Windows, python3 on Linux and macOS

## Where to read the rules

- config/stub.txt is the machine contract: exact order of init and per-turn lines.
- config/statement_en.html is the human contract: costs, fog, scoring, and how actions are ordered.
- config/config.ini sets multiplayer with two players.

## Important folders

- src/main/java/com/codingame/game/ referee, board model, rules split into small classes, viewer
- src/test/java/ JUnit tests and local runners (WaitBot.java, BossVsBoss.java, Main.java)
- config/ everything the puzzle exports to players and the IDE

Main Java types you will touch when changing rules:

- Referee.java orchestrates I/O and delegates to the turn state machine.
- TurnProcessor.java is the explicit 6-stage turn pipeline.
- TurnProtocol.java only consumes immutable DTOs (`GameStateSnapshot`/`PlayerView`/`TurnInput`).
- GameConfig.java holds numeric constants (turn limit, costs, vision radius, map generation).
- ActionParser.java parses player output to typed actions.
- ExpandAction/AttackAction/AutophagyAction/WaitAction each implement validate/cost/execute.
- ActionResolver.java, EnergyService.java, FogOfWarService.java, VictoryChecker.java, MapGenerator.java, GridUtils.java hold the rest of the logic.

## Tests

- TurnInputProtocolTest checks that init and turn text matches the stub so bots do not block on read while the referee waits.
- GameLogicTest covers parsing, energy, map, fog, combat, and victory.
- ContestRulesRegressionTest covers a few contest promises (for example invalid expand still spends energy, seeded map).
- TurnProcessorConflictRulesTest covers simultaneous conflict rules.
- TurnProcessorDeterminismTest enforces replay determinism under fixed seeds.
- TurnProcessorFuzzTest performs random invalid-action fuzzing and invariant checks.
- MassSimulationBalanceTest runs large random-vs-random batches (set `-Dbacteria.mass.games=10000` for 10k).
- EnginePerformanceBaselineTest measures baseline throughput (set `-Dbacteria.perf.games=5000` for deeper runs).

Extra JSON cases for the CodinGame toolchain live under config/ (for example test_smoke.json and validator_turn_limit.json).

## CodinGame CI and Exporter

After the build, CodinGame runs com.codingame.gameengine.runner.Exporter. That class comes from the runner dependency. In pom-plugin.xml, runner must be a normal compile dependency, not test-only only, or the export step fails with ClassNotFoundException on Exporter. This repository is set up that way on purpose.

## Player output (short)

One line per turn, up to five actions, separated by semicolons:

```
EXPAND x y
ATTACK x y
AUTOPHAGY x y
WAIT
```

Both players submit one line, then the referee resolves phases in this order:
EXPAND -> ATTACK -> AUTOPHAGY -> WAIT. Invalid EXPAND or ATTACK still cost energy where the statement says so. Read config/statement_en.html before arguing with the referee.

## Credits

BacterI.A. contest implementation. If you fork the idea, keep stub, referee, and statement in sync so players can trust the spec.
