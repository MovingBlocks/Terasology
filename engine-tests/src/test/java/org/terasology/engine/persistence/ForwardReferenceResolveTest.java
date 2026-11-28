// Copyright 2024 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.persistence;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.terasology.context.Lifetime;
import org.terasology.engine.context.Context;
import org.terasology.engine.context.internal.ContextImpl;
import org.terasology.engine.core.bootstrap.EntitySystemSetupUtil;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.internal.EntityInfoComponent;
import org.terasology.engine.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.engine.entitySystem.entity.internal.PojoEntityManager;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.OnChangedComponent;
import org.terasology.engine.entitySystem.metadata.EntitySystemLibrary;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.prefab.PrefabData;
import org.terasology.engine.entitySystem.prefab.PrefabManager;
import org.terasology.engine.entitySystem.prefab.internal.PojoPrefab;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.network.NetworkMode;
import org.terasology.engine.network.NetworkSystem;
import org.terasology.engine.network.PingComponent;
import org.terasology.engine.persistence.serializers.PrefabSerializer;
import org.terasology.engine.persistence.serializers.WorldSerializerImpl;
import org.terasology.engine.persistence.typeHandling.extensionTypes.ForwardReferenceEntityRef;
import org.terasology.engine.recording.RecordAndReplayCurrentStatus;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.testUtil.ModuleManagerFactory;
import org.terasology.engine.utilities.Assets;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.gestalt.assets.module.ModuleAwareAssetTypeManager;
import org.terasology.gestalt.assets.module.ModuleAwareAssetTypeManagerImpl;
import org.terasology.gestalt.di.ServiceRegistry;
import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;
import org.terasology.protobuf.EntityData;
import org.terasology.reflection.ModuleTypeRegistry;
import org.terasology.reflection.TypeRegistry;
import org.terasology.unittest.stubs.EntityRefComponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link ForwardReferenceEntityRef#resolveForwardReferences}: after a batch-restore,
 * any {@link ForwardReferenceEntityRef} stored in a component field must be replaced with the
 * real resolved {@link EntityRef}.
 *
 * @see <a href="https://github.com/MovingBlocks/Terasology/issues/5400">Issue #5400</a>
 */
public class ForwardReferenceResolveTest {

    private static Context baseContext;
    private static ModuleManager moduleManager;

    private EngineEntityManager entityManager;

    @BeforeAll
    public static void setupClass() throws Exception {
        ServiceRegistry serviceRegistry = new ServiceRegistry();
        RecordAndReplayCurrentStatus recordAndReplayCurrentStatus = new RecordAndReplayCurrentStatus();
        serviceRegistry.with(RecordAndReplayCurrentStatus.class).lifetime(Lifetime.Singleton)
                .use(() -> recordAndReplayCurrentStatus);
        moduleManager = ModuleManagerFactory.create();
        serviceRegistry.with(ModuleManager.class).lifetime(Lifetime.Singleton).use(() -> moduleManager);

        ModuleAwareAssetTypeManager assetTypeManager = new ModuleAwareAssetTypeManagerImpl();
        assetTypeManager.createAssetType(Prefab.class, PojoPrefab::new, "prefabs");
        assetTypeManager.switchEnvironment(moduleManager.getEnvironment());
        serviceRegistry.with(AssetManager.class).lifetime(Lifetime.Singleton)
                .use(() -> assetTypeManager.getAssetManager());

        baseContext = new ContextImpl(serviceRegistry);
        CoreRegistry.setContext(baseContext);
    }

    @BeforeEach
    public void setup() {
        entityManager = createEntityManager();
    }

