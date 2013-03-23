/*
 * Copyright 2013 Moving Blocks
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

public interface Sound extends Asset {

    /**
     * Returns sound sample length in seconds
     * Not available on streaming sounds (will return -1)
     *
     * @return
     */
    public int getLength();

    /**
     * Return channels amount of sound (1 - mono, 2 - stereo)
     *
     * @return
     */
    public int getChannels();

    /**
     * Returns sampling rate of sound (example 44100)
     *
     * @return
     */
    public int getSamplingRate();

    /**
     * Reset sound state (clears buffers, reset cached info)
     *
     * @return
     */
    public void reset();

    // TODO: Have these here?

    /**
     * @return the size of the sound buffer
     */
    public int getBufferSize();

    /**
     * @return the id of the sound buffer
     */
    public int getBufferId();

}
