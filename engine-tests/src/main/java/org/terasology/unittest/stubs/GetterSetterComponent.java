// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.unittest.stubs;

import org.joml.Vector3f;
import org.terasology.engine.entitySystem.Component;

/**
 */
public class GetterSetterComponent implements Component {
    public transient boolean getterUsed;
    public transient boolean setterUsed;

    private Vector3f value = new Vector3f(0, 0, 0);

    public Vector3f getValue() {
        getterUsed = true;
        return value;
    }

    public void setValue(Vector3f value) {
        this.value = value;
        setterUsed = true;
    }
}
