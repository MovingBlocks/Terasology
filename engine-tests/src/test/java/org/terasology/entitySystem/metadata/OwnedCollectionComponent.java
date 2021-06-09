// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.entitySystem.metadata;

import com.google.common.collect.Lists;
import org.terasology.engine.entitySystem.Owns;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.gestalt.entitysystem.component.Component;

import java.util.List;

public class OwnedCollectionComponent implements Component {
    @Owns
    public List<EntityRef> items = Lists.newArrayList();
}
