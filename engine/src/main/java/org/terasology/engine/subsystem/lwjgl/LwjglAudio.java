// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.subsystem.lwjgl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.module.ModuleAwareAssetTypeManager;
import org.terasology.audio.AudioManager;
import org.terasology.audio.StaticSound;
import org.terasology.audio.StreamingSound;
import org.terasology.audio.nullAudio.NullAudioManager;
import org.terasology.audio.openAL.OpenALManager;
import org.terasology.context.Context;
import org.terasology.engine.GameEngine;
import org.terasology.engine.modes.GameState;
import org.terasology.registry.ContextAwareClassFactory;
import org.terasology.registry.In;

public class LwjglAudio extends BaseLwjglSubsystem {

    private static final Logger logger = LoggerFactory.getLogger(LwjglAudio.class);

    @In
    private ContextAwareClassFactory classFactory;

    private AudioManager audioManager;

    @Override
    public String getName() {
        return "Audio";
    }

    @Override
    public void initialise(GameEngine engine, Context rootContext) {
        try {
            audioManager = classFactory.createInjectableInstance(AudioManager.class, OpenALManager.class);
        } catch (Exception e) {
            logger.warn("Could not load OpenAL manager - sound is disabled", e);
            audioManager = classFactory.createInjectableInstance(AudioManager.class, NullAudioManager.class);
        }
    }

    @Override
    public void registerCoreAssetTypes(ModuleAwareAssetTypeManager assetTypeManager) {
        assetTypeManager.registerCoreAssetType(StaticSound.class, audioManager.getStaticSoundFactory(), "sounds");
        assetTypeManager.registerCoreAssetType(StreamingSound.class, audioManager.getStreamingSoundFactory(), "music");
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
