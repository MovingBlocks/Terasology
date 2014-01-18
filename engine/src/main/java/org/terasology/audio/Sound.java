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
package org.terasology.audio;

import org.terasology.asset.Asset;
import org.terasology.asset.AssetData;

public interface Sound<T extends AssetData> extends Asset<T> {

    /**
     * @return channels amount of sound (1 - mono, 2 - stereo)
     */
    int getChannels();

    /**
     * @return sampling rate of sound (example 44100)
     */
    int getSamplingRate();

    /**
     * @return the size of the sound buffer
     */
    int getBufferSize();

    /**
     * Plays the sound at full volume.
     */
    void play();

    /**
     * Plays the sound at the given volume.
     * @param volume
     */
    void play(float volume);

}
