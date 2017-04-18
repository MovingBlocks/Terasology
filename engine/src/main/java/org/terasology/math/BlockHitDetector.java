/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.math;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;

/**
 * BlockHitDetector detects where the block was hit.
 */
public class BlockHitDetector {

    private static Logger logger = LoggerFactory.getLogger(BlockHitDetector.class);

    public static Edge detectEdge(Vector3f hitPosition, Vector3i targetBlockPosition) {
        Vector3f hitPos = new Vector3f(hitPosition);
        hitPos.sub(targetBlockPosition.getX(), targetBlockPosition.getY(),targetBlockPosition.getZ());

        Side sideX, sideY, sideZ;
        if( hitPos.x < 0.f ) {
            hitPos.x = - hitPos.x;
            sideX = Side.LEFT;
        } else {
            sideX = Side.RIGHT;
        }
        if( hitPos.y < 0.f ) {
            hitPos.y = - hitPos.y;
            sideY = Side.BOTTOM;
        } else {
            sideY = Side.TOP;
        }
        if( hitPos.z < 0.f ) {
            hitPos.z = - hitPos.z;
            sideZ = Side.FRONT;
        } else {
            sideZ = Side.BACK;
        }
        switch ( (hitPos.y > hitPos.x ? 4 : 0) + (hitPos.y > hitPos.z ? 2 : 0) + (hitPos.x > hitPos.z ? 1 : 0) ) {
            case 0: //Z>X>Y
                return Edge.forSides(sideZ,sideX);
            case 1: //X>Z>Y
                return Edge.forSides(sideX,sideZ);
            case 3: //X>Y>Z
                return Edge.forSides(sideX,sideY);
            case 4: //Z>Y>X
                return Edge.forSides(sideZ,sideY);
            case 6: //Y>Z>X
                return Edge.forSides(sideY,sideZ);
            case 7: //Y>X>Z
                return Edge.forSides(sideY,sideX);
            default: //2: //X>Y>Z>X invalid, 5: //Y>X>Z>Y invalid
                return Edge.BottomBack;
        }
    }






}
