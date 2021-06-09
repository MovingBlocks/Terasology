// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.unittest.stubs;

import org.terasology.gestalt.entitysystem.component.Component;

public final class IntegerComponent implements Component<IntegerComponent> {
    public int value;

    public IntegerComponent() {
    }

    public IntegerComponent(int value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        IntegerComponent that = (IntegerComponent) o;

        if (value != that.value) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return value;
    }

    @Override
    public void copy(IntegerComponent other) {
        this.value = other.value;
    }
}
