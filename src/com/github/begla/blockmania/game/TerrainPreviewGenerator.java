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
package com.github.begla.blockmania.game;

import com.github.begla.blockmania.logic.generators.ChunkGeneratorTerrain;
import com.github.begla.blockmania.logic.generators.GeneratorManager;
import com.github.begla.blockmania.logic.world.LocalWorldProvider;

import javax.imageio.ImageIO;
import javax.vecmath.Vector2f;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Simple preview generator. Generates heightmap images using the terrain generator.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class TerrainPreviewGenerator extends ChunkGeneratorTerrain {

    /* CONST */
    private static final int ZOOM_FACTOR = 4;
    private static final Vector2f POSITION = new Vector2f(-24575.82f, 20786.54f);

    /**
     * Init. the generator with a given seed value.
     */
    public TerrainPreviewGenerator(GeneratorManager generatorManager) {
        super(generatorManager);
    }

    public static void main(String[] args) {
        GeneratorManager manager = new GeneratorManager(new LocalWorldProvider("World1", "Blockmania42"));
        TerrainPreviewGenerator gen = new TerrainPreviewGenerator(manager);

        gen.generateMap();
    }

    public void generateMap() {
        BufferedImage image = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        int counter = 0;
        for (int x = -128; x < 128; x++) {
            for (int z = -128; z < 128; z++) {
                BIOME_TYPE bt = calcBiomeTypeForGlobalPosition(x * ZOOM_FACTOR + (int) POSITION.x, z * ZOOM_FACTOR + (int) POSITION.y);

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
                    double n = calcDensity(x * ZOOM_FACTOR + (int) POSITION.x, height - 1, z * ZOOM_FACTOR + (int) POSITION.y);

                    if (n >= 0) {
                        if (height > 32)
                            g.setColor(new Color(Math.min(height + color.getRed(), 255), Math.min(height + color.getGreen(), 255), Math.min(height + color.getBlue(), 255)));
                        else
                            g.setColor(new Color(0, 0, (int) (255.0 * (32.0 - (32.0 - height)) / 32.0)));

                        g.fillRect(x + 128, z + 128, 1, 1);
                        break;
                    }
                }

                counter++;
                System.out.printf("%.2f %%\n", (counter / (256.0 * 256.0)) * 100.0);
            }
        }

        try {
            ImageIO.write(image, "png", new File("Terrain.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
