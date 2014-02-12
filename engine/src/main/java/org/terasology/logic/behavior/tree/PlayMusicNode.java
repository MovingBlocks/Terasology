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
import org.terasology.audio.AudioEndListener;
import org.terasology.audio.AudioManager;
import org.terasology.audio.Sound;
import org.terasology.registry.In;
import org.terasology.rendering.nui.properties.OneOf;

/**
 * <b>Properties</b>: <b>music</b><br/>
 * <br/>
 * <b>RUNNING</b>: while music is playing<br/>
 * <b>SUCCESS</b>: once music ends playing<br/>
 * <b>FAILURE</b>: otherwise<br/>
 * <br/>
 * Auto generated javadoc - modify README.markdown instead!
 */
public class PlayMusicNode extends Node {
    @OneOf.Provider(name = "music")
    private AssetUri music;

    @Override
    public Task createTask() {
        return new PlayMusicTask(this);
    }

    public static class PlayMusicTask extends Task implements AudioEndListener {
        @In
        private AudioManager audioManager;
        @In
        private AssetManager assetManager;
        private boolean playing;
        private boolean finished;

        public PlayMusicTask(Node node) {
            super(node);
        }

        @Override
        public void onInitialize() {
            audioManager.registerListener(this);

            AssetUri uri = getNode().music;
            if (uri != null) {
                Sound asset = assetManager.loadAsset(uri, Sound.class);
                if (asset != null) {
                    audioManager.playMusic(asset);
                    playing = true;
                }
            }
        }

        @Override
        public void onTerminate(Status result) {
            audioManager.unregisterListener(this);
        }

        @Override
        public void onAudioEnd(Sound sound) {
            if (playing && getNode().music.equals(sound.getURI())) {
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
