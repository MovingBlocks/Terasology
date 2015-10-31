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
import org.terasology.audio.AudioManager;
import org.terasology.audio.StreamingSound;
import org.terasology.registry.In;
import org.terasology.rendering.nui.properties.OneOf;

import java.util.Optional;

/**
 * <b>Properties</b>: <b>music</b><br>
 * <br>
 * <b>RUNNING</b>: while music is playing<br>
 * <b>SUCCESS</b>: once music ends playing<br>
 * <b>FAILURE</b>: otherwise<br>
 * <br>
 * Auto generated javadoc - modify README.markdown instead!
 */
public class PlayMusicNode extends Node {
    @OneOf.Provider(name = "music")
    private ResourceUrn music;

    @Override
    public Task createTask() {
        return new PlayMusicTask(this);
    }

    private static class PlayMusicTask extends Task implements AudioEndListener {
        @In
        private AudioManager audioManager;
        @In
        private AssetManager assetManager;
        private boolean playing;
        private boolean finished;

        public PlayMusicTask(PlayMusicNode node) {
            super(node);
        }

        @Override
        public void onInitialize() {
            ResourceUrn uri = getNode().music;
            if (uri != null) {
                Optional<StreamingSound> asset = assetManager.getAsset(uri, StreamingSound.class);
                if (asset.isPresent()) {
                    audioManager.playMusic(asset.get(), this);
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
        public PlayMusicNode getNode() {
            return (PlayMusicNode) super.getNode();
        }
    }
}
