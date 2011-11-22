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
package com.github.begla.blockmania.world.entity;

import com.github.begla.blockmania.datastructures.AABB;
import com.github.begla.blockmania.rendering.interfaces.RenderableObject;

import javax.vecmath.Vector3f;

/**
 * Entities are renderable objects in the world. Entities provide a position and a AABB.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public abstract class Entity implements RenderableObject {

    private final Vector3f _spawningPoint = new Vector3f();
    private final Vector3f _position = new Vector3f();

    public Vector3f getPosition() {
        return _position;
    }

    public void setPosition(Vector3f position) {
        _position.set(position);
    }

    public void setPosition(float x, float y, float z) {
        _position.set(x, y, z);
    }

    public void setSpawningPoint(Vector3f spawningPoint) {
        _spawningPoint.set(spawningPoint);
    }

    public Vector3f getSpawningPoint() {
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
