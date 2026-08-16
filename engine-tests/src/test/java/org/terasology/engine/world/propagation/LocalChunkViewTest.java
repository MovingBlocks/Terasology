// Copyright 2026 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.propagation;

import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.terasology.engine.TerasologyTestingEnvironment;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.world.block.internal.BlockManagerImpl;
import org.terasology.engine.world.block.tiles.NullWorldAtlas;
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.engine.world.chunks.Chunks;
import org.terasology.engine.world.chunks.blockdata.ExtraBlockDataManager;
import org.terasology.engine.world.chunks.internal.ChunkImpl;
import org.terasology.engine.world.propagation.light.LightMerger;
import org.terasology.engine.world.propagation.light.LightPropagationRules;
import org.terasology.gestalt.assets.management.AssetManager;

import java.util.Arrays;
import java.util.Comparator;

import static com.google.common.truth.Truth.assertThat;

@Tag("TteTest")
class LocalChunkViewTest extends TerasologyTestingEnvironment {

    private BlockManagerImpl blockManager;
    private ExtraBlockDataManager extraDataManager;

    @BeforeEach
    @Override
    public void setup() throws Exception {
        super.setup();
        blockManager = new BlockManagerImpl(new NullWorldAtlas(), CoreRegistry.get(AssetManager.class), true);
        extraDataManager = new ExtraBlockDataManager();
    }

    /**
     * Builds the neighbourhood exactly as {@code LightMerger.merge} does - sorted by x, then y, then z -
     * since that sort is what defines the array order this view has to agree with.
     */
    private Chunk[] sortedNeighbourhoodAround(Vector3ic centre) {
        Chunk[] chunks = LightMerger.requiredChunks(centre).stream()
                .map(p -> (Chunk) new ChunkImpl(new Vector3i(p), blockManager, extraDataManager))
                .toArray(Chunk[]::new);
        Arrays.sort(chunks, Comparator.<Chunk>comparingInt(c -> c.getPosition().x())
                .thenComparingInt(c -> c.getPosition().y())
                .thenComparing(c -> c.getPosition().z()));
        return chunks;
    }

    /**
     * A write must land in the chunk that actually contains the position.
     * <p>
     * This did not hold: the view indexed the array with x varying fastest while the sort makes z vary
     * fastest, so x and z were transposed and a write aimed at the +X neighbour landed in the +Z one.
     * It went unnoticed because reads used the same wrong mapping - so a read-back check passes either
     * way - and because the centre chunk is invariant under the swap. Hence asserting on the chunks
     * themselves rather than on what the view returns.
     */
    @Test
    void writesLandInTheChunkContainingThePosition() {
        Chunk[] chunks = sortedNeighbourhoodAround(new Vector3i(0, 0, 0));
        LocalChunkView view = new LocalChunkView(chunks, new LightPropagationRules());

        for (Chunk expected : chunks) {
            Vector3ic chunkPos = expected.getPosition();
            // First block of that chunk, in world coordinates.
            Vector3ic blockPos = new Vector3i(
                    chunkPos.x() * Chunks.SIZE_X,
                    chunkPos.y() * Chunks.SIZE_Y,
                    chunkPos.z() * Chunks.SIZE_Z);

            Arrays.stream(chunks).forEach(c -> c.setLight(0, 0, 0, (byte) 0));
            view.setValueAt(blockPos, (byte) 15);

            assertThat(expected.getLight(0, 0, 0)).isEqualTo((byte) 15);
        }
    }

    /** A position outside the 3x3x3 must not alias onto a chunk that happens to sit at that index. */
    @Test
    void positionsOutsideTheViewAreUnavailable() {
        Chunk[] chunks = sortedNeighbourhoodAround(new Vector3i(0, 0, 0));
        LocalChunkView view = new LocalChunkView(chunks, new LightPropagationRules());

        // Three chunks along +X: outside the view, but a flat index would wrap into it.
        Vector3ic outside = new Vector3i(3 * Chunks.SIZE_X, 0, 0);

        assertThat(view.getValueAt(outside)).isEqualTo(PropagatorWorldView.UNAVAILABLE);
        assertThat(view.getBlockAt(outside)).isNull();

        view.setValueAt(outside, (byte) 15);
        Arrays.stream(chunks).forEach(c -> assertThat(c.getLight(0, 0, 0)).isEqualTo((byte) 0));
    }
}
