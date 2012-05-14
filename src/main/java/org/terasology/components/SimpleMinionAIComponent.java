package org.terasology.components;

import org.terasology.entitySystem.Component;
import sun.org.mozilla.javascript.internal.ast.NewExpression;

import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Overdhose
 * copied from SimpleAIComponent, only movementtarget is really used
 */
public final class SimpleMinionAIComponent implements Component {

    public long lastChangeOfDirectionAt = 0;

    public long lastAttacktime = 0;
    public long lastPathtime = 0;
    public int patrolCounter = 0;

    public Vector3f movementTarget = new Vector3f();
    public Vector3f previousTarget = new Vector3f();

    public List<Vector3f> movementTargets = new ArrayList<Vector3f>();
    public List<Vector3f> gatherTargets = new ArrayList<Vector3f>();
    public List<Vector3f> patrolTargets = new ArrayList<Vector3f>();

    public boolean followingPlayer = true;

    public void ClearCommands(){
        movementTargets.removeAll(movementTargets);
        gatherTargets.removeAll(gatherTargets);
        patrolTargets.removeAll(patrolTargets);
    }

}
