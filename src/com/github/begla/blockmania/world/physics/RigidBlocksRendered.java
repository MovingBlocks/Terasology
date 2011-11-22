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
package com.github.begla.blockmania.world.physics;

import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.broadphase.DbvtBroadphase;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.github.begla.blockmania.rendering.interfaces.RenderableObject;

import javax.vecmath.Vector3f;

/**
 * TODO: What is this?!
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class RigidBlocksRendered implements RenderableObject {

    CollisionDispatcher _dispatcher;
    BroadphaseInterface _broadphase;
    DefaultCollisionConfiguration _defaultCollisionConfiguration;
    SequentialImpulseConstraintSolver _sequentialImpulseConstraintSolver;
    DiscreteDynamicsWorld _discreteDynamicsWorld;

    public RigidBlocksRendered() {
        _broadphase = new DbvtBroadphase();
        _defaultCollisionConfiguration = new DefaultCollisionConfiguration();
        _dispatcher = new CollisionDispatcher(_defaultCollisionConfiguration);
        _sequentialImpulseConstraintSolver = new SequentialImpulseConstraintSolver();
        _discreteDynamicsWorld = new DiscreteDynamicsWorld(_dispatcher, _broadphase, _sequentialImpulseConstraintSolver, _defaultCollisionConfiguration);
        _discreteDynamicsWorld.setGravity(new Vector3f(0f, -10f, 0f));
    }

    public void render() {
    }

    public void update() {
    }
}
