// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.common;

import com.google.common.collect.Sets;
import org.terasology.engine.network.Replicate;
import org.terasology.gestalt.entitysystem.component.Component;

import java.util.Set;

/**
 * This component is intended to list component classes that are supposed to be retained when converting between blocks
 * and block items.
 * <p>
 * If a block (item) entity has a component that is not part of its prefab, retaining this component results in the block
 * entity still having this component afterwards. If not retained, it is likely to be removed instead.
 */
public class RetainComponentsComponent implements Component<RetainComponentsComponent> {
    @Replicate
    public Set<Class<? extends Component>> components = Sets.newHashSet();

    @Override
    public void copyFrom(RetainComponentsComponent other) {
        this.components = Sets.newHashSet(other.components); // TODO Investigate, needs to deep-copy or not!
    }
}
