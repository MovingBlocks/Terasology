// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core.subsystem.common;

import java.util.Set;

/**
 * Describes the set of gauges that may apply to a particular interface.
 */
public class GaugeMapEntry {
    public final Class<?> iface;
    public final Set<GaugeSpec<?>> gaugeSpecs;

    @SafeVarargs
    public <T> GaugeMapEntry(Class<T> iface, GaugeSpec<? extends T>... gaugeSpecs) {
        this.iface = iface;
        this.gaugeSpecs = Set.of(gaugeSpecs);
    }
}
