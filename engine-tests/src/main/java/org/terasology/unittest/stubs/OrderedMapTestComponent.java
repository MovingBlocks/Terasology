// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.unittest.stubs;

import org.terasology.engine.entitySystem.Component;

import java.util.Map;

public final class OrderedMapTestComponent implements Component {
    public Map<String, Long> orderedMap;
}
