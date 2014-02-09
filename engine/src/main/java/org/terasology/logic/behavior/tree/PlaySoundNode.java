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

import org.terasology.asset.AssetManager;
import org.terasology.asset.AssetUri;
import org.terasology.audio.AudioManager;
import org.terasology.audio.Sound;
import org.terasology.registry.In;
import org.terasology.rendering.nui.properties.OneOf;
import org.terasology.rendering.nui.properties.Range;

/**
 * <b>Properties</b>: <b>sound</b>, <b>volume</b><br/>
 * <br/>
 * <b>SUCCESS</b>: when sound has started playing.<br/>
 * <b>FAILURE</b>: otherwise<br/>
 * <br/>
 * Auto generated javadoc - modify README.markdown instead!
 */
public class PlaySoundNode extends Node {
    @OneOf.Provider(name = "sounds")
    private AssetUri sound;
    @Range(min = 0, max = 1)
    private float volume;

    @Override
    public Task createTask() {
        return new PlaySoundTask(this);
    }

    public static class PlaySoundTask extends Task {
        @In
        private AudioManager audioManager;
        @In
        private AssetManager assetManager;
        private boolean playing;

        public PlaySoundTask(Node node) {
            super(node);
        }

        @Override
        public void onInitialize() {
            AssetUri uri = getNode().sound;
            if (uri != null) {
                Sound sound1 = assetManager.loadAsset(uri, Sound.class);
                if (sound1 != null) {
                    audioManager.playSound(sound1, getNode().volume);
                    playing = true;
                }
            }
        }

        @Override
        public Status update(float dt) {
            return playing ? Status.SUCCESS : Status.FAILURE;
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
