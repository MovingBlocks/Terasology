// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.reflection.copy.legacy;

import org.terasology.math.geom.Vector4f;
import org.terasology.reflection.copy.CopyStrategy;
import org.terasology.reflection.copy.RegisterCopyStrategy;

/**
 */
@RegisterCopyStrategy
public class Vector4fCopyStrategy implements CopyStrategy<Vector4f> {

    @Override
    public Vector4f copy(Vector4f value) {
        if (value != null) {
            return new Vector4f(value);
        }
        return null;
    }
}
