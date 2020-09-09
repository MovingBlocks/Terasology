// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.benchmark.reflectFactory;

import org.terasology.engine.entitySystem.Component;

/**
 *
 */
public class GetterSetterComponent implements Component {
    private int value;

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
