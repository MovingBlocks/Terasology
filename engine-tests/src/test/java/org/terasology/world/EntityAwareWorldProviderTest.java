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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.terasology.asset.AssetFactory;
import org.terasology.asset.AssetManager;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.asset.Assets;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.reflection.reflect.ReflectionReflectFactory;
import org.terasology.engine.ComponentSystemManager;
import org.terasology.registry.CoreRegistry;
import org.terasology.engine.GameThread;
import org.terasology.engine.bootstrap.EntitySystemBuilder;
import org.terasology.engine.module.ModuleManager;
import org.terasology.engine.module.ModuleManagerImpl;
import org.terasology.engine.module.ModuleSecurityManager;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.Event;
import org.terasology.entitySystem.event.internal.EventReceiver;
import org.terasology.entitySystem.event.internal.EventSystem;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.BeforeRemoveComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnAddedComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnChangedComponent;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabData;
import org.terasology.entitySystem.prefab.internal.PojoPrefab;
import org.terasology.entitySystem.stubs.ForceBlockActiveComponent;
import org.terasology.entitySystem.stubs.IntegerComponent;
import org.terasology.entitySystem.stubs.RetainedOnBlockChangeComponent;
import org.terasology.entitySystem.stubs.StringComponent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.math.Side;
import org.terasology.math.Vector3i;
import org.terasology.network.NetworkComponent;
import org.terasology.network.NetworkMode;
import org.terasology.network.NetworkSystem;
import org.terasology.testUtil.WorldProviderCoreStub;
import org.terasology.utilities.collection.NullIterator;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.family.DefaultBlockFamilyFactoryRegistry;
import org.terasology.world.block.family.HorizontalBlockFamily;
import org.terasology.world.block.family.SymmetricFamily;
import org.terasology.world.block.loader.WorldAtlas;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.internal.BlockManagerImpl;
import org.terasology.world.internal.EntityAwareWorldProvider;

import java.util.Collections;
import java.util.List;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Immortius
 */
public class EntityAwareWorldProviderTest {

    private static ModuleManager moduleManager;
    private EntityAwareWorldProvider worldProvider;
    private EngineEntityManager entityManager;
    private BlockManagerImpl blockManager;
    private WorldProviderCoreStub worldStub;

    private Block blockWithString;
    private Block blockWithDifferentString;
    private Block blockWithRetainedComponent;
    private Block keepActiveBlock;
    private Block blockInFamilyOne;
    private Block blockInFamilyTwo;

    @BeforeClass
    public static void commonSetup() {
        moduleManager = new ModuleManagerImpl(new ModuleSecurityManager());
    }

