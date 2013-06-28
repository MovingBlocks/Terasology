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

package org.terasology.world.generator;

import java.util.List;

import org.terasology.math.Vector3i;
import org.terasology.rendering.gui.widgets.UIDialog;
import org.terasology.world.WorldBiomeProvider;
import org.terasology.world.WorldView;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.generator.BaseChunkGenerator;

/**
 * @author Immortius
 */
public interface MapGenerator {
    String name();
    MapGeneratorUri uri();

    void setup();

    void setWorldSeed(String seed);

    void setWorldBiomeProvider(WorldBiomeProvider biomeProvider);

    Chunk generateChunk(Vector3i pos);

    void secondPassChunk(Vector3i chunkPos, WorldView view);

    boolean hasSetup();
    UIDialog createSetupDialog();

}