    private EngineEntityManager createEntityManager() {
        NetworkSystem networkSystem = mock(NetworkSystem.class);
        when(networkSystem.getMode()).thenReturn(NetworkMode.NONE);

        ServiceRegistry serviceRegistry = new ServiceRegistry();
        serviceRegistry.with(NetworkSystem.class).lifetime(Lifetime.Singleton).use(() -> networkSystem);

        TypeRegistry typeRegistry = new ModuleTypeRegistry(moduleManager.getEnvironment());
        serviceRegistry.with(TypeRegistry.class).lifetime(Lifetime.Singleton).use(() -> typeRegistry);

        EntitySystemSetupUtil.addReflectionBasedLibraries(serviceRegistry);
        EntitySystemSetupUtil.addEntityManagementRelatedClasses(serviceRegistry);

        Context newContext = new ContextImpl(baseContext, serviceRegistry);
        CoreRegistry.setContext(newContext);

        EntitySystemSetupUtil.configureEntityManagementRelatedClasses(
                newContext.get(TypeHandlerLibrary.class),
                newContext.get(EntitySystemLibrary.class),
                moduleManager.getEnvironment(),
                newContext.get(EngineEntityManager.class));

        EngineEntityManager manager = newContext.get(EngineEntityManager.class);
        ((PojoEntityManager) manager).setPrefabManager(newContext.get(PrefabManager.class));

        manager.getComponentLibrary().register(
                new ResourceUrn("unittest", "EntityRefComponent"),
                EntityRefComponent.class);
        manager.getComponentLibrary().register(
                new ResourceUrn("engine", "Location"),
                LocationComponent.class);
        manager.getComponentLibrary().register(
                new ResourceUrn("engine", "Ping"),
                PingComponent.class);
        manager.getComponentLibrary().register(
                new ResourceUrn("unittest", "NestedHolderComponent"),
                NestedHolderComponent.class);
        return manager;
    }

    /**
     * When a {@link ForwardReferenceEntityRef} is stored in a component field, calling
     * {@link ForwardReferenceEntityRef#resolveForwardReferences} must replace it with the
     * real {@link EntityRef} once the target entity exists.
     */
    @Test
    public void resolveForwardRefReplacesProxyWithRealEntityRef() {
        // Given: two real entities
        EntityRef target = entityManager.create();
        EntityRef holder = entityManager.create();

        // A ForwardReferenceEntityRef pointing to the target's id — simulating what
        // EntityRefTypeHandler.deserialize produces for a forward reference
        ForwardReferenceEntityRef proxy = new ForwardReferenceEntityRef(entityManager, target.getId());
        holder.addComponent(new EntityRefComponent(proxy));

        // Sanity: proxy is there before fix-up
        assertInstanceOf(ForwardReferenceEntityRef.class,
                holder.getComponent(EntityRefComponent.class).entityRef,
                "Proxy should be present before resolveForwardReferences");

        // When: run the fix-up pass
        ForwardReferenceEntityRef.resolveForwardReferences(entityManager, Collections.singletonList(holder));

        // Then: the component field must now hold the real EntityRef, not the proxy
        EntityRef stored = holder.getComponent(EntityRefComponent.class).entityRef;
        assertFalse(stored instanceof ForwardReferenceEntityRef,
                "ForwardReferenceEntityRef proxy must have been replaced after fix-up");
        assertTrue(stored.exists(), "Resolved ref must point to a live entity");
        assertEquals(target.getId(), stored.getId(),
                "Resolved ref must point to the original target entity id");
    }

    /**
     * If a component field holds a {@link ForwardReferenceEntityRef} whose target does NOT
     * yet exist (genuinely dangling reference), the fix-up pass must leave it untouched.
     */
    @Test
    public void resolveForwardRefLeavesUnresolvableProxyUntouched() {
        EntityRef holder = entityManager.create();

        // A proxy for an id that does not correspond to any entity
        long nonExistentId = Long.MAX_VALUE;
        ForwardReferenceEntityRef proxy = new ForwardReferenceEntityRef(entityManager, nonExistentId);
        holder.addComponent(new EntityRefComponent(proxy));

        ForwardReferenceEntityRef.resolveForwardReferences(entityManager, Collections.singletonList(holder));

        // Proxy must remain because target doesn't exist
        assertInstanceOf(ForwardReferenceEntityRef.class,
                holder.getComponent(EntityRefComponent.class).entityRef,
                "Unresolvable ForwardReferenceEntityRef must be left in place");
    }

