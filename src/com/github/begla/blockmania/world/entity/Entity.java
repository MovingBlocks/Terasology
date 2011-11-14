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
import com.github.begla.blockmania.rendering.RenderableObject;
import org.lwjgl.util.vector.Matrix3f;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

/**
 * Entities are renderable objects in the world. Entities provide a
 * position and a axis-aligned bounding box.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public abstract class Entity implements RenderableObject {

    private final Vector3f _spawningPoint = new Vector3f();
    private final Vector3f _position = new Vector3f();
    public final Vector3f _offset = new Vector3f();

    /**
     * Returns the position of the entity.
     *
     * @return The position
     */
    public Vector3f getPosition() {
        return _position;
    }

    /**
     * Sets the position of the entity.
     *
     * @param position The position
     */
    public void setPosition(Vector3f position) {
        _position.set(position);
    }

    /**
     * Sets the position of the entity.
     *
     * @param x
     * @param y
     * @param z
     */
    public void setPosition(float x, float y, float z) {
        _position.set(x, y, z);
    }

    /**
     * Sets the spawning point of the entity.
     *
     * @param spawningPoint The spawning point
     */
    public void setSpawningPoint(Vector3f spawningPoint) {
        _spawningPoint.set(spawningPoint);
    }

    /**
     * Returns the spawning point.
     *
     * @return The spawning point
     */
    public Vector3f getSpawningPoint() {
        return _spawningPoint;
    }

    /**
     * Sets the spawning point to the current position of the entity.
     */
    public void setSpawningPoint() {
        _spawningPoint.set(_position);
    }

    /**
     * Sets the position of the entity to the spawning point.
     */
    public void respawn() {
        _position.set(_spawningPoint);
    }

    /**
     * @return The AABB of the entity
     */
    public abstract AABB getAABB();
}
