// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.chunks.pipeline.stages;

import org.terasology.world.chunks.Chunk;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * This class provide possible to create {@link FunctionalStage} which takes 3x3x3 view with processing chunk in center
 * futures for provided stage.
 * <p>
 * Ideally for stage which need neighbor chunks for processing.
 */
public interface StageProvider extends Function<Collection<CompletableFuture<Chunk>>,
        FunctionalStage>, ProcessingStage {
}
