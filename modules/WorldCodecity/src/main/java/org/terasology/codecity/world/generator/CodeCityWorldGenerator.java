package org.terasology.codecity.world.generator;

import org.terasology.engine.SimpleUri;
import org.terasology.world.generation.BaseFacetedWorldGenerator;
import org.terasology.world.generation.WorldBuilder;
import org.terasology.world.generator.RegisterWorldGenerator;

@RegisterWorldGenerator(id = "codecity", displayName = "CodeCity", description = "Generates the world using a CodeCity structure")
public class CodeCityWorldGenerator extends BaseFacetedWorldGenerator {
    public CodeCityWorldGenerator(SimpleUri uri) {
        super(uri);
    }

    @Override
    protected WorldBuilder createWorld(long seed) {
        return new WorldBuilder(seed).addProvider(new CodeCityGroundProvider())
                .addRasterizer(new CodeCityGroundRasterizer())
                .addRasterizer(new CodeCityBuildingRasterizer()).setSeaLevel(0);
    }
}
