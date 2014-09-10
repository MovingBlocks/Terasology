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

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import org.terasology.core.logic.generators.DefaultGenerators;
import org.terasology.core.world.generator.AbstractBaseWorldGenerator;
import org.terasology.core.world.generator.chunkGenerators.FloraGenerator;
import org.terasology.core.world.generator.chunkGenerators.ForestGenerator;
import org.terasology.core.world.generator.chunkGenerators.OreGenerator;
import org.terasology.core.world.generator.chunkGenerators.PerlinTerrainGenerator;
import org.terasology.core.world.liquid.LiquidsGenerator;
import org.terasology.engine.SimpleUri;
import org.terasology.entitySystem.Component;
import org.terasology.world.generator.RegisterWorldGenerator;
import org.terasology.world.generator.WorldConfigurator;

import java.util.Map;

/**
 * @author Immortius
 */
@RegisterWorldGenerator(id = "perlin", displayName = "Perlin", description = "Standard world generator")
public class PerlinWorldGenerator extends AbstractBaseWorldGenerator {

    public PerlinWorldGenerator(SimpleUri uri) {
        super(uri);
    }

    @Override
    public void initialize() {
        register(new PerlinTerrainGenerator());
        register(new FloraGenerator());
        register(new LiquidsGenerator());
        register(new OreGenerator());
        ForestGenerator forestGenerator = new ForestGenerator();
        DefaultGenerators.addDefaultForestGenerators(forestGenerator);
        register(forestGenerator);
    }

    @Override
    public Optional<WorldConfigurator> getConfigurator() {

        WorldConfigurator wc = new WorldConfigurator() {

            @Override
            public Map<String, Component> getProperties() {
                PerlinWorldConfigComponent configComp = new PerlinWorldConfigComponent();
                Map<String, Component> map = Maps.newHashMap();
                map.put("General", configComp);
                return map;
            }

        };

        return Optional.of(wc);
    }
}
