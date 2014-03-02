/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.core.world.generator.worldGenerators;

import org.terasology.core.world.generator.AbstractBaseWorldGenerator;
import org.terasology.core.world.generator.chunkGenerators.BasicHMTerrainGenerationPass;
import org.terasology.core.world.generator.chunkGenerators.FloraGenerationPass;
import org.terasology.core.world.liquid.LiquidsGenerationPass;
import org.terasology.engine.SimpleUri;
import org.terasology.world.generator.RegisterWorldGenerator;

/**
 * @author Immortius
 */
@RegisterWorldGenerator(id = "heightMap", displayName = "Height Map", description = "Generates the world using a height map")
public class HeightMapWorldGenerator extends AbstractBaseWorldGenerator {

    public HeightMapWorldGenerator(SimpleUri uri) {
        super(uri);
    }

    @Override
    public void initialize() {
        register(new BasicHMTerrainGenerationPass());
        register(new FloraGenerationPass());
        register(new LiquidsGenerationPass());
        /*ForestGenerator forestGenerator = new ForestGenerator();
        DefaultGenerators.addDefaultForestGenerators(forestGenerator);
        register(forestGenerator);*/
    }
}
