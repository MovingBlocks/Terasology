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
import org.terasology.audio.StaticSound;
import org.terasology.audio.StaticSoundData;

import java.util.Optional;

/**
 */
public class NullSound extends StaticSound {

    private int channels;
    private int sampleRate;
    private float length;


    public NullSound(ResourceUrn urn, AssetType<?, StaticSoundData> assetType, StaticSoundData data) {
        super(urn, assetType);
        reload(data);
    }

    public NullSound(ResourceUrn urn, AssetType<?, StaticSoundData> assetType, int channels, int sampleRate, float length) {
        super(urn, assetType);
        this.channels = channels;
        this.sampleRate = sampleRate;
        this.length = length;
    }

    @Override
    public float getLength() {
        return length;
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
    protected void doReload(StaticSoundData data) {
        this.channels = data.getChannels();
        this.sampleRate = data.getSampleRate();
        this.length = data.getData().limit() / getChannels() / (data.getBufferBits() / 8) / getSamplingRate();
    }

    @Override
    protected Optional<? extends Asset<StaticSoundData>> doCreateCopy(ResourceUrn instanceUrn, AssetType<?, StaticSoundData> parentAssetType) {
        return Optional.of(new NullSound(instanceUrn, parentAssetType, channels, sampleRate, length));
    }

}
