// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.unittest.stubs;

import org.terasology.gestalt.entitysystem.component.Component;

import java.util.Map;

public final class OrderedMapTestComponent implements Component<OrderedMapTestComponent> {
    public Map<String, Long> orderedMap;

    @Override
    public void copy(OrderedMapTestComponent other) {
        this.orderedMap = other.orderedMap;
    }
}
