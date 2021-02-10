/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.world.block.structure;

import org.terasology.entitySystem.Component;

/**
 * Component for block entities that wish to describe their structural dependency on other blocks. One can describe
 * what are its support sides. If a block describes multiple sides as "allowed", at least one of them is required to
 * exist in order for the block to be "structurally sound".
 *
 * As an example - a chandelier would have topAllowed=true, most of building blocks for houses would be -
 * bottomAllowed=true, sideAllowed=true, table (furniture) would be bottomAllowed=true.
 *
 */
public class SideBlockSupportRequiredComponent implements Component {
    public boolean topAllowed;
    public boolean sideAllowed;
    public boolean bottomAllowed;
    public long dropDelay;
}
