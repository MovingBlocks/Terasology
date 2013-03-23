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
package org.terasology.rendering.gui.animation;

import javax.vecmath.Vector2f;

public class AnimationSize extends Animation {
    private Vector2f sizeFrom;
    private Vector2f distination;
    private float speed;
    private float dX, dY;

    public AnimationSize(Vector2f sizeTo, float speed) {
        this.distination = sizeTo;
        this.speed = speed;
        this.sizeFrom = null;
    }

    @Override
    public void update() {

        if (dX == 0 || dY == 0) {
            dX = target.getSize().x < distination.x ? target.getSize().x / distination.x : -distination.x / target.getSize().x;
            dY = target.getSize().y < distination.y ? target.getSize().y / distination.y : -distination.y / target.getSize().y;
        }

        Vector2f direction = new Vector2f(dX, dY);
        direction.normalize();

        if (isRepeat() && (sizeFrom == null || sizeFrom.equals(new Vector2f(0f, 0f)))) {
            sizeFrom = new Vector2f(target.getSize());
        }

        if (Math.abs((target.getSize().x - distination.x)) > speed || Math.abs((target.getSize().y - distination.y)) > speed) {
            direction.normalize();
            direction.scale(speed);
            Vector2f newSize = new Vector2f(target.getSize());
            newSize.add(direction);

            if (Math.abs((target.getSize().x - distination.x)) < speed) {
                newSize.x = target.getSize().x;
            }

            if (Math.abs((target.getSize().y - distination.y)) < speed) {
                newSize.y = target.getSize().y;
            }

            target.setSize(newSize);
        } else {
            target.setSize(distination);

            if (isRepeat()) {
                Vector2f swap = new Vector2f(distination);
                distination = new Vector2f(sizeFrom);
                sizeFrom = swap;
                dX = 0;
                dY = 0;
            } else {
                stop();
            }
        }
    }

    @Override
    public void renderBegin() {

    }

    @Override
    public void renderEnd() {

    }
}
