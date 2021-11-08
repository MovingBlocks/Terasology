// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.layers.ingame.metrics;

public class NullMetricsMode extends MetricsMode {

    public NullMetricsMode() {
        super("");
    }

    @Override
    public String getMetrics() {
        return "";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
