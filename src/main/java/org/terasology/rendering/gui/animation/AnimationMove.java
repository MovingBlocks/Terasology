/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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

public class AnimationMove extends Animation {
    private Vector2f moveFrom;
    private Vector2f distination;
    private float    speed;

    public AnimationMove(Vector2f moveTo, float speed){
        this.distination = moveTo;
        this.speed       = speed;
        this.moveFrom    = null;
    }

    @Override
    public void update() {
        Vector2f direction = new Vector2f();
        direction.sub(distination, target.getPosition());

        if(isRepeat() && (moveFrom == null || moveFrom.equals(new Vector2f(0f, 0f)))){
            moveFrom = new Vector2f(target.getAbsolutePosition());
        }

        float distance = direction.length();

        if( distance > speed ){
            direction.normalize();
            direction.scale(speed /** tick*/);
            Vector2f newPosition = target.getPosition();
            newPosition.add(direction);
        }else{
            target.setPosition(distination);

            if(isRepeat()){
                Vector2f swap = new Vector2f(distination);
                distination = new Vector2f(moveFrom);
                moveFrom = swap;
            }else{
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
