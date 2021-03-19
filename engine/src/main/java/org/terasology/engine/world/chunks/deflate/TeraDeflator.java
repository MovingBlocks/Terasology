// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.chunks.deflate;

import org.terasology.engine.world.chunks.blockdata.TeraArray;

/**
 * TeraDeflator is the abstract base class used to implement chunk deflation.
 *
 */
public abstract class TeraDeflator {

    public TeraDeflator() {
    }

    public abstract TeraArray deflate(TeraArray in);

}
