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
            ColorProvider colorProvider = CoreRegistry.get(ColorProvider.class);
            // Return white as default if there aren't any color providers
            if (colorProvider == null) {
                return Color.white;
            }
            return colorProvider.colorLut(x, y, z);
        }
    },
    FOLIAGE_LUT {
        @Override
        public Colorc calcColor(int x, int y, int z) {
            ColorProvider colorProvider = CoreRegistry.get(ColorProvider.class);
            // Return white as default if there aren't any color providers
            if (colorProvider == null) {
                return Color.white;
            }
            return colorProvider.foliageLut(x, y, z);
        }
    };
}
