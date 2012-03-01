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
package org.terasology.logic.tools;

import org.terasology.game.Terasology;
import org.terasology.logic.characters.Player;
import org.terasology.math.Side;
import org.terasology.model.blueprints.PortalBlueprint;
import org.terasology.model.structures.BlockPosition;
import org.terasology.model.structures.BlockSelection;
import org.terasology.model.structures.RayBlockIntersection;

import javax.vecmath.Vector3d;
import java.util.logging.Level;

/**
 * Debug tool for tinkering. Can be enabled / disabled whenever, not meant to be part of normal gameplay in this shape
 * @author Rasmus 'Cervator' Praestholm <cervator@gmail.com>
 */
public class DebugTool implements ITool {

    private final Player _player;

    public DebugTool(Player player) {
        _player = player;
    }

    public void executeLeftClickAction() {
        RayBlockIntersection.Intersection is = _player.calcSelectedBlock();

        if (is == null)
            return;


        // Place / replace a portal blueprint based on whether one is already there or not
        PortalBlueprint portalBP = new PortalBlueprint();
        // TODO: Test the selected block against the ES to see if there's already a portal there
        BlockPosition targetPos = is.getBlockPosition();
        // localize the BP to the location
        // Ask entity system if there's a portal there
        BlockSelection localizedSelection = portalBP.getCollection().getLocalizedSelection(targetPos);
        //TerasologyES.portalExistsAt(localizedSelection);

        // TODO: Rotate the blueprint to match the players dominant horizontal direction
        Vector3d rawDirection = new Vector3d(_player.getViewingDirection());
        Side direction = Side.inDirection(rawDirection.x, rawDirection.y, rawDirection.z);
        //System.out.println("Side is" + direction);

        //PortalComponent comp =
        portalBP.createPortal(_player.getParent().getWorldProvider(), targetPos);
        //System.out.println("Built a portal and here's the component: " + comp);
        // Create a portal Entity
        //TerasologyES.createPortal(comp);

        Terasology.getInstance().getLogger().log(Level.INFO, "Built a portal, BP's width/height/depth: " + portalBP.getCollection().calcWidth() + "-" + portalBP.getCollection().calcHeight() + "-" + portalBP.getCollection().calcDepth());
    }

    public void executeRightClickAction() {
        RayBlockIntersection.Intersection is = _player.calcSelectedBlock();

        if (is == null)
            return;

        is.getBlockPosition();
        // Do something else - disable / enable / trigger the portal?

        Vector3d rawDirection = new Vector3d(_player.getViewingDirection());
        Side direction = Side.inHorizontalDirection(rawDirection.x,rawDirection.z);
        System.out.println("Side is" + direction);
    }


}
