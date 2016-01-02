/*
 * Copyright 2013 MovingBlocks
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

package org.terasology.world;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.terasology.TerasologyTestingEnvironment;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.management.AssetManager;
import org.terasology.engine.GameThread;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.BeforeRemoveComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnAddedComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnChangedComponent;
import org.terasology.entitySystem.event.Event;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.event.internal.EventReceiver;
import org.terasology.entitySystem.event.internal.EventSystem;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabData;
import org.terasology.entitySystem.stubs.ForceBlockActiveComponent;
import org.terasology.entitySystem.stubs.IntegerComponent;
import org.terasology.entitySystem.stubs.RetainedOnBlockChangeComponent;
import org.terasology.entitySystem.stubs.StringComponent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.math.geom.Vector3i;
import org.terasology.network.NetworkComponent;
import org.terasology.testUtil.WorldProviderCoreStub;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.family.HorizontalBlockFamilyFactory;
import org.terasology.world.block.family.SymmetricBlockFamilyFactory;
import org.terasology.world.block.loader.BlockFamilyDefinition;
import org.terasology.world.block.loader.BlockFamilyDefinitionData;
import org.terasology.world.internal.EntityAwareWorldProvider;

import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 */
public class EntityAwareWorldProviderTest extends TerasologyTestingEnvironment {

    private EntityAwareWorldProvider worldProvider;
    private WorldProviderCoreStub worldStub;

    private EngineEntityManager entityManager;
    private Prefab prefabWithString;

    private Block airBlock;
    private Block plainBlock;
    private Block blockWithString;
    private Block blockWithDifferentString;
    private Block blockWithRetainedComponent;
    private Block keepActiveBlock;
    private Block blockInFamilyOne;
    private Block blockInFamilyTwo;

    @Before
    public void setup() throws Exception {
        super.setup();
        GameThread.setToCurrentThread();

        this.entityManager = context.get(EngineEntityManager.class);
        AssetManager assetManager = context.get(AssetManager.class);
        BlockManager blockManager = context.get(BlockManager.class);

        airBlock = blockManager.getBlock(BlockManager.AIR_ID);

        worldStub = new WorldProviderCoreStub(airBlock, null);
        worldProvider = new EntityAwareWorldProvider(worldStub, context);

        plainBlock = createBlock("test:plainblock", assetManager, blockManager);
        prefabWithString = createPrefabWithString("test:prefabWithString", "Test", assetManager);
        blockWithString = createBlockWithPrefab("test:blockWithString", prefabWithString, false, assetManager, blockManager);
        keepActiveBlock = createBlockWithPrefab("test:keepActiveBlock", prefabWithString, true, assetManager, blockManager);
        Prefab prefabWithDifferentString = createPrefabWithString("test:prefabWithDifferentString", "Test2", assetManager);
        blockWithDifferentString = createBlockWithPrefab("test:prefabWithDifferentString", prefabWithDifferentString, false, assetManager, blockManager);

        BlockFamily blockFamily = createBlockFamily("test:blockFamily", prefabWithString, assetManager, blockManager);
        Iterator<Block> iterator = blockFamily.getBlocks().iterator();
        blockInFamilyOne = iterator.next();
        blockInFamilyTwo = iterator.next();

        PrefabData retainedPrefabData = new PrefabData();
        retainedPrefabData.addComponent(new RetainedOnBlockChangeComponent(3));
        Prefab retainedPrefab = assetManager.loadAsset(new ResourceUrn("test:retainedPrefab"), retainedPrefabData, Prefab.class);

        blockWithRetainedComponent = createBlockWithPrefab("test:blockWithRetainedComponent", retainedPrefab, false, assetManager, blockManager);
        worldProvider.initialise();
    }

    private Block createBlockWithPrefab(String urn, Prefab prefab, boolean keepActive, AssetManager assetManager, BlockManager blockManager) {
        BlockFamilyDefinitionData data = new BlockFamilyDefinitionData();
        data.setFamilyFactory(new SymmetricBlockFamilyFactory());
        data.getBaseSection().getEntity().setPrefab(prefab);
        data.getBaseSection().getEntity().setKeepActive(keepActive);
        assetManager.loadAsset(new ResourceUrn(urn), data, BlockFamilyDefinition.class);
        return blockManager.getBlock(urn);
    }

    private Prefab createPrefabWithString(String urn, String text, AssetManager assetManager) {
        PrefabData prefabData = new PrefabData();
        prefabData.addComponent(new StringComponent(text));
        return assetManager.loadAsset(new ResourceUrn(urn), prefabData, Prefab.class);
    }

