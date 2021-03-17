/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.engine.audio;

/**
 * Is called by AudioManager to inform about end of playing music or sound.
 * The listener will only be called once. If the same track is played multiple times, a new listener has to be used each time.
 * <br><br>
 * Notice: The code in onAudioEnd is run in the update() method of the AudioManager once the sound/music ends playing.
 */
@FunctionalInterface
public interface AudioEndListener {

    /**
     * Called when the sound or music stops playing
     * @param interrupted true when the track was interrupted, e.g. via {@link AudioManager#stopAllSounds()}, false if not.
     */
    void onAudioEnd(boolean interrupted);
}
