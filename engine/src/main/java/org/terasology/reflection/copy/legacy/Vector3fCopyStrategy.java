// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.reflection.copy.legacy;

import org.terasology.math.geom.Vector3f;
import org.terasology.reflection.copy.CopyStrategy;
import org.terasology.reflection.copy.RegisterCopyStrategy;

/**
 */
@RegisterCopyStrategy
public class Vector3fCopyStrategy implements CopyStrategy<Vector3f> {

    @Override
    public Vector3f copy(Vector3f value) {
        if (value != null) {
            return new Vector3f(value);
        }
        return null;
    }
}
