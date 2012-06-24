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

import com.bulletphysics.collision.broadphase.BroadphaseNativeType;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.linearmath.Transform;
import org.terasology.logic.world.WorldProvider;

import javax.vecmath.Vector3f;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Immortius
 */
public class WorldShape extends CollisionShape {

    public static final int AABB_SIZE = 1048576;
    private Logger logger = Logger.getLogger(getClass().getName());
    private WorldProvider world;

    private float collisionMargin = 0f;
    protected final Vector3f localScaling = new Vector3f(1f, 1f, 1f);

    public WorldShape(WorldProvider world) {
        this.world = world;
    }

    /**
     * getAabb's default implementation is brute force, expected derived classes to implement a fast dedicated version.
     */
    @Override
    public void getAabb(Transform trans, Vector3f aabbMin, Vector3f aabbMax) {
        aabbMin.set(-AABB_SIZE, -AABB_SIZE, -AABB_SIZE);
        aabbMax.set(AABB_SIZE, AABB_SIZE, AABB_SIZE);
    }

    @Override
    public void setLocalScaling(Vector3f scaling) {
        localScaling.set(scaling);
    }

    @Override
    public Vector3f getLocalScaling(Vector3f out) {
        out.set(localScaling);
        return out;
    }

    @Override
    public void calculateLocalInertia(float mass, Vector3f inertia) {
        logger.log(Level.INFO,  "Inertia Requested");
        inertia.set(0,0,0);
    }

    @Override
    public BroadphaseNativeType getShapeType() {
        return BroadphaseNativeType.INVALID_SHAPE_PROXYTYPE;
    }

    @Override
    public void setMargin(float margin) {
        collisionMargin = margin;
    }

    @Override
    public float getMargin() {
        return collisionMargin;
    }

    @Override
    public String getName() {
        return "World";
    }

    public WorldProvider getWorld() {
        return world;
    }
}
