/*
 * Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
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
package org.terasology.model.blueprints;

import org.terasology.logic.world.WorldProvider;
import org.terasology.model.blocks.management.BlockManager;
import org.terasology.model.structures.BlockPosition;
import org.terasology.model.structures.BlockSelection;

//import org.terasology.es.PortalComponent;

/**
 * This blueprint is specific to Portals and linked to its meta object / component.
 * It is divided into two selections - one for the frame and one for the portal effect
 * @author Rasmus 'Cervator' Praestholm <cervator@gmail.com>
 */
public class PortalBlueprint extends Blueprint {

    /** Fill the blueprint with the default Portal design. This isn't the right way to do it... */
    public PortalBlueprint() {
        // This becomes the portalBlocks Selection in PortalComponent
        _blockCollection.addBlock(new BlockPosition(1,1,0), BlockManager.getInstance().getBlock("PortalBlock"));

        // And these guys make up the frame
        _blockCollection.addBlock(new BlockPosition(1,2,0), BlockManager.getInstance().getBlock("PortalFrameBlock"));
        _blockCollection.addBlock(new BlockPosition(0,1,0), BlockManager.getInstance().getBlock("PortalFrameBlock"));
        _blockCollection.addBlock(new BlockPosition(2,1,0), BlockManager.getInstance().getBlock("PortalFrameBlock"));
        _blockCollection.addBlock(new BlockPosition(1,0,0), BlockManager.getInstance().getBlock("PortalFrameBlock"));

        // Set the position we'll use as the attachment point
        _blockCollection.setAttachPos(new BlockPosition(1,-1,0));
    }

    /**
     * Creates a Portal in a particular spot in the world as per the design in this blueprint
     * A PortalComponent is also filled with data in preparation to create an associated Entity
     * @param provider The world to create the Portal in
     * @param pos Where to place the Portal by its attachment point
     * @return An instantiated PortalComponent that knows where the Portal is (via BlockSelections)
     */
    //TODO: Remember that this was hooked up to Cervator's ES hack, it needs to be hooked back up to _something_ later again
    //public PortalComponent createPortal(IWorldProvider provider, BlockPosition pos) {
    public void createPortal(WorldProvider provider, BlockPosition pos) {
        // Build the Portal in pieces so we can store what's what in the returned PortalComponent
        BlockSelection portalBlocks = _blockCollection.buildWithFilter(provider, pos, "PortalBlock");
        BlockSelection frameBlocks = _blockCollection.buildWithFilter(provider, pos, "PortalFrameBlock");

        //return new PortalComponent(portalBlocks, frameBlocks);
    }
}
