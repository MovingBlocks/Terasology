/*
 * Copyright 2020 MovingBlocks
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
package org.terasology.logic.common;

import com.google.common.collect.Sets;
import org.terasology.entitySystem.Component;
import org.terasology.network.Replicate;

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
