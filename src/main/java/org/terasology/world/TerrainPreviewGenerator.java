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

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;

import org.terasology.world.block.Block;
import org.terasology.world.generator.core.PerlinTerrainGeneratorWithSetup;
import java.awt.*;

/**
 * Simple preview generator. Generates heightmap images using the terrain
 * generator.
 * 
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public final class TerrainPreviewGenerator {

    public enum MapStyle {
        BIOMES, FOLIAGE_LUT, COLOR_LUT
    }

    /* CONST */
    private static int ZOOM_FACTOR = 8;
    private static final Vector2f POSITION = new Vector2f(0.0f, 0.0f);
    private static final int SIZE_FACTOR = 64;  //resolution
    private static final int SIZE_FINAL = 256;  //final size (scaled)

    private final WorldBiomeProvider biomeProvider;
    PerlinTerrainGeneratorWithSetup generator;
    public BufferedImage image = new BufferedImage(SIZE_FACTOR, SIZE_FACTOR, BufferedImage.TYPE_INT_RGB);

    public Graphics2D g = image.createGraphics();

    public TerrainPreviewGenerator(final String seed) {
        biomeProvider = new WorldBiomeProviderImpl(seed);
        generator = new PerlinTerrainGeneratorWithSetup();
        generator.setWorldSeed(seed);
        generator.setWorldBiomeProvider(biomeProvider);
    }

    public void generateMap(final MapStyle mapStyle, final String fileName) {
        int counter = 0;
        for (int x = -SIZE_FACTOR/2; x < SIZE_FACTOR/2; x++) {
            for (int z = -SIZE_FACTOR/2; z < SIZE_FACTOR/2; z++) {
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

                        for (int height = 128; height > 0; height -= 4) {
                            final double n = generator.calcDensity((x * ZOOM_FACTOR) + (int) POSITION.x, height - 1, (z * ZOOM_FACTOR) + (int) POSITION.y);

                            if (n >= 0) {
                                if (height > 32) {
                                    g.setColor(new Color(Math.min(height + color.getRed(), 255), Math.min(height + color.getGreen(), 255), Math.min(height + color.getBlue(), 255)));
                                } else {
                                    g.setColor(new Color(0, 0, (int) ((255.0 * (32.0 - (32.0 - height))) / 32.0)));
                                }

                                g.fillRect(x + SIZE_FACTOR/2, z + SIZE_FACTOR/2, 1, 1);
                                break;
                            }
                        }
                        break;
                    case COLOR_LUT:
                        break;
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

                        for (int height = 128; height > 0; height -= 8) {
                            final double n = generator.calcDensity((x * ZOOM_FACTOR) + (int) POSITION.x, height - 1, (z * ZOOM_FACTOR) + (int) POSITION.y);

                            if (n >= 0) {
                                g.setColor(new Color(vecCol.x, vecCol.y, vecCol.z));
                                g.fillRect(x + SIZE_FACTOR/2, z + SIZE_FACTOR/2, 1, 1);

                                break;
                            }
                        }
                        break;
                }

                counter++;

                if ((counter % 1024) == 0) {
                    System.out.printf("%.2f %%\n", (counter / ((float)SIZE_FACTOR * (float)SIZE_FACTOR)) * 100.0);
                }
            }
        }

        image = mkThumbImage(image,SIZE_FINAL);
        /*
        try {
            ImageIO.write(mkThumbImage(image,SIZE_FINAL), "png", new File(PathManager.getInstance().getDataPath()+"\\src\\main\\resources\\assets\\textures\\Biomes.png"));

        } catch (final IOException e) {
            e.printStackTrace();
        }
        */
    }
     //Scale buffered Image
    public static BufferedImage mkThumbImage(BufferedImage orig, int thumbW) {
        double origRatio = (double)orig.getWidth() / (double)orig.getHeight();
        int thumbH = (int)(thumbW * origRatio);
        Image scaled = orig.getScaledInstance(thumbW, thumbH, Image.SCALE_SMOOTH);
        BufferedImage ret = new BufferedImage(thumbW, thumbH, BufferedImage.TYPE_INT_RGB);
        ret.getGraphics().drawImage(scaled, 0, 0, null);
        return ret;
    }

    public static float setZOOM_FACTOR(float ZOOM) {
        ZOOM_FACTOR = (int)ZOOM;
        return 1f;
    }
}
