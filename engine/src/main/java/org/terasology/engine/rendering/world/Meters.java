// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.world;

import io.micrometer.core.instrument.binder.BaseUnits;
import org.terasology.engine.core.subsystem.common.GaugeMapEntry;
import org.terasology.engine.core.subsystem.common.GaugeSpec;

import java.util.List;

public final class Meters {
    public static final String PREFIX = Meters.class.getPackageName();

    public static final List<GaugeMapEntry> GAUGE_MAP = List.of(
            new GaugeMapEntry(WorldRenderer.class,
                    new GaugeSpec<WorldRendererImpl>(
                            PREFIX + ".emptyMeshChunks",
                            "Empty Mesh Chunks",
                            wri -> wri.statChunkMeshEmpty,
                            BaseUnits.OBJECTS),
                    new GaugeSpec<WorldRendererImpl>(
                            PREFIX + ".unreadyChunks",
                            "Unready Chunks",
                            wri -> wri.statChunkNotReady,
                            BaseUnits.OBJECTS),
                    new GaugeSpec<WorldRendererImpl>(
                            PREFIX + ".triangles",
                            "Rendered Triangles",
                            wri -> wri.statRenderedTriangles,
                            BaseUnits.OBJECTS)
            ),
            new GaugeMapEntry(RenderableWorld.class,
                    new GaugeSpec<RenderableWorldImpl>(
                            PREFIX + ".visibleChunks",
                            "Visible Chunks",
                            rwi -> rwi.statVisibleChunks,
                            BaseUnits.OBJECTS),
                    new GaugeSpec<RenderableWorldImpl>(
                            PREFIX + ".dirtyChunks",
                            "Dirty Chunks",
                            rwi -> rwi.statDirtyChunks,
                            BaseUnits.OBJECTS),
                    new GaugeSpec<RenderableWorldImpl>(
                            PREFIX + ".dirtyChunks",
                            "Ignored Phases",
                            rwi -> rwi.statIgnoredPhases)
            )
    );

    private Meters() { }
}
