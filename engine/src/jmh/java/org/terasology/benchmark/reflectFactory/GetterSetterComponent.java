// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.benchmark.reflectFactory;

import org.terasology.gestalt.entitysystem.component.Component;

public class GetterSetterComponent implements Component<GetterSetterComponent> {
    public int value;

    public void setValue(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public void copyFrom(GetterSetterComponent other) {
        this.value = other.value;
    }
}
