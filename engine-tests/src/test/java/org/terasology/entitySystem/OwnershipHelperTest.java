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
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.terasology.context.internal.ContextImpl;
import org.terasology.engine.bootstrap.EntitySystemSetupUtil;
import org.terasology.engine.module.ModuleManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.entitySystem.entity.internal.OwnershipHelper;
import org.terasology.entitySystem.stubs.OwnerComponent;
import org.terasology.network.NetworkSystem;
import org.terasology.registry.CoreRegistry;
import org.terasology.testUtil.ModuleManagerFactory;

import static org.mockito.Mockito.mock;
import static org.terasology.testUtil.TeraAssert.assertEqualsContent;

/**
 */
public class OwnershipHelperTest {

    private static ModuleManager moduleManager;

    EngineEntityManager entityManager;

    @BeforeClass
    public static void setupClass() throws Exception {
        moduleManager = ModuleManagerFactory.create();
    }

    @Before
    public void setup() {
        ContextImpl context = new ContextImpl();
        context.put(ModuleManager.class, moduleManager);
        context.put(NetworkSystem.class, mock(NetworkSystem.class));
        CoreRegistry.setContext(context);
        EntitySystemSetupUtil.addReflectionBasedLibraries(context);
        EntitySystemSetupUtil.addEntityManagementRelatedClasses(context);
        entityManager = context.get(EngineEntityManager.class);
    }

    @Test
    public void listsOwnedEntities() {
        EntityRef ownedEntity = entityManager.create();
        OwnerComponent ownerComp = new OwnerComponent();
        ownerComp.child = ownedEntity;
        EntityRef ownerEntity = entityManager.create(ownerComp);

        OwnershipHelper helper = new OwnershipHelper(entityManager.getComponentLibrary());
        assertEqualsContent(Lists.newArrayList(ownedEntity), helper.listOwnedEntities(ownerEntity));
    }
}
