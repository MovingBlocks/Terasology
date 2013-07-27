/*
 * Copyright 2013 Moving Blocks
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.CoreRegistry;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.event.EventReceiver;
import org.terasology.entitySystem.event.EventSystem;
import org.terasology.math.AABB;
import org.terasology.math.Vector3i;
import org.terasology.performanceMonitor.PerformanceMonitor;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.OnChangedBlock;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.BlockComponent;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

/**
 * Renders blocks using the Bullet physics library.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
// TODO: Merge this with Physics System
public class BulletPhysics implements EventReceiver<OnChangedBlock> {

    private static final Logger logger = LoggerFactory.getLogger(BulletPhysics.class);

    private final Deque<RigidBodyRequest> insertionQueue = Lists.newLinkedList();
    private final Deque<RigidBody> removalQueue = Lists.newLinkedList();

    private final CollisionDispatcher dispatcher;
    private final BroadphaseInterface broadphase;
    private final CollisionConfiguration defaultCollisionConfiguration;
    private final SequentialImpulseConstraintSolver sequentialImpulseConstraintSolver;
    private final DiscreteDynamicsWorld discreteDynamicsWorld;
    private final BlockEntityRegistry blockEntityRegistry;
    private final CollisionGroupManager collisionGroupManager;
    private final PhysicsWorldWrapper wrapper;


    public BulletPhysics(WorldProvider world) {
        collisionGroupManager = CoreRegistry.get(CollisionGroupManager.class);

        broadphase = new DbvtBroadphase();
        broadphase.getOverlappingPairCache().setInternalGhostPairCallback(new GhostPairCallback());
        defaultCollisionConfiguration = new DefaultCollisionConfiguration();
        dispatcher = new CollisionDispatcher(defaultCollisionConfiguration);
        sequentialImpulseConstraintSolver = new SequentialImpulseConstraintSolver();
        discreteDynamicsWorld = new DiscreteDynamicsWorld(dispatcher, broadphase, sequentialImpulseConstraintSolver, defaultCollisionConfiguration);
        discreteDynamicsWorld.setGravity(new Vector3f(0f, -15f, 0f));
        blockEntityRegistry = CoreRegistry.get(BlockEntityRegistry.class);
        CoreRegistry.get(EventSystem.class).registerEventReceiver(this, OnChangedBlock.class, BlockComponent.class);

        wrapper = new PhysicsWorldWrapper(world);
        VoxelWorldShape worldShape = new VoxelWorldShape(wrapper);

        Matrix3f rot = new Matrix3f();
        rot.setIdentity();
        DefaultMotionState blockMotionState = new DefaultMotionState(new Transform(new Matrix4f(rot, new Vector3f(0, 0, 0), 1.0f)));
        RigidBodyConstructionInfo blockConsInf = new RigidBodyConstructionInfo(0, blockMotionState, worldShape, new Vector3f());
        RigidBody rigidBody = new RigidBody(blockConsInf);
        rigidBody.setCollisionFlags(CollisionFlags.STATIC_OBJECT | rigidBody.getCollisionFlags());
        discreteDynamicsWorld.addRigidBody(rigidBody, combineGroups(StandardCollisionGroup.WORLD), (short) (CollisionFilterGroups.ALL_FILTER ^ CollisionFilterGroups.STATIC_FILTER));
    }

    public void dispose() {
        discreteDynamicsWorld.destroy();
        wrapper.dispose();
    }

    public DynamicsWorld getWorld() {
        return discreteDynamicsWorld;
    }

    // TODO: Wrap ghost object?
    public PairCachingGhostObject createCollider(Vector3f pos, ConvexShape shape, List<CollisionGroup> groups, List<CollisionGroup> filters) {
        return createCollider(pos, shape, groups, filters, 0);
    }

    public static short combineGroups(CollisionGroup... groups) {
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
        discreteDynamicsWorld.addCollisionObject(result, groups, filters);
        return result;
    }

    public void addRigidBody(RigidBody body) {
        insertionQueue.add(new RigidBodyRequest(body, CollisionFilterGroups.DEFAULT_FILTER, (short) (CollisionFilterGroups.DEFAULT_FILTER | CollisionFilterGroups.STATIC_FILTER | CollisionFilterGroups.SENSOR_TRIGGER)));
    }

    public void addRigidBody(RigidBody body, List<CollisionGroup> groups, List<CollisionGroup> filter) {
        insertionQueue.add(new RigidBodyRequest(body, combineGroups(groups), combineGroups(filter)));
    }

    public void addRigidBody(RigidBody body, short groups, short filter) {
        insertionQueue.add(new RigidBodyRequest(body, groups, (short) (filter | CollisionFilterGroups.SENSOR_TRIGGER)));
    }

    public void removeRigidBody(RigidBody body) {
        removalQueue.add(body);
    }

    public void removeCollider(GhostObject collider) {
        discreteDynamicsWorld.removeCollisionObject(collider);
    }

    public Iterable<EntityRef> scanArea(AABB area, Iterable<CollisionGroup> collisionFilter) {
        // TODO: Add the aabbTest method from newer versions of bullet to TeraBullet, use that instead
        BoxShape shape = new BoxShape(area.getExtents());
        GhostObject scanObject = createCollider(area.getCenter(), shape, CollisionFilterGroups.SENSOR_TRIGGER, combineGroups(collisionFilter), CollisionFlags.NO_CONTACT_RESPONSE);
        // This in particular is overkill
        broadphase.calculateOverlappingPairs(dispatcher);
        List<EntityRef> result = Lists.newArrayList();
        for (int i = 0; i < scanObject.getNumOverlappingObjects(); ++i) {
            CollisionObject other = scanObject.getOverlappingObject(i);
            Object userObj = other.getUserPointer();
            if (userObj instanceof EntityRef) {
                result.add((EntityRef) userObj);
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
        discreteDynamicsWorld.rayTest(from, to, closest);
        if (closest.userData instanceof Vector3i) {
            return new HitResult(blockEntityRegistry.getEntityAt((Vector3i) closest.userData), closest.hitPointWorld, closest.hitNormalWorld);
        } else if (closest.userData instanceof EntityRef) {
            return new HitResult((EntityRef) closest.userData, closest.hitPointWorld, closest.hitNormalWorld);
        }
        return new HitResult();
    }

    public HitResult rayTrace(Vector3f from, Vector3f direction, float distance, CollisionGroup... collisionGroups) {
        Vector3f to = new Vector3f(direction);
        to.scale(distance);
        to.add(from);

        short filter = combineGroups(collisionGroups);

        CollisionWorld.ClosestRayResultWithUserDataCallback closest = new CollisionWorld.ClosestRayResultWithUserDataCallback(from, to);
        closest.collisionFilterGroup = CollisionFilterGroups.ALL_FILTER;
        closest.collisionFilterMask = filter;
        discreteDynamicsWorld.rayTest(from, to, closest);
        if (closest.userData instanceof Vector3i) {
            return new HitResult(blockEntityRegistry.getEntityAt((Vector3i) closest.userData), closest.hitPointWorld, closest.hitNormalWorld, (Vector3i) closest.userData);
        } else if (closest.userData instanceof EntityRef) {
            return new HitResult((EntityRef) closest.userData, closest.hitPointWorld, closest.hitNormalWorld);
        }
        return new HitResult();
    }

    @Override
    public void onEvent(OnChangedBlock event, EntityRef entity) {
        Vector3f min = event.getBlockPosition().toVector3f();
        min.sub(new Vector3f(0.6f, 0.6f, 0.6f));
        Vector3f max = event.getBlockPosition().toVector3f();
        max.add(new Vector3f(0.6f, 0.6f, 0.6f));
        discreteDynamicsWorld.awakenRigidBodiesInArea(min, max);
    }

    public void update(float delta) {
        processQueuedBodies();
        try {
            PerformanceMonitor.startActivity("Step Simulation");
            discreteDynamicsWorld.stepSimulation(delta, 8);
            PerformanceMonitor.endActivity();
        } catch (Exception e) {
            logger.error("Error running simulation step.", e);
        }
    }

    private synchronized void processQueuedBodies() {
        while (!insertionQueue.isEmpty()) {
            RigidBodyRequest request = insertionQueue.poll();
            discreteDynamicsWorld.addRigidBody(request.body, request.groups, request.filter);
        }
        while (!removalQueue.isEmpty()) {
            RigidBody body = removalQueue.poll();
            discreteDynamicsWorld.removeRigidBody(body);
        }
    }

    private static class RigidBodyRequest {
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
