/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.logic.selection;

import org.terasology.engine.API;
import org.terasology.entitySystem.Component;
import org.terasology.math.Region3i;
import org.terasology.math.Vector3i;

/**
 * Add this component to any item entity, to make the item to a selection item. When using such items, a temporary
 * selection is placed in the world. First use sets the starting point, second use finishes the selection and
 * a ApplyBlockSelectionEvent is fired to the player using the selection item.
 * <p/>
 * TODO add customizing properties to this component, like selection color.
 *
 * @author synopia
 */
@API
public class BlockSelectionComponent implements Component {
    public Vector3i startPosition;
    public Region3i currentSelection;
}
