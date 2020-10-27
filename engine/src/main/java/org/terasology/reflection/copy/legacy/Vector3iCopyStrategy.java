// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.reflection.copy.legacy;

import org.terasology.math.geom.Vector3i;
import org.terasology.reflection.copy.CopyStrategy;
import org.terasology.reflection.copy.RegisterCopyStrategy;

/**
 */
@RegisterCopyStrategy
public class Vector3iCopyStrategy implements CopyStrategy<Vector3i> {

    @Override
    public Vector3i copy(Vector3i value) {
        if (value != null) {
            return new Vector3i(value);
        }
        return null;
    }
}
