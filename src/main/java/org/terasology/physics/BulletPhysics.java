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
package org.terasology.physics;

import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.world.block.BlockComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventReceiver;
import org.terasology.entitySystem.EventSystem;
import org.terasology.game.CoreRegistry;
import org.terasology.math.AABB;
import org.terasology.math.Vector3i;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.world.BlockChangedEvent;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;

import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.broadphase.CollisionFilterGroups;
import com.bulletphysics.collision.broadphase.DbvtBroadphase;
import com.bulletphysics.collision.dispatch.CollisionConfiguration;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.CollisionFlags;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.CollisionWorld;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.collision.dispatch.GhostObject;
import com.bulletphysics.collision.dispatch.GhostPairCallback;
import com.bulletphysics.collision.dispatch.PairCachingGhostObject;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.ConvexShape;
import com.bulletphysics.collision.shapes.voxel.VoxelWorldShape;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.DynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.Transform;
import com.google.common.collect.Lists;

/**
 * Renders blocks using the Bullet physics library.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
// TODO: Merge this with Physics System
public class BulletPhysics implements EventReceiver<BlockChangedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(BulletPhysics.class);
    
    private final Deque<RigidBodyRequest> _insertionQueue = new LinkedList<RigidBodyRequest>();
    private final Deque<RigidBody> _removalQueue = new LinkedList<RigidBody>();

    private final CollisionDispatcher _dispatcher;
    private final BroadphaseInterface _broadphase;
    private final CollisionConfiguration _defaultCollisionConfiguration;
    private final SequentialImpulseConstraintSolver _sequentialImpulseConstraintSolver;
    private final DiscreteDynamicsWorld _discreteDynamicsWorld;
    private final BlockEntityRegistry blockEntityRegistry;
    private final CollisionGroupManager collisionGroupManager;


    public BulletPhysics(WorldProvider world) {
        collisionGroupManager = CoreRegistry.get(CollisionGroupManager.class);

        _broadphase = new DbvtBroadphase();
        _broadphase.getOverlappingPairCache().setInternalGhostPairCallback(new GhostPairCallback());
        _defaultCollisionConfiguration = new DefaultCollisionConfiguration();
        _dispatcher = new CollisionDispatcher(_defaultCollisionConfiguration);
        _sequentialImpulseConstraintSolver = new SequentialImpulseConstraintSolver();
        _discreteDynamicsWorld = new DiscreteDynamicsWorld(_dispatcher, _broadphase, _sequentialImpulseConstraintSolver, _defaultCollisionConfiguration);
        _discreteDynamicsWorld.setGravity(new Vector3f(0f, -15f, 0f));
        blockEntityRegistry = CoreRegistry.get(BlockEntityRegistry.class);
        CoreRegistry.get(EventSystem.class).registerEventReceiver(this, BlockChangedEvent.class, BlockComponent.class);

        PhysicsWorldWrapper wrapper = new PhysicsWorldWrapper(world);
        VoxelWorldShape worldShape = new VoxelWorldShape(wrapper);

        Matrix3f rot = new Matrix3f();
        rot.setIdentity();
        DefaultMotionState blockMotionState = new DefaultMotionState(new Transform(new Matrix4f(rot, new Vector3f(0, 0, 0), 1.0f)));
        RigidBodyConstructionInfo blockConsInf = new RigidBodyConstructionInfo(0, blockMotionState, worldShape, new Vector3f());
        RigidBody rigidBody = new RigidBody(blockConsInf);
        rigidBody.setCollisionFlags(CollisionFlags.STATIC_OBJECT | rigidBody.getCollisionFlags());
        _discreteDynamicsWorld.addRigidBody(rigidBody, combineGroups(StandardCollisionGroup.WORLD), (short)(CollisionFilterGroups.ALL_FILTER ^ CollisionFilterGroups.STATIC_FILTER));
    }

    public DynamicsWorld getWorld() {
        return _discreteDynamicsWorld;
    }

    // TODO: Wrap ghost object?
    public PairCachingGhostObject createCollider(Vector3f pos, ConvexShape shape, List<CollisionGroup> groups, List<CollisionGroup> filters) {
        return createCollider(pos, shape, groups, filters, 0);
    }

    public static short combineGroups(CollisionGroup ... groups) {
        return combineGroups(Arrays.asList(groups));
    }

    public static short combineGroups(Iterable<CollisionGroup> groups) {
        short flags = 0;
        for (CollisionGroup group : groups) {
            flags |= group.getFlag();
        }
        return flags;
    }

    public PairCachingGhostObject createCollider(Vector3f pos, ConvexShape shape, List<CollisionGroup> groups, List<CollisionGroup> filters, int collisionFlags) {
        return createCollider(pos, shape, combineGroups(groups), combineGroups(filters), collisionFlags);
    }

    private PairCachingGhostObject createCollider(Vector3f pos, ConvexShape shape, short groups, short filters, int collisionFlags) {
        Transform startTransform = new Transform(new Matrix4f(new Quat4f(0, 0, 0, 1), pos, 1.0f));
        PairCachingGhostObject result = new PairCachingGhostObject();
        result.setWorldTransform(startTransform);
        result.setCollisionShape(shape);
        result.setCollisionFlags(collisionFlags);
        _discreteDynamicsWorld.addCollisionObject(result, groups, filters);
        return result;
    }

    public void addRigidBody(RigidBody body) {
        _insertionQueue.add(new RigidBodyRequest(body, CollisionFilterGroups.DEFAULT_FILTER, (short)(CollisionFilterGroups.DEFAULT_FILTER | CollisionFilterGroups.STATIC_FILTER | CollisionFilterGroups.SENSOR_TRIGGER)));
    }

    public void addRigidBody(RigidBody body, List<CollisionGroup> groups, List<CollisionGroup> filter) {
        _insertionQueue.add(new RigidBodyRequest(body, combineGroups(groups), combineGroups(filter)));
    }

    public void addRigidBody(RigidBody body, short groups, short filter) {
        _insertionQueue.add(new RigidBodyRequest(body, groups, (short)(filter | CollisionFilterGroups.SENSOR_TRIGGER)));
    }

    public void removeRigidBody(RigidBody body) {
        _removalQueue.add(body);
    }

    public void removeCollider(GhostObject collider) {
        _discreteDynamicsWorld.removeCollisionObject(collider);
    }

    public Iterable<EntityRef> scanArea(AABB area, Iterable<CollisionGroup> collisionFilter) {
        // TODO: Add the aabbTest method from newer versions of bullet to TeraBullet, use that instead
        BoxShape shape = new BoxShape(area.getExtents());
        GhostObject scanObject = createCollider(area.getCenter(), shape, CollisionFilterGroups.SENSOR_TRIGGER, combineGroups(collisionFilter), CollisionFlags.NO_CONTACT_RESPONSE);
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
        closest.collisionFilterGroup = CollisionFilterGroups.SENSOR_TRIGGER;
        _discreteDynamicsWorld.rayTest(from, to, closest);
        if (closest.userData instanceof Vector3i) {
            return new HitResult(blockEntityRegistry.getOrCreateEntityAt((Vector3i)closest.userData), closest.hitPointWorld, closest.hitNormalWorld);
        } else if (closest.userData instanceof EntityRef) {
            return new HitResult((EntityRef) closest.userData, closest.hitPointWorld, closest.hitNormalWorld);
        }
        return new HitResult();
    }

    public HitResult rayTrace(Vector3f from, Vector3f direction, float distance, CollisionGroup ... collisionGroups) {
        Vector3f to = new Vector3f(direction);
        to.scale(distance);
        to.add(from);

        short filter = combineGroups(collisionGroups);

        CollisionWorld.ClosestRayResultWithUserDataCallback closest = new CollisionWorld.ClosestRayResultWithUserDataCallback(from, to);
        closest.collisionFilterGroup = CollisionFilterGroups.ALL_FILTER;
        closest.collisionFilterMask = filter;
        _discreteDynamicsWorld.rayTest(from, to, closest);
        if (closest.userData instanceof Vector3i) {
            return new HitResult(blockEntityRegistry.getOrCreateEntityAt((Vector3i)closest.userData), closest.hitPointWorld, closest.hitNormalWorld, (Vector3i)closest.userData);
        } else if (closest.userData instanceof EntityRef) {
            return new HitResult((EntityRef) closest.userData, closest.hitPointWorld, closest.hitNormalWorld);
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
            _discreteDynamicsWorld.stepSimulation(delta, 8);
            PerformanceMonitor.endActivity();
        } catch (Exception e) {
            logger.error("Error running simulation step.", e);
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
