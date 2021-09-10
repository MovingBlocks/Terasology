// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
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
