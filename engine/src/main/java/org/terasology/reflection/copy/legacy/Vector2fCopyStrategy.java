// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.reflection.copy.legacy;

import org.terasology.math.geom.Vector2f;
import org.terasology.reflection.copy.CopyStrategy;
import org.terasology.reflection.copy.RegisterCopyStrategy;

/**
 */
@RegisterCopyStrategy
public class Vector2fCopyStrategy implements CopyStrategy<Vector2f> {

    @Override
    public Vector2f copy(Vector2f value) {
        if (value != null) {
            return new Vector2f(value);
        }
        return null;
    }
}
