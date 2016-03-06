/*
 * Copyright 2015 MovingBlocks
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

import org.terasology.utilities.Assets;
import org.terasology.entitySystem.MutableComponentContainer;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnChangedComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.math.geom.Vector3f;
import org.terasology.rendering.iconmesh.IconMeshFactory;
import org.terasology.rendering.logic.LightComponent;
import org.terasology.rendering.logic.MeshComponent;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.items.BlockItemComponent;

/**
 * This system adds meshes to items that have RenderItemBlockMeshComponent or RenderItemIconMeshComponent
 */
@RegisterSystem
public class ItemCommonSystem extends BaseComponentSystem {
    private static Random rand = new FastRandom();

    @ReceiveEvent
    public void onRenderItemIconMeshActivated(OnActivatedComponent event, EntityRef item,
                                              RenderItemIconMeshComponent renderItemIconMeshComponent,
                                              ItemComponent itemComponent) {
        addOrUpdateItemMeshComponent(itemComponent, item);
    }

    @ReceiveEvent
    public void onRenderItemIconMeshChanged(OnChangedComponent event, EntityRef item,
                                            RenderItemIconMeshComponent renderItemIconMeshComponent,
                                            ItemComponent itemComponent) {
        addOrUpdateItemMeshComponent(itemComponent, item);
    }

    @ReceiveEvent
    public void onRenderItemBlockMeshActivated(OnActivatedComponent event, EntityRef item,
                                               RenderItemBlockMeshComponent renderItemBlockMeshComponent,
                                               BlockItemComponent blockItemComponent,
                                               ItemComponent itemComponent) {
        addOrUpdateBlockMeshComponent(blockItemComponent, item);
    }

    @ReceiveEvent
    public void onRenderItemBlockMeshChanged(OnChangedComponent event, EntityRef item,
                                             RenderItemBlockMeshComponent renderItemBlockMeshComponent,
                                             BlockItemComponent blockItemComponent,
                                             ItemComponent itemComponent) {
        addOrUpdateBlockMeshComponent(blockItemComponent, item);
    }

    public static void addOrUpdateItemMeshComponent(ItemComponent itemComponent, MutableComponentContainer entity) {
        if (itemComponent != null) {
            MeshComponent meshComponent = null;
            if (entity.hasComponent(MeshComponent.class)) {
                meshComponent = entity.getComponent(MeshComponent.class);
            } else {
                meshComponent = new MeshComponent();
            }
            meshComponent.material = Assets.getMaterial("engine:droppedItem").get();
            if (itemComponent.icon != null) {
                meshComponent.mesh = IconMeshFactory.getIconMesh(itemComponent.icon);
            }
            entity.addOrSaveComponent(meshComponent);
        }
    }

    public static void addOrUpdateBlockMeshComponent(BlockItemComponent blockItemComponent, MutableComponentContainer entity) {
        if (blockItemComponent != null) {
            MeshComponent meshComponent = null;
            if (entity.hasComponent(MeshComponent.class)) {
                meshComponent = entity.getComponent(MeshComponent.class);
            } else {
                meshComponent = new MeshComponent();
            }
            BlockFamily blockFamily = blockItemComponent.blockFamily;

            if (blockFamily == null) {
                return;
            }

            meshComponent.mesh = blockFamily.getArchetypeBlock().getMeshGenerator().getStandaloneMesh();
            meshComponent.material = Assets.getMaterial("engine:terrain").get();
            meshComponent.translucent = blockFamily.getArchetypeBlock().isTranslucent();

            if (blockFamily.getArchetypeBlock().getLuminance() > 0 && !entity.hasComponent(LightComponent.class)) {
                LightComponent lightComponent = entity.addComponent(new LightComponent());

                Vector3f randColor = new Vector3f(rand.nextFloat(), rand.nextFloat(), rand.nextFloat());
                lightComponent.lightColorDiffuse.set(randColor);
                lightComponent.lightColorAmbient.set(randColor);
            }

            entity.addOrSaveComponent(meshComponent);
        }
    }
}
