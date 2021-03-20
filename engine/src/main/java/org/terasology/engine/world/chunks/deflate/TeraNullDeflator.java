// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.chunks.deflate;

import org.terasology.engine.world.chunks.blockdata.TeraArray;

/**
 * TeraNullDeflator performs no deflation at all. It just returns the passed array.
 *
 */
public class TeraNullDeflator extends TeraDeflator {

    public TeraNullDeflator() {
    }

    @Override
    public TeraArray deflate(TeraArray in) {
        return in;
    }

}
