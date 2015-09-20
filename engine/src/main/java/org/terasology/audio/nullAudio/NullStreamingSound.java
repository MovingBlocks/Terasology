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

package org.terasology.audio.nullAudio;

import org.terasology.assets.Asset;
import org.terasology.assets.AssetType;
import org.terasology.assets.ResourceUrn;
import org.terasology.audio.StreamingSound;
import org.terasology.audio.StreamingSoundData;

import java.util.Optional;

/**
 *
 */
public final class NullStreamingSound extends StreamingSound {

    private int channels;
    private int sampleRate;

    public NullStreamingSound(ResourceUrn urn, AssetType<?, StreamingSoundData> assetType, StreamingSoundData data) {
        super(urn, assetType);
        reload(data);
    }

    public NullStreamingSound(ResourceUrn urn, AssetType<?, StreamingSoundData> assetType, int channels, int sampleRate) {
        super(urn, assetType);
        this.channels = channels;
        this.sampleRate = sampleRate;
    }

    @Override
    public void reset() {
    }

    @Override
    public int getChannels() {
        return channels;
    }

    @Override
    public int getSamplingRate() {
        return sampleRate;
    }

    @Override
    public int getBufferSize() {
        return 0;
    }

    @Override
    public void play() {
    }

    @Override
    public void play(float volume) {
    }

    @Override
    protected void doReload(StreamingSoundData data) {
        channels = data.getChannels();
        sampleRate = data.getSamplingRate();
        data.dispose();
    }

    @Override
    protected Optional<? extends Asset<StreamingSoundData>> doCreateCopy(ResourceUrn copyUrn, AssetType<?, StreamingSoundData> parentAssetType) {
        return Optional.of(new NullStreamingSound(copyUrn, parentAssetType, channels, sampleRate));
    }

}
