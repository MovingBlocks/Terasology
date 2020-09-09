// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.audio;

import org.terasology.gestalt.assets.AssetData;

import java.nio.ByteBuffer;

/**
 * The information used to create Sound assets.
 */
public class StaticSoundData implements AssetData {
    private final ByteBuffer data;
    private final int channels;
    private final int sampleRate;
    private final int bufferBits;

    public StaticSoundData(ByteBuffer soundData, int channels, int sampleRate, int bufferBits) {
        this.data = soundData;
        this.channels = channels;
        this.sampleRate = sampleRate;
        this.bufferBits = bufferBits;
    }


    public ByteBuffer getData() {
        return data;
    }

    public int getChannels() {
        return channels;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public int getBufferBits() {
        return bufferBits;
    }
}
