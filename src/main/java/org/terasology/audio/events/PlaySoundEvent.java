/*
 * Copyright 2013 Benjamin Glatzel <benjamin.glatzel@me.com>
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

import org.terasology.audio.Sound;
import org.terasology.entitySystem.EntityRef;

/**
 * @author Immortius
 */
public class PlaySoundEvent extends AbstractPlaySoundEvent {
    protected PlaySoundEvent() {
    }

    public PlaySoundEvent(Sound sound, float volume) {
        super(sound, volume);
    }

    /**
     * @param exceptOwner The sound is not sent to the owner of this entity.
     * @param sound
     * @param volume
     */
    public PlaySoundEvent(EntityRef exceptOwner, Sound sound, float volume) {
        super(exceptOwner, sound, volume);
    }

}
