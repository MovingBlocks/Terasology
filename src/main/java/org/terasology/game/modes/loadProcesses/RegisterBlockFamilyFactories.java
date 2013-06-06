package org.terasology.game.modes.loadProcesses;

import org.terasology.game.CoreRegistry;
import org.terasology.game.modes.LoadProcess;
import org.terasology.logic.mod.Mod;
import org.terasology.logic.mod.ModManager;
import org.terasology.world.block.family.BlockFamilyFactoryRegistry;
import org.terasology.world.block.family.DefaultBlockFamilyFactoryRegistry;
import org.terasology.world.block.family.SymmetricFamilyFactory;

public class RegisterBlockFamilyFactories implements LoadProcess {
    @Override
    public String getMessage() {
        return "Registering Block Family Factories...";
    }

    @Override
    public int begin() {
        return 1;
    }

    @Override
    public boolean step() {
        DefaultBlockFamilyFactoryRegistry blockFamilyRegistry = new DefaultBlockFamilyFactoryRegistry();
        blockFamilyRegistry.setDefaultBlockFamilyFactory(new SymmetricFamilyFactory());

        ModManager modManager = CoreRegistry.get(ModManager.class);

        blockFamilyRegistry.loadBlockFamilyFactories(ModManager.ENGINE_PACKAGE, modManager.getEngineReflections());
        for (Mod mod : modManager.getActiveMods()) {
            if (mod.isCodeMod()) {
                blockFamilyRegistry.loadBlockFamilyFactories(mod.getModInfo().getId(), mod.getReflections());
            }
        }

        CoreRegistry.put(BlockFamilyFactoryRegistry.class, blockFamilyRegistry);
        return true;
    }
}
