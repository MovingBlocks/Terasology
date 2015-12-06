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
package org.terasology.world.selection;

import org.terasology.entitySystem.Component;
import org.terasology.math.Region3i;
import org.terasology.math.geom.Vector3i;
import org.terasology.module.sandbox.API;
import org.terasology.rendering.assets.texture.Texture;

/**
 *         <br><br>
 *         Use this component to track and draw a region of selected blocks.
 *         <br><br>
 *         One example use is with the LocalPlayerBlockSelectionByItemSystem.
 *         Add this component to any item entity, to make the item to a selection item. When using such items, a temporary
 *         selection is placed in the world. First use sets the starting point, second use finishes the selection and
 *         a ApplyBlockSelectionEvent is fired to the player using the selection item.
 *         <br><br>
 */
@API
public class BlockSelectionComponent implements Component {
    /**
     * Starting point for the block selection.   Used to re-create the currentSelection region when the ending point is changed.
     */
    public Vector3i startPosition;

    /**
     * Selected block region.   Starts as null, then is set to a single block indicated by the start position
     * when the starting point is set, then represents the region between the starting
     * and ending points after the ending point is set.
     */
    public Region3i currentSelection;

    /**
     * If true, block selection will be drawn
     */
    public boolean shouldRender;

    /**
     * Texture used to indicate the selected blocks when drawing block selection.  Defaults to "engine:selection" if not specified.
     */
    public Texture texture;
}