    /**
     * The fix-up pass must safely skip entities that no longer exist (e.g. destroyed between
     * deserialization and the fix-up call).
     */
    @Test
    public void resolveForwardRefSkipsDestroyedEntities() {
        EntityRef target = entityManager.create();
        EntityRef holder = entityManager.create();

        ForwardReferenceEntityRef proxy = new ForwardReferenceEntityRef(entityManager, target.getId());
        holder.addComponent(new EntityRefComponent(proxy));
        holder.destroy();

        // Should not throw
        ForwardReferenceEntityRef.resolveForwardReferences(entityManager, Collections.singletonList(holder));

        assertFalse(holder.exists(), "Destroyed entity must still not exist after fix-up");
    }

    /**
     * End-to-end regression test through {@link WorldSerializerImpl#deserializeWorld}: when a holder
     * entity is serialized before its target in the batch, deserialization must resolve and replace
     * the {@link ForwardReferenceEntityRef} proxy with the live {@link EntityRef}.
     */
    @Test
    public void worldSerializerResolvesForwardRefWhenHolderPrecedesTarget() {
        EntityRef holder = entityManager.create();
        EntityRef target = entityManager.create();
        long holderId = holder.getId();
        long targetId = target.getId();
        holder.addComponent(new EntityRefComponent(target));

        PrefabSerializer prefabSerializer = new PrefabSerializer(
                entityManager.getComponentLibrary(), entityManager.getTypeSerializerLibrary());
        WorldSerializerImpl sourceSerializer = new WorldSerializerImpl(entityManager, prefabSerializer);
        EntityData.GlobalStore serializedWorld = sourceSerializer.serializeWorld(false);

        List<EntityData.Entity> orderedEntities = new ArrayList<>(serializedWorld.getEntityList());
        orderedEntities.sort(Comparator.comparingInt(entity -> entity.getId() == holderId ? 0 : 1));
        EntityData.GlobalStore worldWithHolderFirst = serializedWorld.toBuilder()
                .clearEntity()
                .addAllEntity(orderedEntities)
                .build();

        EngineEntityManager freshManager = createEntityManager();
        PrefabSerializer freshPrefabSerializer = new PrefabSerializer(
                freshManager.getComponentLibrary(), freshManager.getTypeSerializerLibrary());
        WorldSerializerImpl targetSerializer = new WorldSerializerImpl(freshManager, freshPrefabSerializer);
        targetSerializer.deserializeWorld(worldWithHolderFirst);

        EntityRef loadedHolder = freshManager.getEntity(holderId);
        assertTrue(loadedHolder.exists(), "Loaded holder entity must exist");
        EntityRef storedRef = loadedHolder.getComponent(EntityRefComponent.class).entityRef;
        assertFalse(storedRef instanceof ForwardReferenceEntityRef,
                "ForwardReferenceEntityRef proxy must be replaced after WorldSerializerImpl.deserializeWorld");
        assertTrue(storedRef.exists(), "Resolved ref must point to a live entity");
        assertEquals(targetId, storedRef.getId(),
                "Resolved ref must point to the original target entity id");
    }

