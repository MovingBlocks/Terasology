/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.engine;

import com.google.common.collect.Lists;
import org.terasology.crashreporter.CrashReporter;
import org.terasology.engine.modes.StateLoading;
import org.terasology.engine.modes.StateMainMenu;
import org.terasology.engine.paths.PathManager;
import org.terasology.engine.subsystem.EngineSubsystem;
import org.terasology.engine.subsystem.headless.HeadlessAudio;
import org.terasology.engine.subsystem.headless.HeadlessGraphics;
import org.terasology.engine.subsystem.headless.HeadlessInput;
import org.terasology.engine.subsystem.headless.HeadlessTimer;
import org.terasology.engine.subsystem.headless.mode.HeadlessStateChangeListener;
import org.terasology.engine.subsystem.headless.mode.StateHeadlessSetup;
import org.terasology.engine.subsystem.lwjgl.LwjglAudio;
import org.terasology.engine.subsystem.lwjgl.LwjglGraphics;
import org.terasology.engine.subsystem.lwjgl.LwjglInput;
import org.terasology.engine.subsystem.lwjgl.LwjglTimer;
import org.terasology.game.GameManifest;
import org.terasology.network.NetworkMode;
import org.terasology.rendering.nui.layers.mainMenu.savedGames.GameInfo;
import org.terasology.rendering.nui.layers.mainMenu.savedGames.GameProvider;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

/**
 * Main method for launching Terasology
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 * @author Kireev   Anton   <adeon.k87@gmail.com>
 */
public final class Terasology {

    private static final String LOAD_LAST_GAME_ARG = "-loadlastgame";
    private static final String HOME_ARG = "-homedir=";
    private static final String LOCAL_ARG = "-homedir";
    private static final String HEADLESS_ARG = "-headless";
    private static final String NO_CRASH_REPORT_ARG = "-noCrashReport";

    private Terasology() {
    }

    public static void main(String[] args) {
        boolean crashReportEnabled = true;
        boolean loadLastGame = false;
        try {
            boolean isHeadless = false;
            Path homePath = null;
            for (String arg : args) {
                if (arg.startsWith(HOME_ARG)) {
                    homePath = Paths.get(arg.substring(HOME_ARG.length()));
                } else if (arg.equals(LOCAL_ARG)) {
                    homePath = Paths.get("");
                } else if (arg.equals(HEADLESS_ARG)) {
                    isHeadless = true;
                    crashReportEnabled = false;
                } else if (arg.equals(NO_CRASH_REPORT_ARG)) {
                    crashReportEnabled = false;
                } else if (arg.equals(LOAD_LAST_GAME_ARG)) {
                    loadLastGame = true;
                }
            }
            if (homePath != null) {
                PathManager.getInstance().useOverrideHomePath(homePath);
            } else {
                PathManager.getInstance().useDefaultHomePath();
            }

            Collection<EngineSubsystem> subsystemList;
            if (isHeadless) {
                subsystemList = Lists.newArrayList(new HeadlessGraphics(), new HeadlessTimer(), new HeadlessAudio(), new HeadlessInput());
            } else {
                subsystemList = Lists.<EngineSubsystem>newArrayList(new LwjglGraphics(), new LwjglTimer(), new LwjglAudio(), new LwjglInput());
            }

            final TerasologyEngine engine = new TerasologyEngine(subsystemList);
            try {
                engine.init();
                if (isHeadless) {
                    engine.subscribeToStateChange(new HeadlessStateChangeListener());
                    engine.run(new StateHeadlessSetup());
                } else {
                    if (loadLastGame) {
                        engine.submitTask("loadGame", new Runnable() {
                            @Override
                            public void run() {
                                GameManifest gameManifest = getLatestGameManifest();
                                if (gameManifest != null) {
                                    engine.changeState(new StateLoading(gameManifest, NetworkMode.NONE));
                                }
                            }
                        });
                    }

                    engine.run(new StateMainMenu());
                }
            } finally {
                engine.close();
            }
        } catch (RuntimeException | IOException e) {

            if (!GraphicsEnvironment.isHeadless()) {
                Path logPath = Paths.get(".");
                try {
                    Path gameLogPath = PathManager.getInstance().getLogPath();
                    if (gameLogPath != null) {
                        logPath = gameLogPath;
                    }
                } catch (Exception eat) {
                    // eat silently
                }

                if (crashReportEnabled) {
                    Path logFile = logPath.resolve("Terasology.log");

                    CrashReporter.report(e, logFile);
                }
            }
        }
    }

    private static GameManifest getLatestGameManifest() {
        GameInfo latestGame = null;
        List<GameInfo> savedGames = GameProvider.getSavedGames();
        for (GameInfo savedGame : savedGames) {
            if (latestGame == null || savedGame.getTimestamp().after(latestGame.getTimestamp())) {
                latestGame = savedGame;
            }
        }

        if (latestGame == null) {
            return null;
        }

        return latestGame.getManifest();
    }

}
