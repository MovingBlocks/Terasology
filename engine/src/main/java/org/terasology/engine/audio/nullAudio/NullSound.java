// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.audio.nullAudio;

import org.terasology.engine.audio.StaticSound;
import org.terasology.engine.audio.StaticSoundData;
import org.terasology.gestalt.assets.Asset;
import org.terasology.gestalt.assets.AssetType;
import org.terasology.gestalt.assets.ResourceUrn;

import java.util.Optional;

/**
 * Null implementation of the Static Sound
 *
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
