/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.world.block;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.math.geom.Vector4f;
import org.terasology.world.biomes.Biome;

import javax.imageio.ImageIO;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Different color sources for blocks.
 */
public enum DefaultColorSource implements BlockColorSource {

    DEFAULT {
        @Override
        public Vector4f calcColor(Biome biome) {
            return new Vector4f(1, 1, 1, 1);
        }
    },
    COLOR_LUT {
        @Override
        public Vector4f calcColor(Biome biome) {
            float humidity = biome.getHumidity();
            float temperature = biome.getTemperature();
            float prod = temperature * humidity;
            int rgbValue = colorLut.getRGB((int) ((1.0 - temperature) * 255.0), (int) ((1.0 - prod) * 255.0));

            Color c = new Color(rgbValue);
            return new Vector4f(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, 1.0f);
        }
    },
    FOLIAGE_LUT {
        @Override
        public Vector4f calcColor(Biome biome) {
            float humidity = biome.getHumidity();
            float temperature = biome.getTemperature();
            float prod = humidity * temperature;
            int rgbValue = foliageLut.getRGB((int) ((1.0 - temperature) * 255.0), (int) ((1.0 - prod) * 255.0));

            Color c = new Color(rgbValue);
            return new Vector4f(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, 1.0f);
        }
    };

    private static final Logger logger = LoggerFactory.getLogger(DefaultColorSource.class);

    /* LUTs */
    private static BufferedImage colorLut;

    private static BufferedImage foliageLut;

    static {
        try {
            // TODO: Read these from asset manager
            colorLut = ImageIO.read(DefaultColorSource.class.getResource("/assets/textures/grasscolor.png"));
            foliageLut = ImageIO.read(DefaultColorSource.class.getResource("/assets/textures/foliagecolor.png"));
        } catch (IOException e) {
            logger.error("Failed to load LUTs", e);
        }
    }

}
