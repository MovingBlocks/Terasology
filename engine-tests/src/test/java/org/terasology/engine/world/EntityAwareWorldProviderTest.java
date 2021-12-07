// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import org.joml.Vector3i;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.terasology.engine.TerasologyTestingEnvironment;
import org.terasology.engine.core.GameThread;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.BeforeRemoveComponent;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.OnAddedComponent;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.OnChangedComponent;
import org.terasology.engine.entitySystem.event.internal.EventReceiver;
import org.terasology.engine.entitySystem.event.internal.EventSystem;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.prefab.PrefabData;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.network.NetworkComponent;
import org.terasology.engine.testUtil.WorldProviderCoreStub;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockComponent;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.block.family.BlockFamily;
import org.terasology.engine.world.block.family.HorizontalFamily;
import org.terasology.engine.world.block.family.SymmetricFamily;
import org.terasology.engine.world.block.loader.BlockFamilyDefinition;
import org.terasology.engine.world.block.loader.BlockFamilyDefinitionData;
import org.terasology.engine.world.internal.EntityAwareWorldProvider;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.gestalt.entitysystem.event.Event;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;
import org.terasology.unittest.stubs.ForceBlockActiveComponent;
import org.terasology.unittest.stubs.IntegerComponent;
import org.terasology.unittest.stubs.RetainedOnBlockChangeComponent;
import org.terasology.unittest.stubs.StringComponent;

