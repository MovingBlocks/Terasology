package org.terasology.world.generator;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.game.CoreRegistry;
import org.terasology.game.types.GameType;
import org.terasology.logic.mod.Mod;
import org.terasology.logic.mod.ModAwareManager;
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
public class MapGeneratorManager extends ModAwareManager<MapGenerator, MapGeneratorUri> {
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

    public static class Hills extends BaseMapGenerator {
        public Hills() {
            super(new MapGeneratorUri("core:hills"));
        }

        @Override
        public String name() {
            return "Hills";
        }

        @Override
        public void setup() {
            registerChunkGenerator(new HillsTerrainGenerator());
            registerChunkGenerator(new FloraGenerator());
            registerChunkGenerator(new LiquidsGenerator());
            registerChunkGenerator(new ForestGenerator());
        }
    }

    @Override
    protected MapGeneratorUri getUri(MapGenerator item) {
        return item.uri();
    }

    @Override
    protected List<Class> getClasses() {
        return Arrays.asList((Class) MapGenerator.class, BaseMapGenerator.class);
    }

    @Override
    protected Comparator<MapGenerator> getItemComparator() {
        return new Comparator<MapGenerator>() {
            @Override
            public int compare(MapGenerator o1, MapGenerator o2) {
                return o1.name().compareTo(o2.name());
            }
        };
    }
}
