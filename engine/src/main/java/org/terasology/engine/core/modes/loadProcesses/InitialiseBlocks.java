// Copyright 2026 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core.modes.loadProcesses;

import org.terasology.engine.context.Context;
import org.terasology.engine.core.modes.SingleStepLoadProcess;
import org.terasology.engine.game.GameManifest;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.block.internal.BlockManagerImpl;

public class InitialiseBlocks extends SingleStepLoadProcess {
    private final Context context;
    private final GameManifest gameManifest;

    public InitialiseBlocks(GameManifest gameManifest, Context context) {
        this.context = context;
        this.gameManifest = gameManifest;
    }

    @Override
    public String getMessage() {
        return "Initialising Blocks...";
    }

    @Override
    public boolean step() {
        BlockManager blockManager = context.get(BlockManager.class);
        if (blockManager instanceof BlockManagerImpl impl) {
            impl.initialise(gameManifest.getRegisteredBlockFamilies(), gameManifest.getBlockIdMap());
        } else {
            throw new IllegalStateException("Expected BlockManagerImpl but got "
                    + (blockManager == null ? "null" : blockManager.getClass().getName()));
        }
        return true;
    }

    @Override
    public int getExpectedCost() {
        return 1;
    }
}
