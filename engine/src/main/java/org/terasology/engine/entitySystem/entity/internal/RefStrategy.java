// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.entitySystem.entity.internal;

import org.terasology.engine.entitySystem.entity.LowLevelEntityManager;

@FunctionalInterface
public interface RefStrategy {

    BaseEntityRef createRefFor(long id, LowLevelEntityManager entityManager);
}
