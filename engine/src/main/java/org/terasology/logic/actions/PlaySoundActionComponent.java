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
package org.terasology.logic.actions;

import com.google.common.collect.Lists;
import org.terasology.audio.StaticSound;
import org.terasology.entitySystem.Component;

import java.util.Arrays;
import java.util.List;

/**
 * When activated, plays a random sound
 *
 */
public class PlaySoundActionComponent implements Component {
    public List<StaticSound> sounds = Lists.newArrayList();
    public float volume = 1.0f;
    public ActionTarget relativeTo = ActionTarget.Instigator;

    public PlaySoundActionComponent() {
    }

    /**
     * Creates new instance of PlaySoundActionComponent
     * @param sounds provided sounds via vararg
     */
    public PlaySoundActionComponent(StaticSound... sounds) {
        this.sounds.addAll(Arrays.asList(sounds));
    }
}
