// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.common;

import com.google.common.collect.Sets;
import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.network.Replicate;

import java.util.Set;

/**
 * This component is intended to list component classes that are supposed to be retained when converting between blocks
 * and block items.
 * <p>
 * If a block (item) entity has a component that is not part of its prefab, retaining this component results in the block
 * entity still having this component afterwards. If not retained, it is likely to be removed instead.
 */
public class RetainComponentsComponent implements Component {
    @Replicate
    public Set<Class<? extends Component>> components = Sets.newHashSet();
}
