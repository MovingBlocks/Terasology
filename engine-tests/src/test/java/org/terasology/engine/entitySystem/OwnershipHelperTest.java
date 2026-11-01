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
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.engine.entitySystem.entity.internal.OwnershipHelper;
import org.terasology.engine.entitySystem.metadata.EntitySystemLibrary;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.gestalt.di.ServiceRegistry;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;
import org.terasology.reflection.ModuleTypeRegistry;
import org.terasology.reflection.TypeRegistry;
import org.terasology.unittest.stubs.OwnerComponent;
import org.terasology.engine.network.NetworkMode;
import org.terasology.engine.network.NetworkSystem;
import org.terasology.engine.recording.RecordAndReplayCurrentStatus;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.testUtil.ModuleManagerFactory;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OwnershipHelperTest {

    private static ModuleManager moduleManager;

    EngineEntityManager entityManager;

    @BeforeAll
    public static void setupClass() throws Exception {
        moduleManager = ModuleManagerFactory.create();
    }

    @BeforeEach
    public void setup() {
        ServiceRegistry serviceRegistry = new ServiceRegistry();
        serviceRegistry.with(ModuleManager.class).lifetime(Lifetime.Singleton).use(() -> moduleManager);
        TypeRegistry typeRegistry = new ModuleTypeRegistry(moduleManager.getEnvironment());
        serviceRegistry.with(TypeRegistry.class).lifetime(Lifetime.Singleton).use(() -> typeRegistry);
        AssetManager assetManager = mock(AssetManager.class);
        serviceRegistry.with(AssetManager.class).lifetime(Lifetime.Singleton).use(() -> assetManager);
        NetworkSystem networkSystem = mock(NetworkSystem.class);
        when(networkSystem.getMode()).thenReturn(NetworkMode.NONE);
        serviceRegistry.with(NetworkSystem.class).lifetime(Lifetime.Singleton).use(() -> networkSystem);
        RecordAndReplayCurrentStatus recordAndReplayCurrentStatus = new RecordAndReplayCurrentStatus();
        serviceRegistry.with(RecordAndReplayCurrentStatus.class).lifetime(Lifetime.Singleton).use(() -> recordAndReplayCurrentStatus);
        EntitySystemSetupUtil.addReflectionBasedLibraries(serviceRegistry);
        EntitySystemSetupUtil.addEntityManagementRelatedClasses(serviceRegistry);
        Context context = new ContextImpl(serviceRegistry);
        EntitySystemSetupUtil.configureEntityManagementRelatedClasses(context.get(TypeHandlerLibrary.class),
                context.get(EntitySystemLibrary.class), context.get(ModuleManager.class).getEnvironment(),
                context.get(EngineEntityManager.class));
        CoreRegistry.setContext(context);
        entityManager = context.get(EngineEntityManager.class);
    }

    @Test
    public void testListsOwnedEntities() {
        EntityRef ownedEntity = entityManager.create();
        OwnerComponent ownerComp = new OwnerComponent();
        ownerComp.child = ownedEntity;
        EntityRef ownerEntity = entityManager.create(ownerComp);

        OwnershipHelper helper = new OwnershipHelper(entityManager.getComponentLibrary());
        ArrayList<EntityRef> target = Lists.newArrayList(helper.listOwnedEntities(ownerEntity));
        assertEquals(target.size(), 1);
        assertEquals(target.get(0), ownedEntity);
    }
}
