package org.terasology.game;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.ModConfig;
import org.terasology.engine.CoreRegistry;
import org.terasology.engine.EngineTime;
import org.terasology.engine.Time;
import org.terasology.engine.paths.PathManager;
import org.terasology.logic.mod.Mod;
import org.terasology.logic.mod.ModManager;
import org.terasology.world.WorldInfo;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.management.BlockManager;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author Immortius
 */
public class Game {
    private static final Logger logger = LoggerFactory.getLogger(Game.class);

    private EngineTime time;

    private String name = "";
    private String seed = "";

    public Game(EngineTime time) {
        this.time = time;
    }

    public void load(GameManifest manifest) {
        this.name = manifest.getTitle();
        this.seed = manifest.getSeed();
        PathManager.getInstance().setCurrentSaveTitle(manifest.getTitle());
        time.setGameTime(manifest.getTime());
    }

    public void save() {
        Time time = CoreRegistry.get(Time.class);
        BlockManager blockManager = CoreRegistry.get(BlockManager.class);
        WorldProvider worldProvider = CoreRegistry.get(WorldProvider.class);

        ModConfig modConfig = new ModConfig();
        for (Mod mod : CoreRegistry.get(ModManager.class).getActiveMods()) {
            modConfig.addMod(mod.getModInfo().getId());
        }

        GameManifest gameManifest = new GameManifest(name, seed, time.getGameTimeInMs(), modConfig);
        List<String> registeredBlockFamilies = Lists.newArrayList();
        for (BlockFamily family : blockManager.listRegisteredBlockFamilies()) {
            registeredBlockFamilies.add(family.getURI().toString());
        }
        gameManifest.setRegisteredBlockFamilies(registeredBlockFamilies);
        gameManifest.setBlockIdMap(blockManager.getBlockIdMap());
        gameManifest.addWorldInfo(worldProvider.getWorldInfo());

        try {
            GameManifest.save(new File(PathManager.getInstance().getCurrentSavePath(), GameManifest.DEFAULT_FILE_NAME), gameManifest);
        } catch (IOException e) {
            logger.error("Failed to save world manifest", e);
        }
    }
}
