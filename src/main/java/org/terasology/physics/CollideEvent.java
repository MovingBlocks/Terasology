/*
 * Copyright 2012
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


package org.terasology.physics;

import javax.vecmath.Vector3f;

import org.terasology.entitySystem.AbstractEvent;
import org.terasology.entitySystem.EntityRef;

/**
 * @author Immortius
 */
public class CollideEvent extends AbstractEvent {
    private EntityRef otherEntity;
    private Vector3f hitPoint;
    private Vector3f hitNormal;
    
    public CollideEvent(EntityRef other, Vector3f point, Vector3f normal) {
        otherEntity = other;
        hitPoint = point;
        hitNormal = normal;
    }

    public EntityRef getOtherEntity() {
        return otherEntity;
    }

	public Vector3f getHitPoint() {
		return hitPoint;
	}

	public Vector3f getHitNormal() {
		return hitNormal;
	}
    
}