    @Before
    public void setup() {
        GameThread.setGameThread();
        AssetManager assetManager = CoreRegistry.put(AssetManager.class, new AssetManager(new ModuleManagerImpl(new ModuleSecurityManager())));
        assetManager.setAssetFactory(AssetType.PREFAB, new AssetFactory<PrefabData, Prefab>() {

            @Override
            public Prefab buildAsset(AssetUri uri, PrefabData data) {
                return new PojoPrefab(uri, data);
            }
        });
        EntitySystemBuilder builder = new EntitySystemBuilder();

        CoreRegistry.put(ComponentSystemManager.class, mock(ComponentSystemManager.class));

        blockManager = CoreRegistry.put(BlockManager.class, new BlockManagerImpl(mock(WorldAtlas.class), new DefaultBlockFamilyFactoryRegistry()));
        NetworkSystem networkSystem = mock(NetworkSystem.class);
        when(networkSystem.getMode()).thenReturn(NetworkMode.NONE);
        entityManager = builder.build(moduleManager, networkSystem, new ReflectionReflectFactory());
        worldStub = new WorldProviderCoreStub(BlockManager.getAir());
        worldProvider = new EntityAwareWorldProvider(worldStub, entityManager);

        blockWithString = new Block();
        PrefabData prefabData = new PrefabData();
        prefabData.addComponent(new StringComponent("Test"));
        Assets.generateAsset(new AssetUri(AssetType.PREFAB, "test:prefabWithString"), prefabData, Prefab.class);
        blockWithString.setPrefab("test:prefabWithString");
        blockManager.addBlockFamily(new SymmetricFamily(new BlockUri("test:blockWithString"), blockWithString), true);

        blockWithDifferentString = new Block();
        prefabData = new PrefabData();
        prefabData.addComponent(new StringComponent("Test2"));
        Assets.generateAsset(
                new AssetUri(AssetType.PREFAB, "test:prefabWithDifferentString"), prefabData, Prefab.class);
        blockWithDifferentString.setPrefab("test:prefabWithDifferentString");
        blockManager.addBlockFamily(new SymmetricFamily(new BlockUri("test:blockWithDifferentString"), blockWithDifferentString), true);

        blockWithRetainedComponent = new Block();
        prefabData = new PrefabData();
        prefabData.addComponent(new RetainedOnBlockChangeComponent(3));
        Assets.generateAsset(
                new AssetUri(AssetType.PREFAB, "test:prefabWithRetainedComponent"), prefabData, Prefab.class);
        blockWithRetainedComponent.setPrefab("test:prefabWithRetainedComponent");
        blockManager.addBlockFamily(new SymmetricFamily(new BlockUri("test:blockWithRetainedComponent"), blockWithRetainedComponent), true);

        blockInFamilyOne = new Block();
        blockInFamilyOne.setKeepActive(true);
        blockInFamilyOne.setPrefab("test:prefabWithString");
        blockInFamilyTwo = new Block();
        blockInFamilyTwo.setPrefab("test:prefabWithString");
        blockInFamilyTwo.setKeepActive(true);
        blockManager.addBlockFamily(new HorizontalBlockFamily(new BlockUri("test:blockFamily"),
                ImmutableMap.<Side, Block>of(Side.FRONT, blockInFamilyOne, Side.LEFT, blockInFamilyTwo, Side.RIGHT, blockInFamilyTwo, Side.BACK, blockInFamilyOne),
                Collections.<String>emptyList()), true);

        keepActiveBlock = new Block();
        keepActiveBlock.setKeepActive(true);
        keepActiveBlock.setPrefab("test:prefabWithString");
        blockManager.addBlockFamily(new SymmetricFamily(new BlockUri("test:keepActiveBlock"), keepActiveBlock), true);

        worldProvider.initialise();
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
        BlockFamily blockFamily = new SymmetricFamily(new BlockUri("test:keepActive"), testBlock);
        blockManager.addBlockFamily(blockFamily, true);
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

        worldProvider.setBlock(Vector3i.zero(), BlockManager.getAir());
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
        assertEquals(blockWithString.getPrefab(), worldProvider.getBlockEntityAt(new Vector3i(0, 0, 0)).getParentPrefab().getName());
        worldProvider.setBlock(Vector3i.zero(), blockWithDifferentString);
        assertEquals(blockWithDifferentString.getPrefab(), worldProvider.getBlockEntityAt(new Vector3i(0, 0, 0)).getParentPrefab().getName());
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
        worldProvider.setBlock(Vector3i.zero(), BlockManager.getAir());
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

        worldProvider.setBlock(Vector3i.zero(), BlockManager.getAir());

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

    public static class LifecycleEventChecker {
        public List<EventInfo> receivedEvents = Lists.newArrayList();

        public LifecycleEventChecker(EventSystem eventSystem, Class<? extends Component> forComponent) {
            eventSystem.registerEventReceiver(new LifecycleEventReceiver<OnAddedComponent>(), OnAddedComponent.class, forComponent);
            eventSystem.registerEventReceiver(new LifecycleEventReceiver<OnActivatedComponent>(), OnActivatedComponent.class, forComponent);
            eventSystem.registerEventReceiver(new LifecycleEventReceiver<OnChangedComponent>(), OnChangedComponent.class, forComponent);
            eventSystem.registerEventReceiver(new LifecycleEventReceiver<BeforeDeactivateComponent>(), BeforeDeactivateComponent.class, forComponent);
            eventSystem.registerEventReceiver(new LifecycleEventReceiver<BeforeRemoveComponent>(), BeforeRemoveComponent.class, forComponent);
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