    @Test
    public void resolveForwardRefReplacesProxiesInCollectionsAndMapsWithoutSavingComponent() {
        List<EntityRef> changedEntities = new ArrayList<>();
        entityManager.getEventSystem().registerEventReceiver(
                (event, entity) -> changedEntities.add(entity),
                OnChangedComponent.class, LocationComponent.class);
        entityManager.getEventSystem().registerEventReceiver(
                (event, entity) -> changedEntities.add(entity),
                OnChangedComponent.class, PingComponent.class);

        EntityRef target = entityManager.create();
        EntityRef holder = entityManager.create();

        LocationComponent location = new LocationComponent();
        location.children.add(new ForwardReferenceEntityRef(entityManager, target.getId()));
        holder.addComponent(location);

        PingComponent ping = new PingComponent();
        ping.pings.put(new ForwardReferenceEntityRef(entityManager, target.getId()), 42L);
        holder.addComponent(ping);

        // Positive control: saveComponent emits OnChangedComponent; resolveForwardReferences must not.
        holder.saveComponent(ping);
        assertEquals(1, changedEntities.size());
        changedEntities.clear();

        ForwardReferenceEntityRef.resolveForwardReferences(entityManager, Collections.singletonList(holder));

        EntityRef resolvedChild = holder.getComponent(LocationComponent.class).children.get(0);
        assertFalse(resolvedChild instanceof ForwardReferenceEntityRef,
                "Proxy inside LocationComponent.children must be replaced");
        assertEquals(target.getId(), resolvedChild.getId());

        EntityRef resolvedKey = holder.getComponent(PingComponent.class).pings.keySet().iterator().next();
        assertFalse(resolvedKey instanceof ForwardReferenceEntityRef,
                "Proxy key inside PingComponent.pings must be replaced");
        assertEquals(target.getId(), resolvedKey.getId());
        assertEquals(42L, holder.getComponent(PingComponent.class).pings.get(resolvedKey));

        assertTrue(changedEntities.isEmpty(),
                "resolveForwardReferences must not fire OnChangedComponent events");
    }

    @Test
    public void resolveForwardRefHandlesNestedObjectsArraysCyclesAndImmutableLists() {
        EntityRef target = entityManager.create();
        EntityRef holder = entityManager.create();
        EntityRef secondHolder = entityManager.create();

        NestedNode root = new NestedNode();
        root.ref = new ForwardReferenceEntityRef(entityManager, target.getId());
        NestedNode cycleChild = new NestedNode();
        cycleChild.ref = new ForwardReferenceEntityRef(entityManager, target.getId());
        root.next = cycleChild;
        cycleChild.next = root;

        List<EntityRef> sharedList = new ArrayList<>();
        sharedList.add(new ForwardReferenceEntityRef(entityManager, target.getId()));

        NestedHolderComponent component = new NestedHolderComponent();
        component.nested = root;
        component.secondPath = cycleChild;
        component.array = new EntityRef[]{new ForwardReferenceEntityRef(entityManager, target.getId())};
        component.sharedList = sharedList;
        component.immutableList = List.of(new ForwardReferenceEntityRef(entityManager, target.getId()));
        holder.addComponent(component);

        NestedHolderComponent secondComponent = new NestedHolderComponent();
        secondComponent.sharedList = sharedList;
        secondHolder.addComponent(secondComponent);

        ForwardReferenceEntityRef.resolveForwardReferences(entityManager, List.of(holder, secondHolder));

        assertFalse(component.nested.ref instanceof ForwardReferenceEntityRef);
        assertEquals(target.getId(), component.nested.ref.getId());
        assertFalse(component.nested.next.ref instanceof ForwardReferenceEntityRef);
        assertEquals(target.getId(), component.nested.next.ref.getId());
        assertFalse(component.array[0] instanceof ForwardReferenceEntityRef);
        assertEquals(target.getId(), component.array[0].getId());
        assertFalse(sharedList.get(0) instanceof ForwardReferenceEntityRef);
        assertEquals(target.getId(), sharedList.get(0).getId());
        assertInstanceOf(ForwardReferenceEntityRef.class, component.immutableList.get(0),
                "Immutable list must not throw and should leave its element intact");
    }

    @Test
    public void resolveForwardRefWithPrefabBasedEntityDoesNotTraverseAssetTree() {
        PrefabData prefabData = new PrefabData();
        Prefab prefab = Assets.generateAsset(new ResourceUrn("unittest:testPrefab"), prefabData, Prefab.class);

        EntityRef target = entityManager.create();
        EntityRef holder = entityManager.create(prefab);
        assertTrue(holder.hasComponent(EntityInfoComponent.class));
        assertEquals(prefab, holder.getComponent(EntityInfoComponent.class).parentPrefab);

        ForwardReferenceEntityRef proxy = new ForwardReferenceEntityRef(entityManager, target.getId());
        holder.addComponent(new EntityRefComponent(proxy));

        ForwardReferenceEntityRef.resolveForwardReferences(entityManager, Collections.singletonList(holder));

        EntityRef resolved = holder.getComponent(EntityRefComponent.class).entityRef;
        assertFalse(resolved instanceof ForwardReferenceEntityRef, "Proxy must be resolved");
        assertEquals(target.getId(), resolved.getId());
        EntityInfoComponent info = holder.getComponent(EntityInfoComponent.class);
        assertEquals(prefab, info.parentPrefab);
    }

