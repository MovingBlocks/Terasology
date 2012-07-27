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
package org.terasology.rendering.physics;

import com.bulletphysics.collision.broadphase.*;
import com.bulletphysics.collision.dispatch.*;
import com.bulletphysics.collision.shapes.*;
import com.bulletphysics.collision.shapes.voxel.VoxelWorldShape;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.DynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.Transform;
import com.google.common.collect.Lists;
import org.terasology.components.block.BlockComponent;
import org.terasology.entityFactory.BlockItemFactory;
import org.terasology.entitySystem.*;
import org.terasology.game.CoreRegistry;
import org.terasology.game.Timer;
import org.terasology.logic.world.BlockChangedEvent;
import org.terasology.logic.world.BlockEntityRegistry;
import org.terasology.math.Vector3i;
import org.terasology.math.AABB;
import org.terasology.performanceMonitor.PerformanceMonitor;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.utilities.FastRandom;

import javax.vecmath.*;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Renders blocks using the Bullet physics library.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class BulletPhysicsRenderer implements EventReceiver<BlockChangedEvent> {

    private Logger _logger = Logger.getLogger(getClass().getName());

    private final Deque<RigidBodyRequest> _insertionQueue = new LinkedList<RigidBodyRequest>();
    private final Deque<RigidBody> _removalQueue = new LinkedList<RigidBody>();

    private final CollisionDispatcher _dispatcher;
    private final BroadphaseInterface _broadphase;
    private final CollisionConfiguration _defaultCollisionConfiguration;
    private final SequentialImpulseConstraintSolver _sequentialImpulseConstraintSolver;
    private final DiscreteDynamicsWorld _discreteDynamicsWorld;
    private final BlockEntityRegistry blockEntityRegistry;

    private final BlockItemFactory _blockItemFactory;

    private Timer _timer;
    private FastRandom _random = new FastRandom();
    private final WorldRenderer _parent;

    public BulletPhysicsRenderer(WorldRenderer parent) {
        _broadphase = new DbvtBroadphase();
        _broadphase.getOverlappingPairCache().setInternalGhostPairCallback(new GhostPairCallback());
        _defaultCollisionConfiguration = new DefaultCollisionConfiguration();
        _dispatcher = new CollisionDispatcher(_defaultCollisionConfiguration);
        _sequentialImpulseConstraintSolver = new SequentialImpulseConstraintSolver();
        _discreteDynamicsWorld = new DiscreteDynamicsWorld(_dispatcher, _broadphase, _sequentialImpulseConstraintSolver, _defaultCollisionConfiguration);
        _discreteDynamicsWorld.setGravity(new Vector3f(0f, -15f, 0f));
        _parent = parent;
        _blockItemFactory = new BlockItemFactory(CoreRegistry.get(EntityManager.class));
        blockEntityRegistry = CoreRegistry.get(BlockEntityRegistry.class);
        _timer = CoreRegistry.get(Timer.class);
        CoreRegistry.get(EventSystem.class).registerEventReceiver(this, BlockChangedEvent.class, BlockComponent.class);

        PhysicsWorldWrapper wrapper = new PhysicsWorldWrapper(parent.getWorldProvider());
        VoxelWorldShape worldShape = new VoxelWorldShape(wrapper);

        Matrix3f rot = new Matrix3f();
        rot.setIdentity();
        DefaultMotionState blockMotionState = new DefaultMotionState(new Transform(new Matrix4f(rot, new Vector3f(0, 0, 0), 1.0f)));
        RigidBodyConstructionInfo blockConsInf = new RigidBodyConstructionInfo(0, blockMotionState, worldShape, new Vector3f());
        RigidBody rigidBody = new RigidBody(blockConsInf);
        rigidBody.setCollisionFlags(CollisionFlags.STATIC_OBJECT | rigidBody.getCollisionFlags());
        _discreteDynamicsWorld.addRigidBody(rigidBody);

    }

    public DynamicsWorld getWorld() {
        return _discreteDynamicsWorld;
    }

    // TODO: Wrap ghost object

    public PairCachingGhostObject createCollider(Vector3f pos, ConvexShape shape, short groups, short filters) {
        return createCollider(pos, shape, groups, filters, 0);
    }

    public PairCachingGhostObject createCollider(Vector3f pos, ConvexShape shape, short groups, short filters, int collisionFlags) {
        Transform startTransform = new Transform(new Matrix4f(new Quat4f(0, 0, 0, 1), pos, 1.0f));
        PairCachingGhostObject result = new PairCachingGhostObject();
        result.setWorldTransform(startTransform);
        result.setCollisionShape(shape);
        result.setCollisionFlags(collisionFlags);
        _discreteDynamicsWorld.addCollisionObject(result, groups, filters);
        return result;
    }

    public void addRigidBody(RigidBody body) {
        _insertionQueue.add(new RigidBodyRequest(body, CollisionFilterGroups.DEFAULT_FILTER, (short)(CollisionFilterGroups.DEFAULT_FILTER | CollisionFilterGroups.STATIC_FILTER)));
    }

    public void addRigidBody(RigidBody body, short groups, short filter) {
        _insertionQueue.add(new RigidBodyRequest(body, groups, filter));
    }

    public void removeRigidBody(RigidBody body) {
        _removalQueue.add(body);
    }

    public void removeCollider(GhostObject collider) {
        _discreteDynamicsWorld.removeCollisionObject(collider);
    }

    public Iterable<EntityRef> scanArea(AABB area, short collisionMask) {
        // TODO: Add the aabbTest method from newer versions of bullet to TeraBullet, use that instead
        BoxShape shape = new BoxShape(area.getExtents());
        GhostObject scanObject = createCollider(area.getCenter(), shape, CollisionFilterGroups.DEFAULT_FILTER, collisionMask, 0);
        // This in particular is overkill
        _broadphase.calculateOverlappingPairs(_dispatcher);
        List<EntityRef> result = Lists.newArrayList();
        for (int i = 0; i < scanObject.getNumOverlappingObjects(); ++i) {
            CollisionObject other = scanObject.getOverlappingObject(i);
            Object userObj = other.getUserPointer();
            if (userObj instanceof EntityRef) {
                result.add((EntityRef)userObj);
            }
        }
        removeCollider(scanObject);
        return result;
    }

    public HitResult rayTrace(Vector3f from, Vector3f direction, float distance) {
        Vector3f to = new Vector3f(direction);
        to.scale(distance);
        to.add(from);

        CollisionWorld.ClosestRayResultWithUserDataCallback closest = new CollisionWorld.ClosestRayResultWithUserDataCallback(from, to);
        _discreteDynamicsWorld.rayTest(from, to, closest);
        if (closest.userData instanceof Vector3i) {
            return new HitResult(blockEntityRegistry.getOrCreateEntityAt((Vector3i)closest.userData), closest.hitPointWorld, closest.hitNormalWorld);
        }
        return new HitResult();
    }

    @Override
    public void onEvent(BlockChangedEvent event, EntityRef entity) {
        Vector3f min = event.getBlockPosition().toVector3f();
        min.sub(new Vector3f(0.6f, 0.6f, 0.6f));
        Vector3f max = event.getBlockPosition().toVector3f();
        max.add(new Vector3f(0.6f, 0.6f, 0.6f));
        _discreteDynamicsWorld.awakenRigidBodiesInArea(min, max);
    }

    public void update(float delta) {
        processQueuedBodies();
        try {
            PerformanceMonitor.startActivity("Step Simulation");
            _discreteDynamicsWorld.stepSimulation(delta, 1);
            PerformanceMonitor.endActivity();
        } catch (Exception e) {
            _logger.log(Level.WARNING, "Somehow Bullet Physics managed to throw an exception again.", e);
        }
    }

    private synchronized void processQueuedBodies() {
        while (!_insertionQueue.isEmpty()) {
            RigidBodyRequest request = _insertionQueue.poll();
            _discreteDynamicsWorld.addRigidBody(request.body, request.groups, request.filter);
        }
        while (!_removalQueue.isEmpty()) {
            RigidBody body = _removalQueue.poll();
            _discreteDynamicsWorld.removeRigidBody(body);
        }
    }

    private static class RigidBodyRequest
    {
        final RigidBody body;
        final short groups;
        final short filter;

        public RigidBodyRequest(RigidBody body, short groups, short filter) {
            this.body = body;
            this.groups = groups;
            this.filter = filter;
        }
    }
}
