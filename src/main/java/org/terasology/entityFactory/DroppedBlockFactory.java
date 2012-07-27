/*
 * Copyright 2012
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

package org.terasology.entityFactory;

import org.terasology.components.ItemComponent;
import org.terasology.components.LightComponent;
import org.terasology.components.block.BlockItemComponent;
import org.terasology.components.rendering.MeshComponent;
import org.terasology.components.world.LocationComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.Prefab;
import org.terasology.entitySystem.PrefabManager;
import org.terasology.game.CoreRegistry;
import org.terasology.model.blocks.Block;
import org.terasology.model.blocks.BlockFamily;
import org.terasology.model.blocks.management.BlockManager;
import org.terasology.physics.BlockPickupComponent;

import javax.vecmath.Vector3f;

/**
 * @author Immortius
 */
public class DroppedBlockFactory {
    private EntityManager entityManager;

    public DroppedBlockFactory(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public EntityRef newInstance(Vector3f location, BlockFamily blockFamily) {
        return newInstance(location, blockFamily, EntityRef.NULL);
    }

    private EntityRef newInstance(Vector3f location, BlockFamily blockFamily, EntityRef placedEntity) {
        if (blockFamily.getArchetypeBlock().isTranslucent()) {
            return EntityRef.NULL;
        }
        Prefab prefab = CoreRegistry.get(PrefabManager.class).getPrefab("core:droppedBlock");
        if (prefab != null && prefab.getComponent(LocationComponent.class) != null) {
            EntityRef blockEntity = CoreRegistry.get(EntityManager.class).create(prefab, location);
            MeshComponent blockMesh = blockEntity.getComponent(MeshComponent.class);
            BlockPickupComponent blockPickup = blockEntity.getComponent(BlockPickupComponent.class);
            blockPickup.blockFamily = blockFamily;
            blockPickup.placedEntity = placedEntity;
            blockMesh.mesh = blockFamily.getArchetypeBlock().getMesh();
            blockEntity.saveComponent(blockMesh);
            blockEntity.saveComponent(blockPickup);
            return blockEntity;
        }
        return EntityRef.NULL;
    }
}
