/*
 * Copyright 2018 MovingBlocks
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
package org.terasology;

import com.google.api.client.util.Lists;
import org.terasology.config.Config;
import org.terasology.engine.TerasologyEngine;
import org.terasology.engine.TerasologyEngineBuilder;
import org.terasology.engine.modes.StateLoading;
import org.terasology.engine.modes.StateMainMenu;
import org.terasology.engine.paths.PathManager;
import org.terasology.engine.subsystem.common.hibernation.HibernationSubsystem;
import org.terasology.engine.subsystem.config.BindsSubsystem;
import org.terasology.engine.subsystem.lwjgl.LwjglAudio;
import org.terasology.engine.subsystem.lwjgl.LwjglGraphics;
import org.terasology.engine.subsystem.lwjgl.LwjglInput;
import org.terasology.engine.subsystem.lwjgl.LwjglTimer;
import org.terasology.engine.subsystem.openvr.OpenVRInput;
import org.terasology.game.GameManifest;
import org.terasology.network.NetworkMode;
import org.terasology.recording.RecordAndReplayStatus;
import org.terasology.recording.RecordAndReplayUtils;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.nui.layers.mainMenu.savedGames.GameInfo;
import org.terasology.rendering.nui.layers.mainMenu.savedGames.GameProvider;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * A base class for replay tests to inherit to run a replay in a thread while testing some variables.
 */
public abstract class ReplayTestingEnvironment {
    private TerasologyEngine host;
    private List<TerasologyEngine> engines = Lists.newArrayList();

    /**
     * Opens the game in the Main Menu.
     * @throws Exception
     */
    protected void openMainMenu() throws Exception {
        host = createEngine();
        host.run(new StateMainMenu());
    }

    /**
     * Opens the game in a replay.
     * @param replayTitle the name of the replay to be played when the game opens.
     * @throws Exception
     */
    protected void openReplay(String replayTitle) throws Exception {
        host = createEngine();
        host.initialize();
        host.changeState(new StateMainMenu());
        host.tick();
        loadReplay(replayTitle);
        mainLoop();
        host.cleanup();
        engines = Lists.newArrayList();
        host = null;
    }

    /**
     * Load a replay while setting the RecordAndReplayStatus.
     * @param replayTitle the name of the replay to be loaded.
     * @throws Exception
     */
    private void loadReplay(String replayTitle) throws Exception {
        RecordAndReplayStatus.setCurrentStatus(RecordAndReplayStatus.PREPARING_REPLAY);
        GameInfo replayInfo = getReplayInfo(replayTitle);
        GameManifest manifest = replayInfo.getManifest();
        CoreRegistry.get(RecordAndReplayUtils.class).setGameTitle(manifest.getTitle());
        Config config = CoreRegistry.get(Config.class);
        config.getWorldGeneration().setDefaultSeed(manifest.getSeed());
        config.getWorldGeneration().setWorldTitle(manifest.getTitle());
        host.changeState(new StateLoading(manifest, NetworkMode.NONE));
    }

    /**
     * The game Main Loop where most of the processing time will be spent on.
     */
    private void mainLoop() {
        while (host.tick()) {
            //do nothing;
        }
    }

    /**
     * Creates a headed TerasologyEngine.
     * @return the created engine
     * @throws Exception
     */
    private TerasologyEngine createEngine() throws Exception {
        TerasologyEngineBuilder builder = new TerasologyEngineBuilder();
        populateSubsystems(builder);
        Path homePath = Paths.get("");
        PathManager.getInstance().useOverrideHomePath(homePath);
        TerasologyEngine engine = builder.build();
        engines.add(engine);
        return engine;
    }

    /**
     * Populates the engine builder with headed subsystems.
     * @param builder the builder to be populated.
     */
    private void populateSubsystems(TerasologyEngineBuilder builder) {
        builder.add(new LwjglAudio())
                .add(new LwjglGraphics())
                .add(new LwjglTimer())
                .add(new LwjglInput())
                .add(new BindsSubsystem())
                .add(new OpenVRInput());

        builder.add(new HibernationSubsystem());
    }

    /**
     * Searches the 'recordings' folder for a selected recording and returns its GameInfo if found.
     * @param title the title of the recording.
     * @return the GameInfo of the selected recording.
     * @throws Exception
     */
    private GameInfo getReplayInfo(String title) throws Exception {
        List<GameInfo> recordingsInfo = GameProvider.getSavedRecordings();
        for (GameInfo info : recordingsInfo) {
            if (title.equals(info.getManifest().getTitle())) {
                return info;
            }
        }
        throw new Exception("No replay found with this title: " + title);
    }

    protected TerasologyEngine getHost() {
        return this.host;
    }



}
