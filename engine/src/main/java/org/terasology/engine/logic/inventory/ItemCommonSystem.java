// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.logic.inventory;

import org.terasology.engine.entitySystem.MutableComponentContainer;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.OnChangedComponent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.rendering.iconmesh.IconMeshFactory;
import org.terasology.engine.rendering.logic.LightComponent;
import org.terasology.engine.rendering.logic.MeshComponent;
import org.terasology.engine.utilities.Assets;
import org.terasology.engine.utilities.random.FastRandom;
import org.terasology.engine.utilities.random.Random;
import org.terasology.engine.world.block.family.BlockFamily;
import org.terasology.engine.world.block.items.BlockItemComponent;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;

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

            float luminance = blockFamily.getArchetypeBlock().getLuminance() / 15f;
            meshComponent.selfLuminance = luminance;
            if (luminance > 0 && !entity.hasComponent(LightComponent.class)) {
                LightComponent lightComponent = entity.addComponent(new LightComponent());
                //scale the light back if it is a less bright block
                lightComponent.lightAttenuationRange *= luminance;
            }

            entity.addOrSaveComponent(meshComponent);
        }
    }
}
