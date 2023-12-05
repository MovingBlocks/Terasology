// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.unittest.stubs;

import org.joml.Vector3f;
import org.terasology.gestalt.entitysystem.component.Component;

public class GetterSetterComponent implements Component<GetterSetterComponent> {
    public transient boolean getterUsed;
    public transient boolean setterUsed;

    public Vector3f value = new Vector3f(0, 0, 0);

    public Vector3f getValue() {
        getterUsed = true;
        return value;
    }

    public void setValue(Vector3f value) {
        this.value = value;
        setterUsed = true;
    }

    @Override
    public void copyFrom(GetterSetterComponent other) {
        this.getterUsed = other.getterUsed;
        this.setterUsed = other.setterUsed;
    }
}
