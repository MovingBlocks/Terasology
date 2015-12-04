/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.engine.subsystem.headless;

import org.terasology.assets.module.ModuleAwareAssetTypeManager;
import org.terasology.audio.AudioManager;
import org.terasology.audio.StaticSound;
import org.terasology.audio.StreamingSound;
import org.terasology.audio.nullAudio.NullAudioManager;
import org.terasology.context.Context;
import org.terasology.engine.GameEngine;
import org.terasology.engine.modes.GameState;
import org.terasology.engine.subsystem.EngineSubsystem;

public class HeadlessAudio implements EngineSubsystem {

    private AudioManager audioManager;

    @Override
    public String getName() {
        return "Audio";
    }

    @Override
    public void initialise(GameEngine engine, Context context) {
        initNoSound(context);
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
        audioManager.dispose();
    }

    private void initNoSound(Context context) {
        audioManager = new NullAudioManager();
        context.put(AudioManager.class, audioManager);
    }

}
