// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.selection;

import org.joml.Vector3i;
import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.logic.selection.MovableSelectionStartEvent;
import org.terasology.module.sandbox.API;
import org.terasology.engine.rendering.assets.texture.Texture;
import org.terasology.engine.world.block.BlockRegion;

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
    public BlockRegion currentSelection;

    /**
     * If true, block selection will be drawn
     */
    public boolean shouldRender;

    /**
     * Texture used to indicate the selected blocks when drawing block selection.  Defaults to "engine:selection" if not specified.
     */
    public Texture texture;

    /**
     * If this is the position of the selected region changes with the camera target. This must be set true for a component
     * before sending the {@link MovableSelectionStartEvent} using the appropriate entity
     */
    public boolean isMovable = false;
}
