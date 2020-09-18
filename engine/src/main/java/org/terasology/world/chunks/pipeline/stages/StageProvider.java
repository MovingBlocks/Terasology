// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.chunks.pipeline.stages;

import org.terasology.world.chunks.Chunk;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public interface StageProvider extends Function<Collection<CompletableFuture<Chunk>>,
        FunctionalStage>, ProcessingStage {
}
