package org.terasology.logic.characters.bullet;

import javax.vecmath.Vector3f;

/**
 * @author Immortius
 */
public class MoveResult {
    private Vector3f finalPosition;
    private boolean horizontalHit = false;
    private boolean bottomHit = false;
    private boolean topHit = false;

    public MoveResult(Vector3f finalPosition, boolean hitHorizontal, boolean hitBottom, boolean hitTop) {
        this.finalPosition = finalPosition;
        this.horizontalHit = hitHorizontal;
        this.bottomHit = hitBottom;
        this.topHit = hitTop;
    }

    public Vector3f getFinalPosition() {
        return finalPosition;
    }

    public boolean isHorizontalHit() {
        return horizontalHit;
    }

    public boolean isBottomHit() {
        return bottomHit;
    }

    public boolean isTopHit() {
        return topHit;
    }
}