    private Block createBlock(String urn, AssetManager assetManager, BlockManager blockManager) {
        BlockFamilyDefinitionData data = new BlockFamilyDefinitionData();
        data.setFamilyFactory(new SymmetricBlockFamilyFactory());
        assetManager.loadAsset(new ResourceUrn(urn), data, BlockFamilyDefinition.class);
        return blockManager.getBlock(urn);
    }

    private BlockFamily createBlockFamily(String urn, Prefab prefab, AssetManager assetManager, BlockManager blockManager) {
        BlockFamilyDefinitionData data = new BlockFamilyDefinitionData();
        data.setFamilyFactory(new HorizontalBlockFamilyFactory());
        data.getBaseSection().getEntity().setKeepActive(true);
        data.getBaseSection().getEntity().setPrefab(prefab);
        assetManager.loadAsset(new ResourceUrn(urn), data, BlockFamilyDefinition.class);
        return blockManager.getBlockFamily(urn);
    }

    @Test
    public void testGetTemporaryBlockSendsNoEvent() {
        BlockEventChecker checker = new BlockEventChecker();
        entityManager.getEventSystem().registerEventHandler(checker);
        EntityRef blockEntity = worldProvider.getBlockEntityAt(new Vector3i(0, 0, 0));
        assertTrue(blockEntity.exists());
        assertFalse(checker.addedReceived);
        assertFalse(checker.activateReceived);
        assertFalse(checker.deactivateReceived);
        assertFalse(checker.removedReceived);
    }

    @Test
    public void testTemporaryCleanedUpWithNoEvent() {
        BlockEventChecker checker = new BlockEventChecker();
        entityManager.getEventSystem().registerEventHandler(checker);
        EntityRef blockEntity = worldProvider.getBlockEntityAt(new Vector3i(0, 0, 0));
        worldProvider.update(1.0f);
        assertFalse(blockEntity.exists());
        assertFalse(checker.addedReceived);
        assertFalse(checker.activateReceived);
        assertFalse(checker.deactivateReceived);
        assertFalse(checker.removedReceived);
    }

    @Test
    public void testActiveBlockNotCleanedUp() {
        Block testBlock = new Block();
        testBlock.setKeepActive(true);
        // BlockFamily blockFamily = new SymmetricFamily(new BlockUri("test:keepActive"), testBlock);
        //blockManager.addBlockFamily(blockFamily, true);
        worldStub.setBlock(Vector3i.zero(), testBlock);

        BlockEventChecker checker = new BlockEventChecker();
        entityManager.getEventSystem().registerEventHandler(checker);

        EntityRef blockEntity = worldProvider.getBlockEntityAt(new Vector3i(0, 0, 0));
        worldProvider.update(1.0f);
        assertTrue(blockEntity.exists());
        assertTrue(blockEntity.isActive());
        assertTrue(checker.addedReceived);
        assertTrue(checker.activateReceived);
    }

    @Test
    public void testComponentsAddedAndActivatedWhenBlockChanged() {
        LifecycleEventChecker checker = new LifecycleEventChecker(entityManager.getEventSystem(), StringComponent.class);

        worldProvider.setBlock(Vector3i.zero(), blockWithString);
        EntityRef blockEntity = worldProvider.getBlockEntityAt(Vector3i.zero());
        assertTrue(blockEntity.exists());

        assertEquals(Lists.newArrayList(new EventInfo(OnAddedComponent.newInstance(), blockEntity), new EventInfo(OnActivatedComponent.newInstance(), blockEntity)),
                checker.receivedEvents);
    }

    @Test
    public void testComponentsDeactivatedAndRemovedWhenBlockChanged() {
        worldProvider.setBlock(Vector3i.zero(), blockWithString);

        LifecycleEventChecker checker = new LifecycleEventChecker(entityManager.getEventSystem(), StringComponent.class);

        worldProvider.setBlock(Vector3i.zero(), airBlock);
        EntityRef blockEntity = worldProvider.getBlockEntityAt(new Vector3i(0, 0, 0));
        assertTrue(blockEntity.exists());

        assertEquals(Lists.newArrayList(new EventInfo(BeforeDeactivateComponent.newInstance(), blockEntity), new EventInfo(BeforeRemoveComponent.newInstance(), blockEntity)),
                checker.receivedEvents);
    }

