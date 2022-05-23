// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.integrationenvironment.fixtures;

import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.integrationenvironment.ModuleTestingHelper;
import org.terasology.engine.registry.In;

// A dummy class for testing injection of super class fields
public class BaseTestingClass {
    @In
    private EntityManager entityManager;

    @In
    private ModuleTestingHelper helper;

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public ModuleTestingHelper getHelper() {
        return helper;
    }
}
