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

package org.terasology.logic.inventory;

import com.google.common.collect.Maps;
import org.terasology.asset.Assets;
import org.terasology.entitySystem.EntityBuilder;
import org.terasology.logic.common.lifespan.LifespanComponent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.engine.CoreRegistry;
import org.terasology.rendering.icons.Icon;
import org.terasology.physics.RigidBodyComponent;
import org.terasology.rendering.logic.MeshComponent;
import org.terasology.rendering.primitives.Mesh;
import org.terasology.rendering.primitives.MeshFactory;
import org.terasology.world.block.items.BlockItemComponent;

import javax.vecmath.Vector3f;
import java.util.Map;

/**
 * @author Adeon
 */
public class ItemPickupFactory {
    private EntityManager entityManager;
    private Map<String, Mesh> iconMeshes = Maps.newHashMap();

    public ItemPickupFactory(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public EntityRef newInstance(Vector3f location, float lifespan, EntityRef item) {
        Prefab prefab = CoreRegistry.get(PrefabManager.class).getPrefab("engine:itemPickup");
        if (prefab != null && prefab.getComponent(LocationComponent.class) != null) {
            EntityBuilder builder = entityManager.newBuilder(prefab);
            // TODO: Should just use mesh component if available, need to change the way items render
            BlockItemComponent blockItemComponent = item.getComponent(BlockItemComponent.class);
            ItemComponent itemComp = item.getComponent(ItemComponent.class);
            if (blockItemComponent != null) {
                MeshComponent mesh = builder.getComponent(MeshComponent.class);
                mesh.mesh = blockItemComponent.blockFamily.getArchetypeBlock().getMesh();
                mesh.material = Assets.getMaterial("engine:terrain");
                // TODO: Move this elsewhere? Handle it differently
                builder.getComponent(RigidBodyComponent.class).mass = blockItemComponent.blockFamily.getArchetypeBlock().getMass();
            } else {
                Mesh itemMesh = iconMeshes.get(itemComp.icon);
                if (itemMesh == null) {
                    Icon icon = Icon.get(itemComp.icon);
                    itemMesh = MeshFactory.getInstance().generateItemMesh(icon.getX(), icon.getY());
                    iconMeshes.put(itemComp.icon, itemMesh);
                }
                builder.getComponent(MeshComponent.class).mesh = itemMesh;
            }

            builder.getComponent(LocationComponent.class).setWorldPosition(location);
            builder.getComponent(LifespanComponent.class).lifespan = lifespan;
            builder.getComponent(PickupComponent.class).itemEntity = item;
            return builder.build();
        }
        return EntityRef.NULL;
    }
}
