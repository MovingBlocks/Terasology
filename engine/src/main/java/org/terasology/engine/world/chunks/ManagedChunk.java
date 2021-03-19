// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.chunks;

import org.terasology.module.sandbox.API;
import org.terasology.protobuf.EntityData;

/**
 */
@API
public interface ManagedChunk extends CoreChunk {

    void markReady();

    boolean isReady();

    void deflate();

    void deflateSunlight();

    void dispose();

    boolean isDisposed();

    void prepareForReactivation();

    // TODO: Expose appropriate iterators, remove this method
    EntityData.ChunkStore.Builder encode();
}
