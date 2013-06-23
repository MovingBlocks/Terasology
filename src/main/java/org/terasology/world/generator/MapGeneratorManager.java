package org.terasology.world.generator;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.mod.ModManager;
import org.terasology.rendering.gui.dialogs.UIDialogSetUpMap;
import org.terasology.rendering.gui.widgets.UIDialog;
import org.terasology.world.generator.core.*;
import org.terasology.world.liquid.LiquidsGenerator;

import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Retrieves all available map generators per reflection. You can access map generators using MapGeneratorUri
 *
 * @author synopia
 */
public class MapGeneratorManager {
    private static final Logger logger = LoggerFactory.getLogger(MapGeneratorManager.class);

    public static class Perlin extends BaseMapGenerator {
        public Perlin() {
            super(new MapGeneratorUri("core:perlin"));
        }

        @Override
        public void setup() {
            registerChunkGenerator(new PerlinTerrainGenerator());
            registerChunkGenerator(new FloraGenerator());
            registerChunkGenerator(new LiquidsGenerator());
            registerChunkGenerator(new ForestGenerator());
        }

        @Override
        public String name() {
            return "Perlin";
        }
    }

    public static class PerlinSetup extends BaseMapGenerator {
        public PerlinSetup() {
            super(new MapGeneratorUri("core:perlin-setup"));
        }

        @Override
        public void setup() {
            registerChunkGenerator(new PerlinTerrainGeneratorWithSetup());
            registerChunkGenerator(new FloraGenerator());
            registerChunkGenerator(new LiquidsGenerator());
            registerChunkGenerator(new ForestGenerator());
        }

        @Override
        public String name() {
            return "Perlin with setup";
        }

        @Override
        public boolean hasSetup() {
            return true;
        }

        @Override
        public UIDialog createSetupDialog() {
            return new UIDialogSetUpMap();
        }
    }

    public static class Flat extends BaseMapGenerator {
        public Flat() {
            super(new MapGeneratorUri("core:flat"));
        }

        @Override
        public void setup() {
            registerChunkGenerator(new FlatTerrainGenerator());
            registerChunkGenerator(new FloraGenerator());
            registerChunkGenerator(new LiquidsGenerator());
            registerChunkGenerator(new ForestGenerator());
        }

        @Override
        public String name() {
            return "Flat";
        }
    }

    public static class Multi extends BaseMapGenerator {

        public Multi() {
            super(new MapGeneratorUri("core:multi"));
        }

        @Override
        public void setup() {
            registerChunkGenerator(new MultiTerrainGenerator());
            registerChunkGenerator(new FloraGenerator());
            registerChunkGenerator(new LiquidsGenerator());
            registerChunkGenerator(new ForestGenerator());
        }

        @Override
        public String name() {
            return "Multi";
        }
    }

    public static class Heightmap extends BaseMapGenerator {
        public Heightmap() {
            super(new MapGeneratorUri("core:heightmap"));
        }

        @Override
        public void setup() {
            registerChunkGenerator(new BasicHMTerrainGenerator());
            registerChunkGenerator(new FloraGenerator());
            registerChunkGenerator(new LiquidsGenerator());
            registerChunkGenerator(new ForestGenerator());
        }

        @Override
        public String name() {
            return "Heightmap Generator";
        }
    }

    private List<MapGenerator> generators = Lists.newArrayList();

    public MapGeneratorManager() {
        refresh();
    }

    public List<MapGenerator> listMapGenerators() {
        return generators;
    }

    public MapGenerator getMapGenerator( MapGeneratorUri uri ) {
        for (MapGenerator generator : generators) {
            if( generator.uri().equals(uri) ) {
                return generator;
            }
        }
        return null;
    }

    public void refresh() {
        generators.clear();
        Set<Class<? extends MapGenerator>> generatorClasses = CoreRegistry.get(ModManager.class).getAllReflections().getSubTypesOf(MapGenerator.class);
        for (Class<? extends MapGenerator> generatorClass : generatorClasses) {
            try {
                if(!generatorClass.isInterface() && !Modifier.isAbstract(generatorClass.getModifiers())) {
                    MapGenerator mapGenerator = generatorClass.newInstance();
                    generators.add(mapGenerator);
                    logger.info("Found map generator "+mapGenerator.uri()+" ("+mapGenerator.name()+")");
                }
            } catch (InstantiationException e) {
                logger.warn("Could not get map generator "+generatorClass.getName());
            } catch (IllegalAccessException e) {
                logger.warn("Could not get map generator " + generatorClass.getName());
            }
        }
        Collections.sort(generators, new Comparator<MapGenerator>() {
            @Override
            public int compare(MapGenerator g1, MapGenerator g2) {
                return g1.name().compareTo(g2.name());
            }
        });
    }
}
