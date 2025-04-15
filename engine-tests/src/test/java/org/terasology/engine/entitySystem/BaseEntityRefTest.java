// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.entitySystem;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.terasology.context.Lifetime;
import org.terasology.engine.context.Context;
import org.terasology.engine.context.internal.ContextImpl;
import org.terasology.engine.core.bootstrap.EntitySystemSetupUtil;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.engine.entitySystem.entity.internal.EntityInfoComponent;
import org.terasology.engine.entitySystem.entity.internal.EntityScope;
import org.terasology.engine.entitySystem.entity.internal.PojoEntityManager;
import org.terasology.engine.entitySystem.metadata.EntitySystemLibrary;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.prefab.internal.PojoPrefab;
import org.terasology.engine.network.NetworkMode;
import org.terasology.engine.network.NetworkSystem;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.terasology.engine.entitySystem.entity.internal.EntityScope.CHUNK;
import static org.terasology.engine.entitySystem.entity.internal.EntityScope.GLOBAL;
import static org.terasology.engine.entitySystem.entity.internal.EntityScope.SECTOR;

public class BaseEntityRefTest {

    private static Context context;
    private PojoEntityManager entityManager;
    private EntityRef ref;

    @BeforeAll
    public static void setupClass() throws Exception {
        ServiceRegistry serviceRegistry = new ServiceRegistry();
        ModuleManager moduleManager = ModuleManagerFactory.create();
        serviceRegistry.with(ModuleManager.class).lifetime(Lifetime.Singleton).use(() -> moduleManager);
        TypeRegistry typeRegistry = new ModuleTypeRegistry(moduleManager.getEnvironment());
        serviceRegistry.with(TypeRegistry.class).lifetime(Lifetime.Singleton).use(() -> typeRegistry);
        ModuleAwareAssetTypeManager assetTypeManager = new ModuleAwareAssetTypeManagerImpl();
        assetTypeManager.createAssetType(Prefab.class, PojoPrefab::new, "prefabs");
        assetTypeManager.switchEnvironment(moduleManager.getEnvironment());
        serviceRegistry.with(AssetManager.class).lifetime(Lifetime.Singleton).use(() -> assetTypeManager.getAssetManager());
        serviceRegistry.with(RecordAndReplayCurrentStatus.class).lifetime(Lifetime.Singleton).use(() -> new RecordAndReplayCurrentStatus());
        context = new ContextImpl(serviceRegistry);
        CoreRegistry.setContext(context);
    }

    @BeforeEach
    public void setup() {
        NetworkSystem networkSystem = mock(NetworkSystem.class);
        when(networkSystem.getMode()).thenReturn(NetworkMode.NONE);
        ServiceRegistry serviceRegistry = new ServiceRegistry();
        serviceRegistry.with(NetworkSystem.class).lifetime(Lifetime.Singleton).use(() -> networkSystem);
        EntitySystemSetupUtil.addReflectionBasedLibraries(serviceRegistry);
        EntitySystemSetupUtil.addEntityManagementRelatedClasses(serviceRegistry);
        context = new ContextImpl(context, serviceRegistry);
        EntitySystemSetupUtil.configureEntityManagementRelatedClasses(context.get(TypeHandlerLibrary.class),
                context.get(EntitySystemLibrary.class), context.get(ModuleManager.class).getEnvironment(),
                context.get(EngineEntityManager.class));
        entityManager = (PojoEntityManager) context.get(EntityManager.class);

        ref = entityManager.create();
    }

    @Test
    public void testSetScope() {
        ref = entityManager.create();
        assertEquals(CHUNK, ref.getScope());
        for (EntityScope scope : EntityScope.values()) {
            ref.setScope(scope);
            assertEquals(ref.getScope(), scope);
        }

        //Move into sector scope
        ref.setScope(SECTOR);
        assertEquals(ref.getScope(), SECTOR);
        assertTrue(entityManager.getSectorManager().contains(ref.getId()));
        assertFalse(entityManager.getGlobalPool().contains(ref.getId()));

        //And move back to global scope
        ref.setScope(GLOBAL);
        assertEquals(ref.getScope(), GLOBAL);
        assertTrue(entityManager.getGlobalPool().contains(ref.getId()));
        assertFalse(entityManager.getSectorManager().contains(ref.getId()));

    }

    @Test
    public void testCreateWithScopeInfoComponent() {
        EntityInfoComponent info = new EntityInfoComponent();
        info.scope = SECTOR;
        EntityInfoComponent info2 = new EntityInfoComponent();
        info2.scope = SECTOR;

        ref = entityManager.create(info);
        assertEquals(SECTOR, ref.getScope());

        long safeId = ref.getId();
        ref.destroy();

        ref = entityManager.createEntityWithId(safeId, Lists.newArrayList(info2));
        assertNotNull(ref);
        assertNotEquals(EntityRef.NULL, ref);
        assertEquals(SECTOR, ref.getScope());

    }

}
