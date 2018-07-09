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
import org.terasology.recording.RecordAndReplayUtils;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.nui.layers.mainMenu.savedGames.GameInfo;
import org.terasology.rendering.nui.layers.mainMenu.savedGames.GameProvider;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public abstract class ReplayTestingEnvironment {
    private TerasologyEngine host;
    private List<TerasologyEngine> engines = Lists.newArrayList();


    protected void setup() throws Exception {
        host = createEngine();
        host.run(new StateMainMenu());

    }

    protected TerasologyEngine createEngine() throws Exception {
        TerasologyEngineBuilder builder = new TerasologyEngineBuilder();
        populateSubsystems(builder);
        Path homePath = Paths.get("");
        PathManager.getInstance().useOverrideHomePath(homePath);
        TerasologyEngine engine = builder.build();
        engines.add(engine);
        return engine;
    }

    private void populateSubsystems(TerasologyEngineBuilder builder) {
        builder.add(new LwjglAudio())
                .add(new LwjglGraphics())
                .add(new LwjglTimer())
                .add(new LwjglInput())
                .add(new BindsSubsystem())
                .add(new OpenVRInput());

        builder.add(new HibernationSubsystem());
    }

    protected TerasologyEngine getHost() {
        return host;
    }

    protected void openReplay(String title) throws Exception {
        GameInfo replayInfo = getReplayInfo(title);
        loadReplay(replayInfo);

    }

    protected GameInfo getReplayInfo(String title) throws Exception {
        List<GameInfo> recordingsInfo = GameProvider.getSavedRecordings();
        for(GameInfo info : recordingsInfo) {
            if (title.equals(info.getManifest().getTitle())) {
                return info;
            }
        }
        throw new Exception("No replay found with this title: " + title);
    }

    private void loadReplay(GameInfo replayInfo) {
        GameManifest manifest = replayInfo.getManifest();
        CoreRegistry.get(RecordAndReplayUtils.class).setGameTitle(manifest.getTitle());
        Config config = CoreRegistry.get(Config.class);
        config.getWorldGeneration().setDefaultSeed(manifest.getSeed());
        config.getWorldGeneration().setWorldTitle(manifest.getTitle());
        host.changeState(new StateLoading(manifest, NetworkMode.NONE));
    }



}
