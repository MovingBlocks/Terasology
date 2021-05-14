// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.subsystem.lwjgl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.audio.AudioManager;
import org.terasology.engine.audio.StaticSound;
import org.terasology.engine.audio.StreamingSound;
import org.terasology.engine.audio.nullAudio.NullAudioManager;
import org.terasology.engine.audio.openAL.OpenALException;
import org.terasology.engine.audio.openAL.OpenALManager;
import org.terasology.engine.config.AudioConfig;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.GameEngine;
import org.terasology.engine.core.modes.GameState;
import org.terasology.gestalt.assets.module.ModuleAwareAssetTypeManager;

public class LwjglAudio extends BaseLwjglSubsystem {

    private static final Logger logger = LoggerFactory.getLogger(LwjglAudio.class);

    private AudioManager audioManager;

    @Override
    public String getName() {
        return "Audio";
    }

    @Override
    public void initialise(GameEngine engine, Context rootContext) {
        try {
            audioManager = new OpenALManager(rootContext.get(AudioConfig.class));
        } catch (OpenALException e) {
            logger.warn("Could not load OpenAL manager - sound is disabled", e);
            audioManager = new NullAudioManager();
        }
        rootContext.put(AudioManager.class, audioManager);
    }

    @Override
    public void registerCoreAssetTypes(ModuleAwareAssetTypeManager assetTypeManager) {
        assetTypeManager.createAssetType(StaticSound.class, audioManager.getStaticSoundFactory(), "sounds");
        assetTypeManager.createAssetType(StreamingSound.class, audioManager.getStreamingSoundFactory(), "music");
    }

    @Override
    public void postUpdate(GameState currentState, float delta) {
        audioManager.update(delta);
    }

    @Override
    public void shutdown() {
        if (audioManager != null) {
            audioManager.dispose();
        }
    }
}
