// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.reflection.copy.legacy;

import org.terasology.math.geom.Vector2i;
import org.terasology.reflection.copy.CopyStrategy;
import org.terasology.reflection.copy.RegisterCopyStrategy;

/**
 */
@RegisterCopyStrategy
public class Vector2iCopyStrategy implements CopyStrategy<Vector2i> {
    @Override
    public Vector2i copy(Vector2i value) {
        return new Vector2i(value);
    }
}
