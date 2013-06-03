/*
 * Copyright 2013 Moving Blocks
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

import org.terasology.logic.common.lifespan.LifespanComponent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.engine.CoreRegistry;
import org.terasology.physics.RigidBodyComponent;
import org.terasology.rendering.logic.MeshComponent;
import org.terasology.world.block.entity.BlockPickupComponent;
import org.terasology.world.block.family.BlockFamily;

import javax.vecmath.Vector3f;

/**
 * @author Immortius
 */
public class DroppedBlockFactory {
    private EntityManager entityManager;

    public DroppedBlockFactory(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public EntityRef newInstance(Vector3f location, BlockFamily blockFamily, float lifespan) {
        return newInstance(location, blockFamily, lifespan, EntityRef.NULL);
    }

    public EntityRef newInstance(Vector3f location, BlockFamily blockFamily, float lifespan, EntityRef placedEntity) {
        if (!blockFamily.getArchetypeBlock().isDebrisOnDestroy()) {
            return EntityRef.NULL;
        }
        Prefab prefab = CoreRegistry.get(PrefabManager.class).getPrefab("core:droppedBlock");
        if (prefab != null && prefab.getComponent(LocationComponent.class) != null) {
            EntityRef blockEntity = entityManager.create(prefab, location);

            BlockPickupComponent blockPickup = blockEntity.getComponent(BlockPickupComponent.class);
            blockPickup.blockFamily = blockFamily;
            blockPickup.placedEntity = placedEntity;
            blockEntity.saveComponent(blockPickup);

            MeshComponent blockMesh = blockEntity.getComponent(MeshComponent.class);
            blockMesh.mesh = blockFamily.getArchetypeBlock().getMesh();
            blockEntity.saveComponent(blockMesh);

            LifespanComponent lifespanComp = blockEntity.getComponent(LifespanComponent.class);
            lifespanComp.lifespan = lifespan;
            blockEntity.saveComponent(lifespanComp);

            RigidBodyComponent rigidBody = blockEntity.getComponent(RigidBodyComponent.class);
            rigidBody.mass = blockFamily.getArchetypeBlock().getMass();
            blockEntity.saveComponent(rigidBody);
            return blockEntity;
        }
        return EntityRef.NULL;
    }
}
