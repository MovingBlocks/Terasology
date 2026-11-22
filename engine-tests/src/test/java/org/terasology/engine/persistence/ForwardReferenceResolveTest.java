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
import org.terasology.engine.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.engine.entitySystem.entity.internal.PojoEntityManager;
import org.terasology.engine.entitySystem.metadata.EntitySystemLibrary;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.prefab.PrefabManager;
import org.terasology.engine.network.NetworkMode;
import org.terasology.engine.network.NetworkSystem;
import org.terasology.engine.persistence.typeHandling.extensionTypes.ForwardReferenceEntityRef;
import org.terasology.engine.recording.RecordAndReplayCurrentStatus;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.testUtil.ModuleManagerFactory;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.gestalt.assets.module.ModuleAwareAssetTypeManager;
import org.terasology.gestalt.assets.module.ModuleAwareAssetTypeManagerImpl;
import org.terasology.gestalt.di.ServiceRegistry;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;
import org.terasology.reflection.ModuleTypeRegistry;
import org.terasology.reflection.TypeRegistry;
import org.terasology.unittest.stubs.EntityRefComponent;

import java.util.Collections;

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

    private Context context;
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
        assetTypeManager.switchEnvironment(moduleManager.getEnvironment());
        serviceRegistry.with(AssetManager.class).lifetime(Lifetime.Singleton)
                .use(() -> assetTypeManager.getAssetManager());

        baseContext = new ContextImpl(serviceRegistry);
        CoreRegistry.setContext(baseContext);
    }

    @BeforeEach
    public void setup() throws Exception {
        NetworkSystem networkSystem = mock(NetworkSystem.class);
        when(networkSystem.getMode()).thenReturn(NetworkMode.NONE);

        ServiceRegistry serviceRegistry = new ServiceRegistry();
        serviceRegistry.with(NetworkSystem.class).lifetime(Lifetime.Singleton).use(() -> networkSystem);

        TypeRegistry typeRegistry = new ModuleTypeRegistry(moduleManager.getEnvironment());
        serviceRegistry.with(TypeRegistry.class).lifetime(Lifetime.Singleton).use(() -> typeRegistry);

        EntitySystemSetupUtil.addReflectionBasedLibraries(serviceRegistry);
        EntitySystemSetupUtil.addEntityManagementRelatedClasses(serviceRegistry);

        context = new ContextImpl(baseContext, serviceRegistry);
        CoreRegistry.setContext(context);

        EntitySystemSetupUtil.configureEntityManagementRelatedClasses(
                context.get(TypeHandlerLibrary.class),
                context.get(EntitySystemLibrary.class),
                moduleManager.getEnvironment(),
                context.get(EngineEntityManager.class));

        entityManager = context.get(EngineEntityManager.class);
        ((PojoEntityManager) entityManager).setPrefabManager(context.get(PrefabManager.class));

        entityManager.getComponentLibrary().register(
                new org.terasology.gestalt.assets.ResourceUrn("unittest", "EntityRefComponent"),
                EntityRefComponent.class);
    }

    /**
     * When a {@link ForwardReferenceEntityRef} is stored in a component field, calling
     * {@link ForwardReferenceEntityRef#resolveForwardReferences} must replace it with the
     * real {@link EntityRef} once the target entity exists.
     */
    @Test
    public void resolveForwardRef_replacesProxyWithRealEntityRef() {
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
        assertTrue(stored.getId() == target.getId(),
                "Resolved ref must point to the original target entity id");
    }

    /**
     * If a component field holds a {@link ForwardReferenceEntityRef} whose target does NOT
     * yet exist (genuinely dangling reference), the fix-up pass must leave it untouched.
     */
    @Test
    public void resolveForwardRef_leavesUnresolvableProxyUntouched() {
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
    public void resolveForwardRef_skipsDestroyedEntities() {
        EntityRef target = entityManager.create();
        EntityRef holder = entityManager.create();

        ForwardReferenceEntityRef proxy = new ForwardReferenceEntityRef(entityManager, target.getId());
        holder.addComponent(new EntityRefComponent(proxy));
        holder.destroy();

        // Should not throw
        ForwardReferenceEntityRef.resolveForwardReferences(entityManager, Collections.singletonList(holder));

        assertFalse(holder.exists(), "Destroyed entity must still not exist after fix-up");
    }
}
