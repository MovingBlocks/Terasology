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

package org.terasology.audio.events;

import org.terasology.audio.StaticSound;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.network.BroadcastEvent;

/**
 * @author Immortius
 */
@BroadcastEvent(skipInstigator = true)
public class PlaySoundEvent extends AbstractPlaySoundEvent {
    protected PlaySoundEvent() {
    }

    public PlaySoundEvent(StaticSound sound, float volume) {
        super(sound, volume);
    }

    /**
     * @param exceptOwner The sound is not sent to the owner of this entity.
     * @param sound
     * @param volume
     */
    public PlaySoundEvent(EntityRef exceptOwner, StaticSound sound, float volume) {
        super(exceptOwner, sound, volume);
    }

}
