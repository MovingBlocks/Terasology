/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.world.block.structure;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.internal.verification.Times;
import org.terasology.entitySystem.AbstractFunctionalEntityTest;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.internal.NullEntityRef;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.logic.delay.DelayManager;
import org.terasology.logic.delay.DelayedActionTriggeredEvent;
import org.terasology.logic.health.DestroyEvent;
import org.terasology.logic.inventory.InventoryAuthoritySystem;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.math.Vector3i;
import org.terasology.network.NetworkMode;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.OnChangedBlock;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.BlockManager;

public class SideBlockSupportRequiredTest extends AbstractFunctionalEntityTest {
    private SideBlockSupportRequired sideBlockSupportRequired;
    private BlockStructuralSupportSystem blockStructuralSupportSystem;
    private DelayManager delayManager;

    @Before
    public void setupTest() {
        setup(NetworkMode.DEDICATED_SERVER);

        InventoryAuthoritySystem inventoryAuthorityManager = new InventoryAuthoritySystem();
        registerComponentSystem(inventoryAuthorityManager);

        delayManager = Mockito.mock(DelayManager.class);

        CoreRegistry.put(InventoryManager.class, inventoryAuthorityManager);
        CoreRegistry.put(PrefabManager.class, Mockito.mock(PrefabManager.class));
        CoreRegistry.put(DelayManager.class, delayManager);

        Mockito.when(entityManager.create()).thenReturn(Mockito.mock(EntityRef.class));
        Mockito.when(worldProvider.isBlockRelevant(Mockito.<Vector3i>any())).thenReturn(true);

        blockStructuralSupportSystem = new BlockStructuralSupportSystem();
        sideBlockSupportRequired = new SideBlockSupportRequired();

        registerComponentSystem(blockStructuralSupportSystem);
        CoreRegistry.put(BlockStructuralSupportRegistry.class, blockStructuralSupportSystem);

        registerComponentSystem(sideBlockSupportRequired);
    }

    @Test
    public void blockGetsDestroyedWhenLostSupport() {
        SideBlockSupportRequiredComponent comp = new SideBlockSupportRequiredComponent();
        comp.bottomAllowed = true;

        EntityRef supportRequiredEntity = setupScenarioWithComponent(comp);

        OnChangedBlock onChangedBlock = new OnChangedBlock(new Vector3i(0, 1, 0), new Block(), BlockManager.getAir());
        eventSystem.send(supportRequiredEntity, onChangedBlock);

        Mockito.verify(supportRequiredEntity, new Times(1)).send(Mockito.any(DestroyEvent.class));
    }
    
    @Test
    public void blockGetsDestroyedAfterDelayWhenLostSupport() {
        SideBlockSupportRequiredComponent comp = new SideBlockSupportRequiredComponent();
        comp.bottomAllowed = true;
        comp.dropDelay = 100;

        EntityRef supportRequiredEntity = setupScenarioWithComponent(comp);

        OnChangedBlock onChangedBlock = new OnChangedBlock(new Vector3i(0, 1, 0), new Block(), BlockManager.getAir());
        eventSystem.send(supportRequiredEntity, onChangedBlock);

        Mockito.verify(supportRequiredEntity, new Times(0)).send(Mockito.any(DestroyEvent.class));
        Mockito.verify(delayManager, new Times(1)).addDelayedAction(supportRequiredEntity, "Engine:SideBlockSupportCheck", 100);

        eventSystem.send(supportRequiredEntity, new DelayedActionTriggeredEvent("Engine:SideBlockSupportCheck"));

        Mockito.verify(supportRequiredEntity, new Times(1)).send(Mockito.any(DestroyEvent.class));
    }

    private EntityRef setupScenarioWithComponent(SideBlockSupportRequiredComponent comp) {
        EntityRef entity = Mockito.mock(EntityRef.class);
        Mockito.when(entity.hasComponent(BlockComponent.class)).thenReturn(true);
        Block anyBlock = new Block();
        Block air = BlockManager.getAir();

        EntityRef supportRequiredEntity = Mockito.mock(EntityRef.class);
        Mockito.when(supportRequiredEntity.getComponent(SideBlockSupportRequiredComponent.class))
                .thenReturn(comp);
        Mockito.when(supportRequiredEntity.hasComponent(SideBlockSupportRequiredComponent.class))
                .thenReturn(true);
        final BlockComponent blockComp = new BlockComponent();
        blockComp.setPosition(new Vector3i(0, 2, 0));
        Mockito.when(supportRequiredEntity.getComponent(BlockComponent.class))
                .thenReturn(blockComp);
        Mockito.when(supportRequiredEntity.hasComponent(BlockComponent.class))
                .thenReturn(true);

        Block supportedBlock = new Block();
        supportedBlock.setEntity(supportRequiredEntity);

        setupBlock(new Vector3i(0, 0, 0), anyBlock);
        setupBlock(new Vector3i(1, 1, 0), anyBlock);
        setupBlock(new Vector3i(0, 1, 1), anyBlock);
        setupBlock(new Vector3i(-1, 1, 0), anyBlock);
        setupBlock(new Vector3i(0, 1, -1), anyBlock);
        setupBlock(new Vector3i(0, 1, 0), air);

        Vector3i location = new Vector3i(0, 2, 0);
        setupBlock(location, supportedBlock, supportRequiredEntity);

        return supportRequiredEntity;
    }

    private void setupBlock(Vector3i location, Block block) {
        setupBlock(location, block, Mockito.mock(EntityRef.class));
    }

    private void setupBlock(Vector3i location, Block block, EntityRef createdEntity) {
        Mockito.when(worldProvider.getBlock(location)).thenReturn(block);
        Mockito.when(blockEntityRegistry.getExistingBlockEntityAt(location)).thenReturn(NullEntityRef.getInstance());
        Mockito.when(blockEntityRegistry.getBlockEntityAt(location)).thenReturn(createdEntity);
    }
}
