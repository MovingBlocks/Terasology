# PC Facade

The _PC Facade_ is the front-facing layer for running _Terasology_ on the PC.

## Usage

Most users won't interact directly with the PC Facade, but instead use the [Terasology Launcher](https://github.com/MovingBlocks/TerasologyLauncher) to start the game.

When starting distributions of Terasology manually, users will interact with the command-line interface ([CLI](#cli)).

Developers will most often use the PC Facade via [Gradle](#gradle) or via a run configuration in their IDE.

### CLI

The PC Facade provides a command-line interface (CLI) for configuring Terasology.
See the usage help (`./Terasology --help`) for a full overview.
For implementing the CLI, we make use of the [PicoCLI](https://picocli.info/) framework for Java command-line applications.

```sh
./Terasology --help

Usage: terasology [-h] [--[no-]crash-report] [--create-last-game] [--headless] [--load-last-game] [--permissive-security] [--[no-]save-games] [--[no-]sound]
                  [--[no-]splash] [--homedir=<homeDir>] [--max-data-size=<size>] [--oom-score=<score>] [--override-default-config=<overrideConfigPath>]
                  [--server-port=<serverPort>]
                  
      --[no-]crash-report   Enable crash reporting
      --create-last-game    Recreates the world of the latest game with a new save file on startup
      -h, /h, -?, /?, -help, /help, --help
                            Show help
      --headless            Start headless (no graphics)
      --homedir=<homeDir>   Path to home directory
      --load-last-game      Load the latest game on startup
      --max-data-size=<size>
                            Set maximum process data size [Linux only]
      --oom-score=<score>   Adjust out-of-memory score [Linux only]
      --override-default-config=<overrideConfigPath>
                            Override default config
      --permissive-security
      --[no-]save-games     Enable new save games
      --server-port=<serverPort>
                            Change the server port
      --[no-]sound          Enable sound
      --[no-]splash         Enable splash screen

For details, see
 https://github.com/MovingBlocks/Terasology/wiki/Advanced-Options

Alternatively use our standalone Launcher from
 https://github.com/MovingBlocks/TerasologyLauncher/releases
```

### Gradle

The PC Facade provides a couple of Gradle tasks for starting the game on the PC.
These "Terasology run tasks" offer various default configurations, for instance, to enable debugging or start in _headless mode_.

``` 
Terasology run tasks
--------------------
debug - Run 'Terasology' to play the game as a standard PC application (in debug mode)
game - Run 'Terasology' to play the game as a standard PC application
permissiveNatives - Run 'Terasology' with security set to permissive and natives loading a second way (for KComputers)
profile - Run 'Terasology' to play the game as a standard PC application (with Java FlightRecorder profiling)
server - Starts a headless multiplayer server with data stored in [project-root]/terasology-server
```

To pass additional arguments to the PC Facade when running via Gradle, use the `--args` option (see the Gradle [Application Plugin](https://docs.gradle.org/6.9.1/userguide/application_plugin.html) documentation).
For instance, to print the usage help, run

```sh
gradlew game --args='--help'
```

## Architecture

The main entry point for the PC Facade is `Terasology#main`.
Starting from there, the game engine is initialized and started according to the user inputs.


<p align="center">
<!-- this image is an editable SVG created with Draw.io -->
<img src="./docs/pc-facade-overview.drawio.svg" alt="Architectural overview of the PC Facade entry point."/>
</p>

The execution flow is as follows:

1. A `TerasologyEngineBuilder` is instantiated to prepare the game engine.

1. Additional subsystems are collected via the builder. This creates and adds subsystems that are specific for running Terasology on a PC. The facade also chooses respective subsystem implementations depending on whether the game is run in _headless mode_. For instance, for a headless server it would choose `HeadlessGraphics` instead of `LwjglGraphics`.

1. The builder is used to create an instance of `TerasologyEngine` which is subsequently initialized (`TerasologyEngine#initialize`, not to be confused with `GameEngine#initializeRun`). This triggers initialization of the engine for subsystems and asset management. This does not start the game loop yet.

1. Depending on the arguments provided by the user, the facade creates an appropriate `GameState`
    - `HeadlessSetup` if the game is started in _headless mode_
    - `Loading` if a game should directly be loaded (either by loading a save game or by creating a new one)
    - `MainMenu` otherwise

1. This state is passed to `TerasologyEngine#run` to start the game loop with the given state.

 
