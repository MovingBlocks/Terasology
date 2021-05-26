// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.audio;

import org.terasology.gestalt.assets.AssetData;

import java.nio.ByteBuffer;

/**
 * The information used to create a streaming sound asset
 */
public interface StreamingSoundData extends AssetData {

    ByteBuffer readNextInto(ByteBuffer dataBuffer);

    int getChannels();

    int getBufferBits();

    int getSamplingRate();

    void reset();

    void dispose();

}
