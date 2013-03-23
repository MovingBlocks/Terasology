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
