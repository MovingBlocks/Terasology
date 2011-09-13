/*
 * Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
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
package com.github.begla.blockmania.debug;

import com.github.begla.blockmania.generators.ChunkGeneratorTerrain;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class TerrainPreviewGenerator extends ChunkGeneratorTerrain {

    private static int zoomOut = 2;

    /**
     * @param seed
     */
    public TerrainPreviewGenerator(String seed) {
        super(seed);
    }

    public static void main(String[] args) {
        TerrainPreviewGenerator gen = new TerrainPreviewGenerator("abcd");
        gen.generateBaseTerrainImage();
        gen.generateBiomeMap();
    }

    public void generateBiomeMap() {
        BufferedImage image = new BufferedImage(1024, 1024, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        for (int x = -512; x < 512; x++) {
            for (int y = -512; y < 512; y++) {
                BIOME_TYPE n = calcBiomeType(x * zoomOut, y * zoomOut);

                Color color = Color.BLACK;

                switch (n) {
                    case PLAINS:
                        color = Color.BLUE;
                        break;
                    case MOUNTAINS:
                        color = Color.RED;
                        break;
                    case SNOW:
                        color = Color.WHITE;
                        break;
                    case DESERT:
                        color = Color.YELLOW;
                        break;
                }

                g.setColor(color);
                g.fillRect(x + 512, y + 512, 1, 1);
            }
        }

        try {
            ImageIO.write(image, "png", new File("BiomeMap.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void generateBaseTerrainImage() {
        BufferedImage image = new BufferedImage(1024, 1024, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        for (int x = -512; x < 512; x++) {
            for (int y = -512; y < 512; y++) {
                double n = calcBaseTerrain(x * zoomOut, y * zoomOut);

                int color = (int) (n * 255.0);
                color = (color > 255) ? 255 : color;
                color = (color < 0) ? 0 : color;

                g.setColor(new Color(color, color, color));
                g.fillRect(x + 512, y + 512, 1, 1);
            }
        }

        try {
            ImageIO.write(image, "png", new File("BaseTerrain.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
