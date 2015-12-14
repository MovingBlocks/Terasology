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

package org.terasology.world.block.family;

import com.google.common.collect.Lists;
import org.terasology.world.block.BlockUri;

import java.util.Collections;
import java.util.List;

/**
 * A freeform family is a pseudo block family that can be combined with any block shape to produce an actual block
 * family.
 *
 */
public class FreeformFamily {
    public BlockUri uri;
    public List<String> categories;

    public FreeformFamily(BlockUri uri) {
        this(uri, Collections.<String>emptyList());
    }

    public FreeformFamily(BlockUri uri, Iterable<String> categories) {
        this.uri = uri;
        this.categories = Lists.newArrayList(categories);
    }
}
