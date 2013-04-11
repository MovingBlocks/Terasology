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
package org.terasology.components;

import java.util.List;

import org.terasology.audio.Sound;
import org.terasology.entitySystem.Component;

import com.google.common.collect.Lists;

/**
 * @author Immortius <immortius@gmail.com>
 */
public final class CharacterSoundComponent implements Component {
	//TODO all of these could be just one string variable
    public List<Sound> footstepSounds = Lists.newArrayList();
    public List<Sound> damageSounds = Lists.newArrayList();
    public List<Sound> landingSounds = Lists.newArrayList();
    public List<Sound> jumpSounds = Lists.newArrayList();
    public List<Sound> deathSounds = Lists.newArrayList();
    public List<Sound> respawnSounds = Lists.newArrayList();

    public float footstepVolume = 1.0f;
    public float damageVolume = 1.0f;
    public float landingVolume = 1.0f;
    public float deathVolume = 1.0f;
    public float respawnVolume = 1.0f;
    public float swimmingVolume = 1.0f;
    public float diveVolume = 1.0f;

}
