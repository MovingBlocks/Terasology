/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.logic.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.audio.events.PlaySoundEvent;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.EventPriority;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.characters.events.AttackEvent;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.ChunkMath;
import org.terasology.math.Side;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.In;
import org.terasology.utilities.Assets;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.entity.placement.PlaceBlocks;
import org.terasology.world.block.family.BlockFamily;

@RegisterSystem(RegisterMode.AUTHORITY)
public class SlabStackSystem extends BaseComponentSystem {
    private static final Logger logger = LoggerFactory.getLogger(SlabStackSystem.class);

    private static final int SLAB_PER_LAYER = 1;
    private static final int MAX_LAYERS = 3;
    private static final int MAX_SLABS = MAX_LAYERS * SLAB_PER_LAYER;
    private static final String LAYER_1_URI = "Core:Snowslab";
    private static final String LAYER_2_URI = "Core:Snowslab";
    private static final String LAYER_3_URI = "Core:Snowslab";

    @In
    private WorldProvider worldProvider;

    @ReceiveEvent(components = {SlabComponent.class, SlabStackComponent.class, BlockComponent.class})
    public void onActivate(ActivateEvent event, EntityRef entity){
        BlockComponent blockComponent = entity.getComponent(BlockComponent.class);
        Block block = blockComponent.getBlock();
    }
}
