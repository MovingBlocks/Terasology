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
package com.github.begla.blockmania.worldgenerator;

import com.github.begla.blockmania.World;

/**
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class Main {

    /**
     * 
     * @param args
     */
    public static void main(String[] args) {
        Main gen = new Main();
        gen.generateWorld("", "blubb", 16, 1024);
    }

    /**
     * 
     * @param title
     * @param seed
     * @param sizeX
     * @param sizeZ
     */
    public void generateWorld(String title, String seed, int sizeX, int sizeZ) {
        int size = sizeX * sizeZ;
        World w = new World(title, seed, null);

        int counter = 0;
        for (int x = 0; x < sizeX; x++) {
            for (int z = 0; z < sizeZ; z++) {
                w.generateNewChunk(x, z);
                counter++;

                if (counter % 10 == 0) {
                    System.out.printf("World generation running... %.3f%%\n", ((float) counter / size) * 100);
                }
            }
        }
    }
}
