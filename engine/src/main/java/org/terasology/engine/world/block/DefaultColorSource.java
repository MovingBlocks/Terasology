// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.block;

import org.terasology.engine.registry.CoreRegistry;
import org.terasology.nui.Color;
import org.terasology.nui.Colorc;

public enum DefaultColorSource implements BlockColorSource {
    DEFAULT {
        @Override
        public Colorc calcColor(int x, int y, int z) {
            return Color.white;
        }
    },
    COLOR_LUT {
        @Override
        public Colorc calcColor(int x, int y, int z) {
            if (colorProvider == null) {
                colorProvider = CoreRegistry.get(ColorProvider.class);
            }
            return colorProvider.colorLut(x, y, z);
        }
    },
    FOLIAGE_LUT {
        @Override
        public Colorc calcColor(int x, int y, int z) {
            if (colorProvider == null) {
                colorProvider = CoreRegistry.get(ColorProvider.class);
            }
            return colorProvider.foliageLut(x, y, z);
        }
    };

    private static ColorProvider colorProvider;
}
