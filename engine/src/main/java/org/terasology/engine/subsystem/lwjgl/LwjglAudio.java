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
package org.terasology.engine.subsystem.lwjgl;

import org.lwjgl.LWJGLException;
import org.lwjgl.openal.OpenALException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.AssetManager;
import org.terasology.asset.AssetType;
import org.terasology.audio.AudioManager;
import org.terasology.audio.nullAudio.NullAudioManager;
import org.terasology.audio.openAL.OpenALManager;
import org.terasology.config.Config;
import org.terasology.engine.ComponentSystemManager;
import org.terasology.engine.modes.GameState;
import org.terasology.registry.CoreRegistry;

public class LwjglAudio extends BaseLwjglSubsystem {

    private static final Logger logger = LoggerFactory.getLogger(LwjglAudio.class);
    
    private AudioManager audioManager;

    @Override
    public synchronized void preInitialise() {
        super.preInitialise();
    }

    @Override
    public void postInitialise(Config config) {
        initOpenAL(config);
    }

    @Override
    public void preUpdate(GameState currentState, float delta) {
    }

    @Override
    public void postUpdate(GameState currentState, float delta) {
        audioManager.update(delta);
    }

    @Override
    public void shutdown(Config config) {
    }

    @Override
    public void dispose() {
        audioManager.dispose();
    }

    private void initOpenAL(Config config) {
        if (config.getAudio().isDisableSound()) {
            audioManager = new NullAudioManager();
        } else {
            try {
                audioManager = new OpenALManager(config.getAudio());
            } catch (LWJGLException | OpenALException e) {
                logger.warn("Could not load OpenAL manager - sound is disabled", e);
                audioManager = new NullAudioManager();
            }
        }
        CoreRegistry.putPermanently(AudioManager.class, audioManager);
        AssetManager assetManager = CoreRegistry.get(AssetManager.class);
        assetManager.setAssetFactory(AssetType.SOUND, audioManager.getStaticSoundFactory());
        assetManager.setAssetFactory(AssetType.MUSIC, audioManager.getStreamingSoundFactory());
    }

    @Override
    public void registerSystems(ComponentSystemManager componentSystemManager) {
    }

}
