/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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
package org.terasology.world;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.TerasologyDevelopment;
import org.terasology.world.block.Block;
import org.terasology.world.generator.chunkGenerators.PerlinTerrainGenerator;

/**
 * Simple preview generator. Generates heightmap images using the terrain
 * generator.
 * 
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public final class TerrainPreviewGenerator {

    private static final Logger logger = LoggerFactory.getLogger(TerrainPreviewGenerator.class);

    private static final String SEED = "rAtAiWyKgDlEeFjKiSsPzKaOuKhRrWqV";

    private enum MapStyle {
        BIOMES, FOLIAGE_LUT, COLOR_LUT
    }

    /* CONST */
    private static final int ZOOM_FACTOR = 8;
    private static final Vector2f POSITION = new Vector2f(0.0f, 0.0f);

    private final WorldBiomeProvider biomeProvider;
    PerlinTerrainGenerator generator;

    public static void main(final String[] args) {
        final TerrainPreviewGenerator gen = new TerrainPreviewGenerator(SEED);

        gen.generateMap(MapStyle.BIOMES, "Biomes.png");
        gen.generateMap(MapStyle.COLOR_LUT, "ColorLut.png");
        gen.generateMap(MapStyle.FOLIAGE_LUT, "FoliageLut.png");
    }

    public TerrainPreviewGenerator(final String seed) {
        biomeProvider = new WorldBiomeProviderImpl(seed);
        generator = new PerlinTerrainGenerator();
        generator.setWorldSeed(seed);
        generator.setWorldBiomeProvider(biomeProvider);
    }

    public void generateMap(final MapStyle mapStyle, final String fileName) {
        final BufferedImage image = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
        final Graphics2D g = image.createGraphics();

        int counter = 0;
        for (int x = -128; x < 128; x++) {
            for (int z = -128; z < 128; z++) {
                switch (mapStyle) {
                    case BIOMES:
                        final WorldBiomeProvider.Biome bt = biomeProvider.getBiomeAt((x * ZOOM_FACTOR) + (int) POSITION.x, (z * ZOOM_FACTOR) + (int) POSITION.y);

                        Color color = Color.BLACK;

                        switch (bt) {
                            case PLAINS:
                                color = new Color(0, 0, 25);
                                break;
                            case MOUNTAINS:
                                color = new Color(25, 0, 0);
                                break;
                            case SNOW:
                                color = new Color(25, 25, 25);
                                break;
                            case DESERT:
                                color = new Color(25, 25, 0);
                                break;
                            case FOREST:
                                color = new Color(0, 25, 0);
                                break;
                        }

                        for (int height = 256; height > 0; height -= 4) {
                            final double n = generator.calcDensity((x * ZOOM_FACTOR) + (int) POSITION.x, height - 1, (z * ZOOM_FACTOR) + (int) POSITION.y);

                            if (n >= 0) {
                                if (height > 32) {
                                    g.setColor(new Color(
                                            Math.min(height + color.getRed(), 255),
                                            Math.min(height + color.getGreen(), 255),
                                            Math.min(height + color.getBlue(), 255)));
                                } else {
                                    g.setColor(new Color(0, 0, (int) ((255.0 * (32.0 - (32.0 - height))) / 32.0)));
                                }

                                g.fillRect(x + 128, z + 128, 1, 1);
                                break;
                            }
                        }
                        break;
                    case COLOR_LUT:
                    case FOLIAGE_LUT:
                        float humidity = biomeProvider.getHumidityAt((x * ZOOM_FACTOR) + (int) POSITION.x, (z * ZOOM_FACTOR) + (int) POSITION.y);
                        final float temp = biomeProvider.getTemperatureAt((x * ZOOM_FACTOR) + (int) POSITION.x, (z * ZOOM_FACTOR) + (int) POSITION.y);
                        humidity *= temp;

                        Vector4f vecCol = new Vector4f();

                        if (mapStyle == MapStyle.COLOR_LUT) {
                            vecCol = Block.ColorSource.COLOR_LUT.calcColor(temp, humidity);
                        } else if (mapStyle == MapStyle.FOLIAGE_LUT) {
                            vecCol = Block.ColorSource.FOLIAGE_LUT.calcColor(temp, humidity);
                        }

                        for (int height = 256; height > 0; height -= 4) {
                            final double n = generator.calcDensity((x * ZOOM_FACTOR) + (int) POSITION.x, height - 1, (z * ZOOM_FACTOR) + (int) POSITION.y);

                            if (n >= 0) {
                                g.setColor(new Color(vecCol.x, vecCol.y, vecCol.z));
                                g.fillRect(x + 128, z + 128, 1, 1);

                                break;
                            }
                        }
                        break;
                }

                counter++;

                if ((counter % 1024) == 0) {
                    System.out.printf("%.2f %%\n", (counter / (256.0 * 256.0)) * 100.0);
                }
            }
        }

        try {
            ImageIO.write(image, "png", TerasologyDevelopment.getOutputFolder(fileName));
        } catch (final IOException e) {
            logger.error("Failed to write terrain preview", e);
        }
    }
}
