// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.reflection.copy.legacy;

import org.terasology.math.geom.Quat4f;
import org.terasology.reflection.copy.CopyStrategy;
import org.terasology.reflection.copy.RegisterCopyStrategy;

/**
 */
@RegisterCopyStrategy
public class Quat4fCopyStrategy implements CopyStrategy<Quat4f> {

    @Override
    public Quat4f copy(Quat4f value) {
        if (value != null) {
            return new Quat4f(value);
        }
        return null;
    }
}
