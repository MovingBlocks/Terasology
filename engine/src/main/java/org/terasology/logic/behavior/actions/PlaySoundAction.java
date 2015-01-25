/*
 * Copyright 2015 MovingBlocks
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
package org.terasology.logic.behavior.actions;

import org.terasology.asset.AssetManager;
import org.terasology.asset.AssetUri;
import org.terasology.audio.AudioEndListener;
import org.terasology.audio.AudioManager;
import org.terasology.audio.StaticSound;
import org.terasology.logic.behavior.ActionName;
import org.terasology.logic.behavior.core.Actor;
import org.terasology.logic.behavior.core.BaseAction;
import org.terasology.logic.behavior.core.BehaviorState;
import org.terasology.math.geom.Vector3f;
import org.terasology.module.sandbox.API;
import org.terasology.registry.In;
import org.terasology.rendering.nui.properties.OneOf;
import org.terasology.rendering.nui.properties.Range;

/**
 * Plays a sound. Return SUCCESS when sound ends.
 */
@API
@ActionName("sound")
public class PlaySoundAction extends BaseAction {
    @OneOf.Provider(name = "sounds")
    private AssetUri sound = new AssetUri();
    @Range(min = 0, max = 1)
    private float volume;
    @In
    private transient AudioManager audioManager;
    @In
    private transient AssetManager assetManager;

    @Override
    public void construct(Actor actor) {
        if (sound != null) {
            StaticSound snd = assetManager.loadAsset(sound, StaticSound.class);
            if (snd != null) {
                if (actor.hasLocation()) {
                    Vector3f worldPosition = actor.location().getWorldPosition();
                    audioManager.playSound(snd, worldPosition, volume, AudioManager.PRIORITY_NORMAL, createEndListener(actor));
                } else {
                    audioManager.playSound(snd, new Vector3f(), volume, AudioManager.PRIORITY_NORMAL, createEndListener(actor));
                }
                actor.setValue(getId(), true);
            }
        }
    }

    @Override
    public BehaviorState modify(Actor actor, BehaviorState result) {
        Boolean playing = actor.getValue(getId());
        if (playing == null || !playing) {
            return BehaviorState.SUCCESS;
        }
        return BehaviorState.RUNNING;
    }

    private AudioEndListener createEndListener(final Actor actor) {
        return new AudioEndListener() {
            @Override
            public void onAudioEnd() {
                actor.setValue(getId(), false);
            }
        };
    }

}