    @Test
    public void testComponentsUpdatedWhenBlockChanged() {
        worldProvider.setBlock(Vector3i.zero(), blockWithString);

        LifecycleEventChecker checker = new LifecycleEventChecker(entityManager.getEventSystem(), StringComponent.class);

        worldProvider.setBlock(Vector3i.zero(), blockWithDifferentString);
        EntityRef blockEntity = worldProvider.getBlockEntityAt(new Vector3i(0, 0, 0));
        assertTrue(blockEntity.exists());

        assertEquals(Lists.newArrayList(new EventInfo(OnChangedComponent.newInstance(), blockEntity)), checker.receivedEvents);
    }

    @Test
    public void testPrefabUpdatedWhenBlockChanged() {
        worldProvider.setBlock(Vector3i.zero(), blockWithString);
        assertEquals(blockWithString.getPrefab().get().getName(), worldProvider.getBlockEntityAt(new Vector3i(0, 0, 0)).getParentPrefab().getName());
        worldProvider.setBlock(Vector3i.zero(), blockWithDifferentString);
        assertEquals(blockWithDifferentString.getPrefab().get().getName(), worldProvider.getBlockEntityAt(new Vector3i(0, 0, 0)).getParentPrefab().getName());
    }

    @Test
    public void testEntityNotRemovedIfForceBlockActiveComponentAdded() {
        EntityRef blockEntity = worldProvider.getBlockEntityAt(new Vector3i(0, 0, 0));
        blockEntity.addComponent(new ForceBlockActiveComponent());
        worldProvider.update(1.0f);
        assertTrue(blockEntity.exists());
        assertTrue(blockEntity.isActive());
    }


    @Test
    public void testEntityCeasesToBeTemporaryIfBlockChangedToKeepActive() {
        worldProvider.setBlock(Vector3i.zero(), keepActiveBlock);
        worldProvider.update(1.0f);
        LifecycleEventChecker checker = new LifecycleEventChecker(entityManager.getEventSystem(), StringComponent.class);
        worldProvider.getBlockEntityAt(new Vector3i(0, 0, 0));
        assertTrue(checker.receivedEvents.isEmpty());
    }

    @Test
    public void testEntityBecomesTemporaryWhenChangedFromAKeepActiveBlock() {
        worldProvider.setBlock(Vector3i.zero(), keepActiveBlock);
        EntityRef blockEntity = worldProvider.getBlockEntityAt(new Vector3i(0, 0, 0));
        worldProvider.setBlock(Vector3i.zero(), airBlock);
        worldProvider.update(1.0f);
        assertFalse(blockEntity.isActive());
    }

    @Test
    public void testEntityBecomesTemporaryIfForceBlockActiveComponentRemoved() {
        EntityRef blockEntity = worldProvider.getBlockEntityAt(new Vector3i(0, 0, 0));
        blockEntity.addComponent(new ForceBlockActiveComponent());
        worldProvider.update(1.0f);
        blockEntity.removeComponent(ForceBlockActiveComponent.class);
        worldProvider.update(1.0f);
        assertFalse(blockEntity.exists());
        assertFalse(blockEntity.isActive());
    }

    @Test
    public void testEntityExtraComponentsRemovedBeforeCleanUp() {
        EntityRef entity = worldProvider.getBlockEntityAt(new Vector3i(0, 0, 0));
        entity.addComponent(new StringComponent("test"));

        LifecycleEventChecker checker = new LifecycleEventChecker(entityManager.getEventSystem(), StringComponent.class);

        worldProvider.update(1.0f);
        assertEquals(Lists.newArrayList(new EventInfo(BeforeDeactivateComponent.newInstance(), entity), new EventInfo(BeforeRemoveComponent.newInstance(), entity)),
                checker.receivedEvents);
    }

    @Test
    public void testEntityExtraComponentsRemovedBeforeCleanUpForBlocksWithPrefabs() {
        worldStub.setBlock(Vector3i.zero(), blockWithString);
        EntityRef entity = worldProvider.getBlockEntityAt(new Vector3i(0, 0, 0));
        entity.addComponent(new IntegerComponent(1));

        LifecycleEventChecker checker = new LifecycleEventChecker(entityManager.getEventSystem(), IntegerComponent.class);

        worldProvider.update(1.0f);
        assertEquals(Lists.newArrayList(new EventInfo(BeforeDeactivateComponent.newInstance(), entity), new EventInfo(BeforeRemoveComponent.newInstance(), entity)),
                checker.receivedEvents);
    }

