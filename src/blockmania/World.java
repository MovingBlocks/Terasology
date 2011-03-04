/*
 *  Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package blockmania;

import java.util.Random;
import java.util.ArrayList;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

/**
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class World extends RenderObject {

    // The title of the world
    String title = "World 1";
    // The seed used for the terrain generation
    String seed = "BLOCKMANIA";
    ArrayList<Vector4f> blocks = new ArrayList<Vector4f>();
    // The chunks to display
    Chunk[][][] chunks;
    // Random number generator
    Random rand = new Random();
    PerlinNoiseGenerator pGen = new PerlinNoiseGenerator();
    public static final Vector3f worldDimensions = new Vector3f(1024, 256, 1024);
    long timeSinceLastChunkUpdate = 0;
    Player[] players = new Player[32];

    public World(Player mainChar) {
        chunks = new Chunk[(int) Configuration.viewingDistanceInChunks.x][2][(int) Configuration.viewingDistanceInChunks.y];
        players[0] = mainChar;

        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                updateWorld();
            }
        });

        t.start();
    }

    @Override
    public void render() {

        for (int x = 0; x < (int) Configuration.viewingDistanceInChunks.x; x++) {
            for (int y = 0; y < 1; y++) {
                for (int z = 0; z < (int) Configuration.viewingDistanceInChunks.y; z++) {
                    if (chunks[x][y][z] != null) {
                        // Render active chunks only
                        chunks[x][y][z].render();

                        if (System.currentTimeMillis() - timeSinceLastChunkUpdate > 50) {
                            if (chunks[x][y][z].updateDisplayList()) {
                                timeSinceLastChunkUpdate = System.currentTimeMillis();
                            }
                        }
                    }


                }
            }
        }
    }

    public final void updateWorld() {

        long timeStart = System.currentTimeMillis();

        for (int x = 0; x < worldDimensions.x; x++) {
            for (int z = 0; z < worldDimensions.z; z++) {

                float height = pGen.getTerrainHeightAt(x/4.0f, z/4.0f);

                if (height < 0) {
                    height = 0;
                }

                float y = height * 256 + 32.0f;

                if (y > 64) {
                    y = 64;
                }

                int chunkX = (int) Math.floor(x / (int) Chunk.chunkDimensions.x);
                int chunkY = (int) Math.floor(y / (int) Chunk.chunkDimensions.y);
                int chunkZ = (int) Math.floor(z / (int) Chunk.chunkDimensions.z);

                // Create a new chunk if needed
                if (chunks[chunkX][chunkY][chunkZ] == null) {
                    System.out.println("Generating chunk at X: " + chunkX + ", Y: " + chunkY + ", Z: " + chunkZ);
                    chunks[chunkX][chunkY][chunkZ] = new Chunk(new Vector3f(chunkX, chunkY, chunkZ));
                }

                Vector3f blockCoord = new Vector3f(x - (chunkX * Chunk.chunkDimensions.x), y - (chunkY * Chunk.chunkDimensions.y), z - (chunkZ * Chunk.chunkDimensions.z));

                // Generate or update the corresponding chunk
                chunks[chunkX][chunkY][chunkZ].setBlock((int) blockCoord.x, (int) blockCoord.y, (int) blockCoord.z, 0x1);
                blockCoord.y--;

                // Fill with dirt
                while (blockCoord.y >= 0) {
                    chunks[chunkX][chunkY][chunkZ].setBlock((int) blockCoord.x, (int) blockCoord.y, (int) blockCoord.z, 0x2);
                    blockCoord.y--;
                }
            }
        }

        System.out.println("World updated (" + (System.currentTimeMillis() - timeStart) / 1000d + "s).");

    }
}
