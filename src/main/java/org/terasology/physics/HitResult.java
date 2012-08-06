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

import org.terasology.entitySystem.EntityRef;

import javax.vecmath.Vector3f;

/**
 * @author Immortius
 */
public class HitResult {
    private boolean hit;
    private EntityRef entity;
    private Vector3f hitPoint;
    private Vector3f hitNormal;

    public HitResult() {
        hit = false;
        entity = EntityRef.NULL;
    }

    public HitResult(EntityRef entity, Vector3f hitPoint, Vector3f hitNormal) {
        this.hit = true;
        this.entity = entity;
        this.hitPoint = hitPoint;
        this.hitNormal = hitNormal;
    }

    public boolean isHit() {
        return hit;
    }

    public EntityRef getEntity() {
        return entity;
    }

    public Vector3f getHitPoint() {
        return hitPoint;
    }

    public Vector3f getHitNormal() {
        return hitNormal;
    }
}
