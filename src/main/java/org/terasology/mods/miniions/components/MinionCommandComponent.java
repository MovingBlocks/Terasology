package org.terasology.mods.miniions.components;

import javax.vecmath.Vector3f;

/**
 * Created with IntelliJ IDEA.
 * User: Overdhose
 * Date: 24/05/12
 * Time: 2:37
 * clicking only 2 points will select all blocks in the rectangle formed above the height of start point
 * clicking a 3d and /or 4th point will limit the height/ set depth of the selection, depending if the block clicked was higher / lower then the startpoint
 * if only a depth point was selected, the selection will only be downwards
 * only rectangular selection with no feedback to user
 * selected blocks will get grouped by height, and minions will gather top to bottom
 * startpoint should be a miniion (waypoint) flag block
 */
public class MinionCommandComponent {

    public MinionCommandComponent() {
    }

    public Vector3f startPoint = null;
    public Vector3f endPoint = null;
    public Vector3f heightPoint = null;
    public Vector3f depthPoint = null;
}
