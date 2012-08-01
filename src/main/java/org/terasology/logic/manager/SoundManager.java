/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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
package org.terasology.logic.manager;

import org.terasology.asset.AssetUri;
import org.terasology.audio.Sound;
import org.terasology.audio.SoundPool;
import org.terasology.audio.SoundSource;

/**
 * @author Immortius <immortius@gmail.com>
 */
// TODO: Rename this to AudioManager, and AudioManager to AudioManagerAbstract or something
public interface SoundManager {
    float MAX_DISTANCE = 50.0f;
    int PRIORITY_LOCKED = Integer.MAX_VALUE;
    int PRIORITY_HIGHEST = 100;
    int PRIORITY_HIGH = 10;
    int PRIORITY_NORMAL = 5;
    int PRIORITY_LOW = 3;
    int PRIORITY_LOWEST = 1;

    /**
     * Initializes AudioManager
     */
    void initialize();

    /**
     * Update AudioManager sound sources
     * <p/>
     * Should be called in main game loop
     */
    void update();

    /**
     * Gracefully destroy audio subsystem
     */
    void destroy();

    SoundPool getSoundPool(String pool);

    SoundSource getSoundSource(String pool, AssetUri sound, int priority);

    SoundSource getSoundSource(String pool, Sound sound, int priority);

    void stopAllSounds();
}