    @Test
    public void testEntityMissingComponentsAddedBeforeCleanUp() {
        worldStub.setBlock(Vector3i.zero(), blockWithString);
        EntityRef entity = worldProvider.getBlockEntityAt(new Vector3i(0, 0, 0));
        entity.removeComponent(StringComponent.class);

        LifecycleEventChecker checker = new LifecycleEventChecker(entityManager.getEventSystem(), StringComponent.class);

        worldProvider.update(1.0f);
        assertEquals(Lists.newArrayList(new EventInfo(OnAddedComponent.newInstance(), entity), new EventInfo(OnActivatedComponent.newInstance(), entity)),
                checker.receivedEvents);
    }

    @Test
    public void testChangedComponentsRevertedBeforeCleanUp() {
        worldStub.setBlock(Vector3i.zero(), blockWithString);
        EntityRef entity = worldProvider.getBlockEntityAt(new Vector3i(0, 0, 0));
        StringComponent comp = entity.getComponent(StringComponent.class);
        comp.value = "Moo";
        entity.saveComponent(comp);

        LifecycleEventChecker checker = new LifecycleEventChecker(entityManager.getEventSystem(), StringComponent.class);

        worldProvider.update(1.0f);
        assertEquals(Lists.newArrayList(new EventInfo(OnChangedComponent.newInstance(), entity)), checker.receivedEvents);
    }

    @Test
    public void allComponentsNotMarkedAsRetainedRemovedOnBlockChange() {
        worldStub.setBlock(Vector3i.zero(), blockWithString);
        EntityRef entity = worldProvider.getBlockEntityAt(new Vector3i(0, 0, 0));
        entity.addComponent(new ForceBlockActiveComponent());
        entity.addComponent(new RetainedOnBlockChangeComponent(2));

        worldProvider.setBlock(Vector3i.zero(), airBlock);

        assertTrue(entity.hasComponent(RetainedOnBlockChangeComponent.class));
        assertFalse(entity.hasComponent(ForceBlockActiveComponent.class));
    }

    @Test
    public void retainedComponentsNotAltered() {
        EntityRef entity = worldProvider.getBlockEntityAt(new Vector3i(0, 0, 0));
        entity.addComponent(new RetainedOnBlockChangeComponent(2));

        worldProvider.setBlock(Vector3i.zero(), blockWithRetainedComponent);

        assertEquals(2, entity.getComponent(RetainedOnBlockChangeComponent.class).value);
    }

    @Test
    public void networkComponentAddedWhenChangedToNonTemporary() {
        LifecycleEventChecker checker = new LifecycleEventChecker(entityManager.getEventSystem(), NetworkComponent.class);
        EntityRef entity = worldProvider.getBlockEntityAt(new Vector3i(0, 0, 0));
        entity.addComponent(new RetainedOnBlockChangeComponent(2));

        assertEquals(Lists.newArrayList(new EventInfo(OnAddedComponent.newInstance(), entity), new EventInfo(OnActivatedComponent.newInstance(), entity)),
                checker.receivedEvents);
        assertTrue(entity.hasComponent(NetworkComponent.class));
    }

    @Test
    public void networkComponentRemovedWhenTemporaryCleanedUp() {
        EntityRef entity = worldProvider.getBlockEntityAt(new Vector3i(0, 0, 0));
        entity.addComponent(new RetainedOnBlockChangeComponent(2));

        LifecycleEventChecker checker = new LifecycleEventChecker(entityManager.getEventSystem(), NetworkComponent.class);
        entity.removeComponent(RetainedOnBlockChangeComponent.class);

        worldProvider.update(1.0f);

        assertEquals(Lists.newArrayList(new EventInfo(BeforeDeactivateComponent.newInstance(), entity), new EventInfo(BeforeRemoveComponent.newInstance(), entity)),
                checker.receivedEvents);
    }

    @Test
    public void componentsNotAlteredIfBlockInSameFamily() {
        worldProvider.setBlock(Vector3i.zero(), blockInFamilyOne);
        EntityRef entity = worldProvider.getBlockEntityAt(Vector3i.zero());
        entity.addComponent(new IntegerComponent());
        worldProvider.setBlock(Vector3i.zero(), blockInFamilyTwo);
        assertNotNull(entity.getComponent(IntegerComponent.class));
    }