    @Test
    public void worldSerializerResolvesForwardRefForPrefabBasedEntity() {
        PrefabData prefabData = new PrefabData();
        Prefab prefab = Assets.generateAsset(new ResourceUrn("unittest:serializerPrefab"), prefabData, Prefab.class);

        EntityRef target = entityManager.create();
        EntityRef holder = entityManager.create(prefab);
        long holderId = holder.getId();
        long targetId = target.getId();
        holder.addComponent(new EntityRefComponent(target));

        PrefabSerializer prefabSerializer = new PrefabSerializer(
                entityManager.getComponentLibrary(), entityManager.getTypeSerializerLibrary());
        WorldSerializerImpl sourceSerializer = new WorldSerializerImpl(entityManager, prefabSerializer);
        EntityData.GlobalStore serializedWorld = sourceSerializer.serializeWorld(false);

        List<EntityData.Entity> orderedEntities = new ArrayList<>(serializedWorld.getEntityList());
        orderedEntities.sort(Comparator.comparingInt(entity -> entity.getId() == holderId ? 0 : 1));
        EntityData.GlobalStore worldWithHolderFirst = serializedWorld.toBuilder()
                .clearEntity()
                .addAllEntity(orderedEntities)
                .build();

        EngineEntityManager freshManager = createEntityManager();
        PrefabSerializer freshPrefabSerializer = new PrefabSerializer(
                freshManager.getComponentLibrary(), freshManager.getTypeSerializerLibrary());
        WorldSerializerImpl targetSerializer = new WorldSerializerImpl(freshManager, freshPrefabSerializer);
        targetSerializer.deserializeWorld(worldWithHolderFirst);

        EntityRef loadedHolder = freshManager.getEntity(holderId);
        assertTrue(loadedHolder.exists(), "Loaded holder entity must exist");
        EntityRef storedRef = loadedHolder.getComponent(EntityRefComponent.class).entityRef;
        assertFalse(storedRef instanceof ForwardReferenceEntityRef,
                "ForwardReferenceEntityRef proxy must be replaced after deserializeWorld on prefab-based entity");
        assertEquals(targetId, storedRef.getId());
        assertEquals(prefab, loadedHolder.getComponent(EntityInfoComponent.class).parentPrefab);
    }