import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("TteTest")
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

    @BeforeEach
    public void setup() throws Exception {
        super.setup();
        GameThread.setToCurrentThread();

        this.entityManager = context.get(EngineEntityManager.class);
        AssetManager assetManager = context.get(AssetManager.class);
        BlockManager blockManager = context.get(BlockManager.class);

        airBlock = blockManager.getBlock(BlockManager.AIR_ID);

        worldStub = new WorldProviderCoreStub(airBlock);
        worldProvider = new EntityAwareWorldProvider(worldStub, context);

        plainBlock = createBlock("test:plainblock", assetManager, blockManager);
        prefabWithString = createPrefabWithString("test:prefabWithString", "Test", assetManager);
        blockWithString = createBlockWithPrefab("test:blockWithString", prefabWithString, false, assetManager, blockManager);
        keepActiveBlock = createBlockWithPrefab("test:keepActiveBlock", prefabWithString, true, assetManager, blockManager);
        Prefab prefabWithDifferentString = createPrefabWithString("test:prefabWithDifferentString", "Test2", assetManager);
        blockWithDifferentString = createBlockWithPrefab("test:prefabWithDifferentString", prefabWithDifferentString,
                false, assetManager, blockManager);

        BlockFamily blockFamily = createBlockFamily("test:blockFamily", prefabWithString, assetManager, blockManager);
        Iterator<Block> iterator = blockFamily.getBlocks().iterator();
        blockInFamilyOne = iterator.next();
        blockInFamilyTwo = iterator.next();

        PrefabData retainedPrefabData = new PrefabData();
        retainedPrefabData.addComponent(new RetainedOnBlockChangeComponent(3));
        Prefab retainedPrefab = assetManager.loadAsset(new ResourceUrn("test:retainedPrefab"), retainedPrefabData, Prefab.class);

        blockWithRetainedComponent = createBlockWithPrefab("test:blockWithRetainedComponent", retainedPrefab,
                false, assetManager, blockManager);
        worldProvider.initialise();
    }

    private Block createBlockWithPrefab(String urn, Prefab prefab, boolean keepActive, AssetManager assetManager, BlockManager blockManager) {
        BlockFamilyDefinitionData data = new BlockFamilyDefinitionData();
        data.setBlockFamily(SymmetricFamily.class);
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
        data.setBlockFamily(SymmetricFamily.class);
        assetManager.loadAsset(new ResourceUrn(urn), data, BlockFamilyDefinition.class);
        return blockManager.getBlock(urn);
    }

    private BlockFamily createBlockFamily(String urn, Prefab prefab, AssetManager assetManager, BlockManager blockManager) {
        BlockFamilyDefinitionData data = new BlockFamilyDefinitionData();
        data.setBlockFamily(HorizontalFamily.class);
        data.getBaseSection().getEntity().setKeepActive(true);
        data.getBaseSection().getEntity().setPrefab(prefab);
        assetManager.loadAsset(new ResourceUrn(urn), data, BlockFamilyDefinition.class);
        return blockManager.getBlockFamily(urn);
    }

    @Test
    public void testGetTemporaryBlockSendsNoEvent() {
        BlockEventChecker checker = new BlockEventChecker();
        entityManager.getEventSystem().registerEventHandler(checker);
        EntityRef blockEntity = worldProvider.getBlockEntityAt(new Vector3i());
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
        EntityRef blockEntity = worldProvider.getBlockEntityAt(new Vector3i());
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
        worldStub.setBlock(new Vector3i(), testBlock);

        BlockEventChecker checker = new BlockEventChecker();
        entityManager.getEventSystem().registerEventHandler(checker);

        EntityRef blockEntity = worldProvider.getBlockEntityAt(new Vector3i());
        worldProvider.update(1.0f);
        assertTrue(blockEntity.exists());
        assertTrue(blockEntity.isActive());
        assertTrue(checker.addedReceived);
        assertTrue(checker.activateReceived);
    }

    @Disabled("Failing due to #2625. TODO: fix to match new behaviour")
    @Test
    public void testComponentsAddedAndActivatedWhenBlockChanged() {
        LifecycleEventChecker checker = new LifecycleEventChecker(entityManager.getEventSystem(), StringComponent.class);

        worldProvider.setBlock(new Vector3i(), blockWithString);
        EntityRef blockEntity = worldProvider.getBlockEntityAt(new Vector3i());
        assertTrue(blockEntity.exists());

        assertEquals(Lists.newArrayList(new EventInfo(OnAddedComponent.newInstance(), blockEntity),
                new EventInfo(OnActivatedComponent.newInstance(), blockEntity)), checker.receivedEvents);
    }

    @Disabled("Failing due to #2625. TODO: fix to match new behaviour")
    @Test
    public void testComponentsDeactivatedAndRemovedWhenBlockChanged() {
        worldProvider.setBlock(new Vector3i(), blockWithString);

        LifecycleEventChecker checker = new LifecycleEventChecker(entityManager.getEventSystem(), StringComponent.class);

        worldProvider.setBlock(new Vector3i(), airBlock);
        EntityRef blockEntity = worldProvider.getBlockEntityAt(new Vector3i());
        assertTrue(blockEntity.exists());

        assertEquals(Lists.newArrayList(new EventInfo(BeforeDeactivateComponent.newInstance(), blockEntity),
                new EventInfo(BeforeRemoveComponent.newInstance(), blockEntity)), checker.receivedEvents);
    }

    @Disabled("Failing due to #2625. TODO: fix to match new behaviour")
    @Test
    public void testComponentsUpdatedWhenBlockChanged() {
        worldProvider.setBlock(new Vector3i(), blockWithString);

        LifecycleEventChecker checker = new LifecycleEventChecker(entityManager.getEventSystem(), StringComponent.class);

        worldProvider.setBlock(new Vector3i(), blockWithDifferentString);
        EntityRef blockEntity = worldProvider.getBlockEntityAt(new Vector3i());
        assertTrue(blockEntity.exists());

        assertEquals(Lists.newArrayList(new EventInfo(OnChangedComponent.newInstance(), blockEntity)), checker.receivedEvents);
    }

    @Test
    public void testPrefabUpdatedWhenBlockChanged() {
        worldProvider.setBlock(new Vector3i(), blockWithString);
        assertEquals(blockWithString.getPrefab().get().getName(), worldProvider.getBlockEntityAt(new Vector3i()).getParentPrefab().getName());
        worldProvider.setBlock(new Vector3i(), blockWithDifferentString);
        assertEquals(blockWithDifferentString.getPrefab().get().getName(),
                worldProvider.getBlockEntityAt(new Vector3i()).getParentPrefab().getName());
    }

    @Test
    public void testEntityNotRemovedIfForceBlockActiveComponentAdded() {
        EntityRef blockEntity = worldProvider.getBlockEntityAt(new Vector3i());
        blockEntity.addComponent(new ForceBlockActiveComponent());
        worldProvider.update(1.0f);
        assertTrue(blockEntity.exists());
        assertTrue(blockEntity.isActive());
    }

    @Disabled("Failing due to #2625. TODO: fix to match new behaviour")
    @Test
    public void testEntityCeasesToBeTemporaryIfBlockChangedToKeepActive() {
        worldProvider.setBlock(new Vector3i(), keepActiveBlock);
        worldProvider.update(1.0f);
        LifecycleEventChecker checker = new LifecycleEventChecker(entityManager.getEventSystem(), StringComponent.class);
        worldProvider.getBlockEntityAt(new Vector3i());
        assertTrue(checker.receivedEvents.isEmpty());
    }

    @Test
    public void testEntityBecomesTemporaryWhenChangedFromAKeepActiveBlock() {
        worldProvider.setBlock(new Vector3i(), keepActiveBlock);
        EntityRef blockEntity = worldProvider.getBlockEntityAt(new Vector3i());
        worldProvider.setBlock(new Vector3i(), airBlock);
        worldProvider.update(1.0f);
        assertFalse(blockEntity.isActive());
    }

    @Test
    public void testEntityBecomesTemporaryWhenChangedFromAKeepActiveBlockJoml() {
        worldProvider.setBlock(new Vector3i(), keepActiveBlock);
        EntityRef blockEntity = worldProvider.getBlockEntityAt(new Vector3i());
        worldProvider.setBlock(new Vector3i(), airBlock);
        worldProvider.update(1.0f);
        assertFalse(blockEntity.isActive());
    }


    @Test
    public void testEntityBecomesTemporaryIfForceBlockActiveComponentRemoved() {
        EntityRef blockEntity = worldProvider.getBlockEntityAt(new Vector3i());
        blockEntity.addComponent(new ForceBlockActiveComponent());
        worldProvider.update(1.0f);
        blockEntity.removeComponent(ForceBlockActiveComponent.class);
        worldProvider.update(1.0f);
        assertFalse(blockEntity.exists());
        assertFalse(blockEntity.isActive());
    }

    @Test
    public void testEntityExtraComponentsRemovedBeforeCleanUp() {
        EntityRef entity = worldProvider.getBlockEntityAt(new Vector3i());
        entity.addComponent(new StringComponent("test"));

        LifecycleEventChecker checker = new LifecycleEventChecker(entityManager.getEventSystem(), StringComponent.class);

        worldProvider.update(1.0f);
        assertEquals(Lists.newArrayList(new EventInfo(BeforeDeactivateComponent.newInstance(), entity),
                new EventInfo(BeforeRemoveComponent.newInstance(), entity)), checker.receivedEvents);
    }

    @Test
    public void testEntityExtraComponentsRemovedBeforeCleanUpForBlocksWithPrefabs() {
        worldStub.setBlock(new Vector3i(), blockWithString);
        EntityRef entity = worldProvider.getBlockEntityAt(new Vector3i());
        entity.addComponent(new IntegerComponent(1));

        LifecycleEventChecker checker = new LifecycleEventChecker(entityManager.getEventSystem(), IntegerComponent.class);

        worldProvider.update(1.0f);
        assertEquals(Lists.newArrayList(new EventInfo(BeforeDeactivateComponent.newInstance(), entity),
                new EventInfo(BeforeRemoveComponent.newInstance(), entity)), checker.receivedEvents);
    }

    @Test
    public void testEntityMissingComponentsAddedBeforeCleanUp() {
        worldStub.setBlock(new Vector3i(), blockWithString);
        EntityRef entity = worldProvider.getBlockEntityAt(new Vector3i());
        entity.removeComponent(StringComponent.class);

        LifecycleEventChecker checker = new LifecycleEventChecker(entityManager.getEventSystem(), StringComponent.class);

        worldProvider.update(1.0f);
        assertEquals(Lists.newArrayList(new EventInfo(OnAddedComponent.newInstance(), entity),
                new EventInfo(OnActivatedComponent.newInstance(), entity)), checker.receivedEvents);
    }

    @Test
    public void testChangedComponentsRevertedBeforeCleanUp() {
        worldStub.setBlock(new Vector3i(), blockWithString);
        EntityRef entity = worldProvider.getBlockEntityAt(new Vector3i());
        StringComponent comp = entity.getComponent(StringComponent.class);
        comp.value = "Moo";
        entity.saveComponent(comp);

        LifecycleEventChecker checker = new LifecycleEventChecker(entityManager.getEventSystem(), StringComponent.class);

        worldProvider.update(1.0f);
        assertEquals(Lists.newArrayList(new EventInfo(OnChangedComponent.newInstance(), entity)), checker.receivedEvents);
    }

    @Test
    public void testAllComponentsNotMarkedAsRetainedRemovedOnBlockChange() {
        worldStub.setBlock(new Vector3i(), blockWithString);
        EntityRef entity = worldProvider.getBlockEntityAt(new Vector3i());
        entity.addComponent(new ForceBlockActiveComponent());
        entity.addComponent(new RetainedOnBlockChangeComponent(2));

        worldProvider.setBlock(new Vector3i(), airBlock);

        assertTrue(entity.hasComponent(RetainedOnBlockChangeComponent.class));
        assertFalse(entity.hasComponent(ForceBlockActiveComponent.class));
    }

    @Test
    public void testRetainedComponentsNotAltered() {
        EntityRef entity = worldProvider.getBlockEntityAt(new Vector3i());
        entity.addComponent(new RetainedOnBlockChangeComponent(2));

        worldProvider.setBlock(new Vector3i(), blockWithRetainedComponent);

        assertEquals(2, entity.getComponent(RetainedOnBlockChangeComponent.class).value);
    }

    @Test
    public void testMetworkComponentAddedWhenChangedToNonTemporary() {
        LifecycleEventChecker checker = new LifecycleEventChecker(entityManager.getEventSystem(), NetworkComponent.class);
        EntityRef entity = worldProvider.getBlockEntityAt(new Vector3i());
        entity.addComponent(new RetainedOnBlockChangeComponent(2));

        assertEquals(Lists.newArrayList(new EventInfo(OnAddedComponent.newInstance(), entity),
                new EventInfo(OnActivatedComponent.newInstance(), entity)), checker.receivedEvents);
        assertTrue(entity.hasComponent(NetworkComponent.class));
    }

    @Test
    public void testNetworkComponentRemovedWhenTemporaryCleanedUp() {
        EntityRef entity = worldProvider.getBlockEntityAt(new Vector3i());
        entity.addComponent(new RetainedOnBlockChangeComponent(2));

        LifecycleEventChecker checker = new LifecycleEventChecker(entityManager.getEventSystem(), NetworkComponent.class);
        entity.removeComponent(RetainedOnBlockChangeComponent.class);

        worldProvider.update(1.0f);

        assertEquals(Lists.newArrayList(new EventInfo(BeforeDeactivateComponent.newInstance(), entity),
                new EventInfo(BeforeRemoveComponent.newInstance(), entity)), checker.receivedEvents);
    }

    @Test
    public void testComponentsNotAlteredIfBlockInSameFamily() {
        worldProvider.setBlock(new Vector3i(), blockInFamilyOne);
        EntityRef entity = worldProvider.getBlockEntityAt(new Vector3i());
        entity.addComponent(new IntegerComponent());
        worldProvider.setBlock(new Vector3i(), blockInFamilyTwo);
        assertNotNull(entity.getComponent(IntegerComponent.class));
    }

    @Test
    public void testComponentsAlteredIfBlockInSameFamilyWhenForced() {
        worldProvider.setBlock(new Vector3i(), blockInFamilyOne);
        EntityRef entity = worldProvider.getBlockEntityAt(new Vector3i());
        entity.addComponent(new IntegerComponent());
        worldProvider.setBlockForceUpdateEntity(new Vector3i(), blockInFamilyTwo);
        assertNull(entity.getComponent(IntegerComponent.class));
    }

    @Test
    public void testComponentUntouchedIfRetainRequested() {
        worldProvider.setBlock(new Vector3i(), blockInFamilyOne);
        EntityRef entity = worldProvider.getBlockEntityAt(new Vector3i());
        entity.addComponent(new IntegerComponent());
        worldProvider.setBlockRetainComponent(new Vector3i(), blockWithString, IntegerComponent.class);
        assertNotNull(entity.getComponent(IntegerComponent.class));
    }

    @Disabled("Failing due to #2625. TODO: fix to match new behaviour")
    @Test
    public void testBlockEntityPrefabCorrectlyAlteredOnChangeToDifferentPrefab() {
        worldProvider.setBlock(new Vector3i(), blockWithString);
        EntityRef entity = worldProvider.getBlockEntityAt(new Vector3i());
        worldProvider.setBlock(new Vector3i(), blockWithDifferentString);
        assertEquals(blockWithDifferentString.getPrefab().get().getUrn(), entity.getParentPrefab().getUrn());
    }

    @Disabled("Failing due to #2625. TODO: fix to match new behaviour")
    @Test
    public void testBlockEntityPrefabCorrectlyRemovedOnChangeToBlockWithNoPrefab() {
        worldProvider.setBlock(new Vector3i(), blockWithString);
        EntityRef entity = worldProvider.getBlockEntityAt(new Vector3i());
        worldProvider.setBlock(new Vector3i(), plainBlock);
        assertEquals(null, entity.getParentPrefab());
    }

    @Disabled("Failing due to #2625. TODO: fix to match new behaviour")
    @Test
    public void testBlockEntityPrefabCorrectlyAddedOnChangeToBlockWithPrefab() {
        worldProvider.setBlock(new Vector3i(), plainBlock);
        EntityRef entity = worldProvider.getBlockEntityAt(new Vector3i());
        worldProvider.setBlock(new Vector3i(), blockWithString);
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