    @Test
    public void componentsAlteredIfBlockInSameFamilyWhenForced() {
        worldProvider.setBlock(Vector3i.zero(), blockInFamilyOne);
        EntityRef entity = worldProvider.getBlockEntityAt(Vector3i.zero());
        entity.addComponent(new IntegerComponent());
        worldProvider.setBlockForceUpdateEntity(Vector3i.zero(), blockInFamilyTwo);
        assertNull(entity.getComponent(IntegerComponent.class));
    }

    @Test
    public void componentUntouchedIfRetainRequested() {
        worldProvider.setBlock(Vector3i.zero(), blockInFamilyOne);
        EntityRef entity = worldProvider.getBlockEntityAt(Vector3i.zero());
        entity.addComponent(new IntegerComponent());
        worldProvider.setBlockRetainComponent(Vector3i.zero(), blockWithString, IntegerComponent.class);
        assertNotNull(entity.getComponent(IntegerComponent.class));
    }


    @Test
    public void testBlockEntityPrefabCorrectlyAlteredOnChangeToDifferentPrefab() {
        worldProvider.setBlock(Vector3i.zero(), blockWithString);
        EntityRef entity = worldProvider.getBlockEntityAt(Vector3i.zero());
        worldProvider.setBlock(Vector3i.zero(), blockWithDifferentString);
        assertEquals(blockWithDifferentString.getPrefab().get().getUrn(), entity.getParentPrefab().getUrn());
    }

    @Test
    public void testBlockEntityPrefabCorrectlyRemovedOnChangeToBlockWithNoPrefab() {
        worldProvider.setBlock(Vector3i.zero(), blockWithString);
        EntityRef entity = worldProvider.getBlockEntityAt(Vector3i.zero());
        worldProvider.setBlock(Vector3i.zero(), plainBlock);
        assertEquals(null, entity.getParentPrefab());
    }

    @Test
    public void testBlockEntityPrefabCorrectlyAddedOnChangeToBlockWithPrefab() {
        worldProvider.setBlock(Vector3i.zero(), plainBlock);
        EntityRef entity = worldProvider.getBlockEntityAt(Vector3i.zero());
        worldProvider.setBlock(Vector3i.zero(), blockWithString);
        assertEquals(blockWithString.getPrefab().get().getUrn().toString(), entity.getParentPrefab().getUrn().toString());
    }

    public static class LifecycleEventChecker {
        public List<EventInfo> receivedEvents = Lists.newArrayList();

        public LifecycleEventChecker(EventSystem eventSystem, Class<? extends Component> forComponent) {
            eventSystem.registerEventReceiver(new LifecycleEventReceiver<>(), OnAddedComponent.class, forComponent);
            eventSystem.registerEventReceiver(new LifecycleEventReceiver<>(), OnActivatedComponent.class, forComponent);
            eventSystem.registerEventReceiver(new LifecycleEventReceiver<>(), OnChangedComponent.class, forComponent);
            eventSystem.registerEventReceiver(new LifecycleEventReceiver<>(), BeforeDeactivateComponent.class, forComponent);
            eventSystem.registerEventReceiver(new LifecycleEventReceiver<>(), BeforeRemoveComponent.class, forComponent);
        }

        private class LifecycleEventReceiver<T extends Event> implements EventReceiver<T> {

            @Override
            public void onEvent(T event, EntityRef entity) {
                receivedEvents.add(new EventInfo(event, entity));
            }
        }
    }

    public static class BlockEventChecker extends BaseComponentSystem {

        public boolean addedReceived;
        public boolean activateReceived;
        public boolean deactivateReceived;
        public boolean removedReceived;

        @ReceiveEvent(components = BlockComponent.class)
        public void onAdded(OnAddedComponent event, EntityRef entity) {
            addedReceived = true;
        }

        @ReceiveEvent(components = BlockComponent.class)
        public void onActivated(OnActivatedComponent event, EntityRef entity) {
            activateReceived = true;
        }

        @ReceiveEvent(components = BlockComponent.class)
        public void onDeactivated(BeforeDeactivateComponent event, EntityRef entity) {
            deactivateReceived = true;
        }

        @ReceiveEvent(components = BlockComponent.class)
        public void onRemoved(BeforeRemoveComponent event, EntityRef entity) {
            removedReceived = true;
        }
    }

    public static class EventInfo {
        public EntityRef targetEntity;
        public Event event;

        public EventInfo(Event event, EntityRef target) {
            this.event = event;
            this.targetEntity = target;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj instanceof EventInfo) {
                EventInfo other = (EventInfo) obj;
                return Objects.equal(other.targetEntity, targetEntity) && Objects.equal(other.event, event);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(targetEntity, event);
        }
    }
}