    @Test
    public void resolveForwardRefPreservesLinkedHashMapAndLinkedHashSetOrder() {
        EntityRef t1 = entityManager.create();
        EntityRef t2 = entityManager.create();
        EntityRef t3 = entityManager.create();
        EntityRef holder = entityManager.create();

        NestedHolderComponent comp = new NestedHolderComponent();

        comp.orderedSet.add(t1);
        comp.orderedSet.add(new ForwardReferenceEntityRef(entityManager, t2.getId()));
        comp.orderedSet.add(t3);

        comp.orderedKeyMap.put(t1, "one");
        comp.orderedKeyMap.put(new ForwardReferenceEntityRef(entityManager, t2.getId()), "two");
        comp.orderedKeyMap.put(t3, "three");

        comp.orderedValueMap.put("k1", t1);
        comp.orderedValueMap.put("k2", new ForwardReferenceEntityRef(entityManager, t2.getId()));
        comp.orderedValueMap.put("k3", t3);

        holder.addComponent(comp);

        ForwardReferenceEntityRef.resolveForwardReferences(entityManager, Collections.singletonList(holder));

        NestedHolderComponent resolvedComp = holder.getComponent(NestedHolderComponent.class);

        List<EntityRef> setList = new ArrayList<>(resolvedComp.orderedSet);
        assertEquals(3, setList.size());
        assertEquals(t1, setList.get(0));
        assertEquals(t2, setList.get(1));
        assertEquals(t3, setList.get(2));
        assertFalse(setList.get(1) instanceof ForwardReferenceEntityRef);

        List<EntityRef> keyList = new ArrayList<>(resolvedComp.orderedKeyMap.keySet());
        assertEquals(3, keyList.size());
        assertEquals(t1, keyList.get(0));
        assertEquals(t2, keyList.get(1));
        assertEquals(t3, keyList.get(2));
        assertFalse(keyList.get(1) instanceof ForwardReferenceEntityRef);
        assertEquals("two", resolvedComp.orderedKeyMap.get(t2));

        List<Map.Entry<String, EntityRef>> valueEntries = new ArrayList<>(resolvedComp.orderedValueMap.entrySet());
        assertEquals(3, valueEntries.size());
        assertEquals("k1", valueEntries.get(0).getKey());
        assertEquals(t1, valueEntries.get(0).getValue());
        assertEquals("k2", valueEntries.get(1).getKey());
        assertEquals(t2, valueEntries.get(1).getValue());
        assertFalse(valueEntries.get(1).getValue() instanceof ForwardReferenceEntityRef);
        assertEquals("k3", valueEntries.get(2).getKey());
        assertEquals(t3, valueEntries.get(2).getValue());
    }

    @Test
    public void resolveForwardRefContinuesWhenMapEntrySetValueThrowsUnsupportedOperation() {
        EntityRef target1 = entityManager.create();
        EntityRef target2 = entityManager.create();
        EntityRef holder = entityManager.create();

        Map<String, EntityRef> map = Map.of(
                "first", new ForwardReferenceEntityRef(entityManager, target1.getId()),
                "second", new ForwardReferenceEntityRef(entityManager, target2.getId())
        );

        NestedHolderComponent comp = new NestedHolderComponent();
        comp.unmodifiableMap = map;
        holder.addComponent(comp);

        ForwardReferenceEntityRef.resolveForwardReferences(entityManager, Collections.singletonList(holder));

        NestedHolderComponent resolvedComp = holder.getComponent(NestedHolderComponent.class);
        assertEquals(2, resolvedComp.unmodifiableMap.size());
        assertInstanceOf(ForwardReferenceEntityRef.class, resolvedComp.unmodifiableMap.get("first"));
        assertInstanceOf(ForwardReferenceEntityRef.class, resolvedComp.unmodifiableMap.get("second"));
    }

    public static final class NestedNode {
        public EntityRef ref;
        public NestedNode next;
    }

    public static final class NestedHolderComponent implements Component<NestedHolderComponent> {
        public NestedNode nested;
        public NestedNode secondPath;
        public EntityRef[] array;
        public List<EntityRef> sharedList;
        public List<EntityRef> immutableList;
        public Set<EntityRef> orderedSet = new LinkedHashSet<>();
        public Map<EntityRef, String> orderedKeyMap = new LinkedHashMap<>();
        public Map<String, EntityRef> orderedValueMap = new LinkedHashMap<>();
        public Map<String, EntityRef> unmodifiableMap;

        @Override
        public void copyFrom(NestedHolderComponent other) {
            this.nested = other.nested;
            this.secondPath = other.secondPath;
            this.array = other.array;
            this.sharedList = other.sharedList;
            this.immutableList = other.immutableList;
            this.orderedSet = other.orderedSet != null ? new LinkedHashSet<>(other.orderedSet) : null;
            this.orderedKeyMap = other.orderedKeyMap != null ? new LinkedHashMap<>(other.orderedKeyMap) : null;
            this.orderedValueMap = other.orderedValueMap != null ? new LinkedHashMap<>(other.orderedValueMap) : null;
            this.unmodifiableMap = other.unmodifiableMap;
        }
    }
}
