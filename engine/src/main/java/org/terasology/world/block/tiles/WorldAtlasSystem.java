// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.block.tiles;

import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.engine.registry.In;

@RegisterSystem
public class WorldAtlasSystem extends BaseComponentSystem implements UpdateSubscriberSystem {
    @In
    private WorldAtlas worldAtlas;


    @Override
    public void update(float delta) {
        worldAtlas.update();
    }

    @Override
    public void shutdown() {
        worldAtlas.dispose();
    }
}
