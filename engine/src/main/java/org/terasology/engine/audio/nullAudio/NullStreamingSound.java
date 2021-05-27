// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.audio.nullAudio;

import org.terasology.engine.audio.StreamingSound;
import org.terasology.engine.audio.StreamingSoundData;
import org.terasology.gestalt.assets.Asset;
import org.terasology.gestalt.assets.AssetType;
import org.terasology.gestalt.assets.ResourceUrn;

import java.util.Optional;

/**
 * Null implementation of the StreamingSound
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
