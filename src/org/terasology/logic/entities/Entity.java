/*
 * Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
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
package org.terasology.logic.entities;

import org.terasology.model.structures.AABB;
import org.terasology.persistence.TeraObject;
import org.terasology.rendering.interfaces.IGameObject;

import javax.vecmath.Vector3d;

/**
 * Entities are renderable objects in the world. Entities provide a position and a AABB.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public abstract class Entity extends TeraObject implements IGameObject {

    private final Vector3d _spawningPoint = new Vector3d();
    private final Vector3d _position = new Vector3d();

    public Vector3d getPosition() {
        return _position;
    }

    public void setPosition(Vector3d position) {
        _position.set(position);
    }

    public void setPosition(double x, double y, double z) {
        _position.set(x, y, z);
    }

    public void setSpawningPoint(Vector3d spawningPoint) {
        _spawningPoint.set(spawningPoint);
    }

    public Vector3d getSpawningPoint() {
        return _spawningPoint;
    }

    public void setSpawningPoint() {
        _spawningPoint.set(_position);
    }

    /**
     * Sets the position of the entity to the spawning point.
     */
    public void respawn() {
        _position.set(_spawningPoint);
    }

    public abstract AABB getAABB();
}
