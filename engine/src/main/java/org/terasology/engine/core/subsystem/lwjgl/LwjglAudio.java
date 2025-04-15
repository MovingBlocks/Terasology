// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.subsystem.lwjgl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.context.Lifetime;
import org.terasology.engine.audio.AudioManager;
import org.terasology.engine.audio.StaticSound;
import org.terasology.engine.audio.StreamingSound;
import org.terasology.engine.audio.nullAudio.NullAudioManager;
import org.terasology.engine.audio.openAL.OpenALException;
import org.terasology.engine.audio.openAL.OpenALManager;
import org.terasology.engine.config.AudioConfig;
import org.terasology.engine.core.GameEngine;
import org.terasology.engine.core.modes.GameState;
import org.terasology.gestalt.assets.module.ModuleAwareAssetTypeManager;
import org.terasology.gestalt.di.ServiceRegistry;

import javax.inject.Inject;

public class LwjglAudio extends BaseLwjglSubsystem {

    private static final Logger logger = LoggerFactory.getLogger(LwjglAudio.class);

    @Inject
    protected AudioConfig audioConfig;

    private AudioManager audioManager;

    @Inject
    public LwjglAudio() {
    }

    @Override
    public String getName() {
        return "Audio";
    }

    @Override
    public void initialise(GameEngine engine, ServiceRegistry serviceRegistry) {
        try {
            audioManager = new OpenALManager(audioConfig);
        } catch (OpenALException e) {
            logger.warn("Could not load OpenAL manager - sound is disabled", e);
            audioManager = new NullAudioManager();
        }
        serviceRegistry.with(AudioManager.class).lifetime(Lifetime.Singleton).use(() -> audioManager);
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
