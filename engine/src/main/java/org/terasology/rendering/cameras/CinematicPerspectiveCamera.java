/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.rendering.cameras;

import javax.vecmath.Vector3f;
import java.util.Deque;
import java.util.LinkedList;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class CinematicPerspectiveCamera extends PerspectiveCamera {
    private Deque<Vector3f> previousPositions = new LinkedList<>();
    private Deque<Vector3f> previousViewingDirections = new LinkedList<>();


    private int lastCount = 60;
    private float multiplier = 0.9f;

    @Override
    public void update(float deltaT) {
        previousPositions.addFirst(new Vector3f(position));
        previousViewingDirections.addFirst(new Vector3f(viewingDirection));

        if (previousPositions.size() > lastCount) {
            previousPositions.removeLast();
            previousViewingDirections.removeLast();
        }

        position.set(calculateVector(previousPositions));
        viewingDirection.set(calculateVector(previousViewingDirections));

        super.update(deltaT);    //To change body of overridden methods use File | Settings | File Templates.
    }

    private Vector3f calculateVector(Deque<Vector3f> vectors) {
        int i = 0;
        float x = 0;
        float y = 0;
        float z = 0;
        float factorMult = 0;

        for (Vector3f vector : vectors) {
            double factor = Math.pow(multiplier, i);
            factorMult += factor;
            x += vector.x * factor;
            y += vector.y * factor;
            z += vector.z * factor;
            i++;
        }

        return new Vector3f(x / factorMult, y / factorMult, z / factorMult);
    }
}
