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
package org.terasology.logic.behavior.tree;

import org.terasology.assets.ResourceUrn;
import org.terasology.assets.management.AssetManager;
import org.terasology.audio.AudioEndListener;
import org.terasology.audio.StaticSound;
import org.terasology.audio.events.PlaySoundEvent;
import org.terasology.registry.In;
import org.terasology.rendering.nui.properties.OneOf;
import org.terasology.rendering.nui.properties.Range;

import java.util.Optional;

/**
 * <b>Properties</b>: <b>sound</b>, <b>volume</b><br>
 * <br>
 * <b>RUNNING</b>: while sound is playing<br>
 * <b>SUCCESS</b>: once sound ends playing<br>
 * <b>FAILURE</b>: otherwise<br>
 * <br>
 * Auto generated javadoc - modify README.markdown instead!
 */
public class PlaySoundNode extends Node {
    @OneOf.Provider(name = "sounds")
    private ResourceUrn sound;
    @Range(min = 0, max = 1)
    private float volume;

    @Override
    public Task createTask() {
        return new PlaySoundTask(this);
    }

    public static class PlaySoundTask extends Task implements AudioEndListener {
        @In
        private AssetManager assetManager;
        private boolean playing;
        private boolean finished;

        public PlaySoundTask(Node node) {
            super(node);
        }

        @Override
        public void onInitialize() {
            ResourceUrn uri = getNode().sound;
            if (uri != null) {
                Optional<StaticSound> snd = assetManager.getAsset(uri, StaticSound.class);
                if (snd.isPresent()) {
                    actor().getEntity().send(new PlaySoundEvent(snd.get(), getNode().volume));
                    playing = true;
                }
            }
        }

        @Override
        public void onAudioEnd() {
            if (playing) {
                playing = false;
                finished = true;
            }
        }

        @Override
        public Status update(float dt) {
            if (finished) {
                return Status.SUCCESS;
            }
            return playing ? Status.RUNNING : Status.FAILURE;
        }

        @Override
        public void handle(Status result) {

        }

        @Override
        public PlaySoundNode getNode() {
            return (PlaySoundNode) super.getNode();
        }
    }
}
