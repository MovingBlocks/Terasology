// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.subsystem.headless;

import org.terasology.context.Lifetime;
import org.terasology.engine.audio.AudioManager;
import org.terasology.engine.audio.StaticSound;
import org.terasology.engine.audio.StreamingSound;
import org.terasology.engine.audio.nullAudio.NullAudioManager;
import org.terasology.engine.core.GameEngine;
import org.terasology.engine.core.modes.GameState;
import org.terasology.engine.core.subsystem.EngineSubsystem;
import org.terasology.gestalt.assets.module.ModuleAwareAssetTypeManager;
import org.terasology.gestalt.di.ServiceRegistry;

import javax.inject.Inject;

public class HeadlessAudio implements EngineSubsystem {

    private AudioManager audioManager;

    @Inject
    public HeadlessAudio() {
    }

    @Override
    public String getName() {
        return "Audio";
    }

    @Override
    public void initialise(GameEngine engine, ServiceRegistry serviceRegistry) {
        initNoSound(serviceRegistry);
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
        audioManager.dispose();
    }

    private void initNoSound(ServiceRegistry serviceRegistry) {
        audioManager = new NullAudioManager();
        serviceRegistry.with(AudioManager.class).lifetime(Lifetime.Singleton).use(() -> audioManager);
    }

}
