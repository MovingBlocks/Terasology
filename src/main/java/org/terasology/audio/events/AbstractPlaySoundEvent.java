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
import org.terasology.entitySystem.AbstractEvent;
import org.terasology.entitySystem.EntityRef;

/**
 * @author Immortius
 */
public abstract class AbstractPlaySoundEvent extends AbstractEvent {
    private Sound sound;
    private float volume;
    private EntityRef instigator = EntityRef.NULL;

    protected AbstractPlaySoundEvent() {
    }

    public AbstractPlaySoundEvent(Sound sound, float volume) {
        this.sound = sound;
        this.volume = volume;
    }

    public AbstractPlaySoundEvent(EntityRef instigator, Sound sound, float volume) {
        this.instigator = instigator;
        this.sound = sound;
        this.volume = volume;
    }

    public Sound getSound() {
        return sound;
    }

    public float getVolume() {
        return volume;
    }

    public EntityRef getInstigator() {
        return instigator;
    }
}
