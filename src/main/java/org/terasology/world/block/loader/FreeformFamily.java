/*
 * Copyright 2013 Moving Blocks
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

package org.terasology.world.block.loader;

import org.terasology.world.block.BlockUri;

/**
 * A freeform family is a pseudo block family that can be combined with any block shape to produce an actual block
 * family.
 *
 * @author Immortius
 */
public class FreeformFamily {
    public BlockUri uri;
    public String[] categories = new String[0];

    public FreeformFamily(BlockUri uri) {
        this.uri = uri;
    }

    public FreeformFamily(BlockUri uri, String[] categories) {
        this(uri);
        this.categories = categories;
    }
}
