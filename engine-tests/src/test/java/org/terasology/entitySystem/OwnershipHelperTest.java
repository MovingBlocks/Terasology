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
package org.terasology.entitySystem;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.terasology.context.internal.ContextImpl;
import org.terasology.engine.bootstrap.EntitySystemSetupUtil;
import org.terasology.engine.module.ModuleManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.entitySystem.entity.internal.OwnershipHelper;
import org.terasology.entitySystem.stubs.OwnerComponent;
import org.terasology.network.NetworkSystem;
import org.terasology.recording.RecordAndReplayCurrentStatus;
import org.terasology.registry.CoreRegistry;
import org.terasology.testUtil.ModuleManagerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;


/**
 */
public class OwnershipHelperTest {

    private static ModuleManager moduleManager;

    EngineEntityManager entityManager;

    @BeforeAll
    public static void setupClass() throws Exception {
        moduleManager = ModuleManagerFactory.create();
    }

    @BeforeEach
    public void setup() {
        ContextImpl context = new ContextImpl();
        context.put(ModuleManager.class, moduleManager);
        context.put(NetworkSystem.class, mock(NetworkSystem.class));
        context.put(RecordAndReplayCurrentStatus.class, new RecordAndReplayCurrentStatus());
        CoreRegistry.setContext(context);
        EntitySystemSetupUtil.addReflectionBasedLibraries(context);
        EntitySystemSetupUtil.addEntityManagementRelatedClasses(context);
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
