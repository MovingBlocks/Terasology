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
package org.terasology.rendering.physics;

import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.broadphase.DbvtBroadphase;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.Transform;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.terasology.componentSystem.items.InventorySystem;
import org.terasology.components.ItemComponent;
import org.terasology.entityFactory.BlockItemFactory;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.PrefabManager;
import org.terasology.events.inventory.ReceiveItemEvent;
import org.terasology.game.ComponentSystemManager;
import org.terasology.game.CoreRegistry;
import org.terasology.game.Terasology;
import org.terasology.game.Timer;
import org.terasology.logic.LocalPlayer;
import org.terasology.logic.manager.AudioManager;
import org.terasology.logic.world.Chunk;
import org.terasology.model.blocks.Block;
import org.terasology.model.blocks.management.BlockManager;
import org.terasology.rendering.interfaces.IGameObject;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.utilities.FastRandom;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Renders blocks using the Bullet physics library.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class BulletPhysicsRenderer implements IGameObject {

    private class BlockRigidBody extends RigidBody implements Comparable<BlockRigidBody> {
        private final byte _type;
        private final long _createdAt;

        public boolean _temporary = false;
        public boolean _picked = false;

        public BlockRigidBody(RigidBodyConstructionInfo constructionInfo, byte type) {
            super(constructionInfo);
            _type = type;
            _createdAt = _timer.getTimeInMs();
        }

        // TODO: This won't work in multiplayer
        public float distanceToPlayer() {
            Transform t = new Transform();
            getMotionState().getWorldTransform(t);
            Matrix4f tMatrix = new Matrix4f();
            t.getMatrix(tMatrix);

            Vector3f blockPlayer = new Vector3f();
            tMatrix.get(blockPlayer);
            blockPlayer.sub(new Vector3f(CoreRegistry.get(LocalPlayer.class).getPosition()));

            return blockPlayer.length();
        }

        public long calcAgeInMs() {
            return _timer.getTimeInMs() - _createdAt;
        }

        public byte getType() {
            return _type;
        }

        public int compareTo(BlockRigidBody blockRigidBody) {
            if (blockRigidBody.calcAgeInMs() == calcAgeInMs()) {
                return 0;
            }

            if (blockRigidBody.calcAgeInMs() > calcAgeInMs())
                return 1;
            else
                return -1;
        }
    }


    public enum BLOCK_SIZE {
        FULL_SIZE,
        HALF_SIZE,
        QUARTER_SIZE
    }

    private static final int MAX_TEMP_BLOCKS = 128;
    private Logger _logger = Logger.getLogger(getClass().getName());

    private final LinkedList<RigidBody> _insertionQueue = new LinkedList<RigidBody>();
    private final ArrayList<BlockRigidBody> _blocks = new ArrayList<BlockRigidBody>();

    private HashSet<RigidBody> _chunks = new HashSet<RigidBody>();

    private final BoxShape _blockShape = new BoxShape(new Vector3f(0.5f, 0.5f, 0.5f));
    private final BoxShape _blockShapeHalf = new BoxShape(new Vector3f(0.25f, 0.25f, 0.25f));
    private final BoxShape _blockShapeQuarter = new BoxShape(new Vector3f(0.125f, 0.125f, 0.125f));

    private final CollisionDispatcher _dispatcher;
    private final BroadphaseInterface _broadphase;
    private final DefaultCollisionConfiguration _defaultCollisionConfiguration;
    private final SequentialImpulseConstraintSolver _sequentialImpulseConstraintSolver;
    private final DiscreteDynamicsWorld _discreteDynamicsWorld;

    private final BlockItemFactory _blockItemFactory;

    private Timer _timer;
    private FastRandom _random = new FastRandom();
    private final WorldRenderer _parent;

    public BulletPhysicsRenderer(WorldRenderer parent) {
        _broadphase = new DbvtBroadphase();
        _defaultCollisionConfiguration = new DefaultCollisionConfiguration();
        _dispatcher = new CollisionDispatcher(_defaultCollisionConfiguration);
        _sequentialImpulseConstraintSolver = new SequentialImpulseConstraintSolver();
        _discreteDynamicsWorld = new DiscreteDynamicsWorld(_dispatcher, _broadphase, _sequentialImpulseConstraintSolver, _defaultCollisionConfiguration);
        _discreteDynamicsWorld.setGravity(new Vector3f(0f, -10f, 0f));
        _parent = parent;
        _blockItemFactory = new BlockItemFactory(CoreRegistry.get(EntityManager.class), CoreRegistry.get(PrefabManager.class));
        _timer = CoreRegistry.get(Timer.class);
    }

    public BlockRigidBody[] addLootableBlocks(Vector3f position, Block block) {
        BlockRigidBody result[] = new BlockRigidBody[8];

        for (int i = 0; i < block.getLootAmount(); i++) {
            // Position the smaller blocks
            Vector3f offsetPossition = new Vector3f((float) _random.randomDouble() * 0.5f, (float) _random.randomDouble() * 0.5f, (float) _random.randomDouble() * 0.5f);
            offsetPossition.add(position);

            result[i] = addBlock(offsetPossition, block.getId(), new Vector3f(0.0f, 4000f, 0.0f), BLOCK_SIZE.QUARTER_SIZE, false);
        }

        return result;
    }

    public BlockRigidBody addTemporaryBlock(Vector3f position, byte type, BLOCK_SIZE size) {
        BlockRigidBody result = addBlock(position, type, size, true);

        return result;
    }

    public BlockRigidBody addTemporaryBlock(Vector3f position, byte type, Vector3f impulse, BLOCK_SIZE size) {
        BlockRigidBody result = addBlock(position, type, impulse, size, true);

        return result;
    }

    public BlockRigidBody addBlock(Vector3f position, byte type, BLOCK_SIZE size, boolean temporary) {
        return addBlock(position, type, new Vector3f(0f, 0f, 0f), size, temporary);
    }

    /**
     * Adds a new physics block to be rendered as a rigid body. Translucent blocks are ignored.
     *
     * @param position The position
     * @param type     The block type
     * @param impulse  An impulse
     * @param size     The size of the block
     * @return The created rigid body (if any)
     */
    public synchronized BlockRigidBody addBlock(Vector3f position, byte type, Vector3f impulse, BLOCK_SIZE size, boolean temporary) {
        if (temporary && _blocks.size() > MAX_TEMP_BLOCKS)
            return null;

        BoxShape shape = _blockShape;
        Block block = BlockManager.getInstance().getBlock(type);

        if (block.isTranslucent())
            return null;

        if (size == BLOCK_SIZE.HALF_SIZE)
            shape = _blockShapeHalf;
        else if (size == BLOCK_SIZE.QUARTER_SIZE)
            shape = _blockShapeQuarter;

        Matrix3f rot = new Matrix3f();
        rot.setIdentity();

        DefaultMotionState blockMotionState = new DefaultMotionState(new Transform(new Matrix4f(rot, position, 1.0f)));

        Vector3f fallInertia = new Vector3f();
        shape.calculateLocalInertia(block.getMass(), fallInertia);

        RigidBodyConstructionInfo blockCI = new RigidBodyConstructionInfo(block.getMass(), blockMotionState, shape, fallInertia);

        BlockRigidBody rigidBlock = new BlockRigidBody(blockCI, type);
        rigidBlock.setRestitution(0.0f);
        rigidBlock.setAngularFactor(0.5f);
        rigidBlock.setFriction(0.5f);
        rigidBlock._temporary = temporary;

        // Apply impulse
        rigidBlock.applyImpulse(impulse, new Vector3f(0.0f, 0.0f, 0.0f));

        _insertionQueue.add(rigidBlock);

        return rigidBlock;
    }

    public void updateChunks() {
        ArrayList<Chunk> chunks = CoreRegistry.get(WorldRenderer.class).getChunksInProximity();
        HashSet<RigidBody> newBodies = new HashSet<RigidBody>();

        for (int i = 0; i < 32 && i < chunks.size(); i++) {
            final Chunk chunk = chunks.get(i);

            if (chunk != null) {
                chunk.updateRigidBody();

                RigidBody c = chunk.getRigidBody();

                if (c != null) {
                    newBodies.add(c);

                    if (!_chunks.contains(c)) {
                        _discreteDynamicsWorld.addRigidBody(c);
                    }
                }
            }
        }

        for (RigidBody body : _chunks) {
            if (!newBodies.contains(body)) {
                _discreteDynamicsWorld.removeRigidBody(body);
            }
        }

        _chunks = newBodies;
    }

    public void render() {

        FloatBuffer mBuffer = BufferUtils.createFloatBuffer(16);
        float[] mFloat = new float[16];

        GL11.glPushMatrix();

        Vector3d cameraPosition = _parent.getActiveCamera().getPosition();
        GL11.glTranslated(-cameraPosition.x, -cameraPosition.y, -cameraPosition.z);

        List<CollisionObject> collisionObjects = _discreteDynamicsWorld.getCollisionObjectArray();

        for (CollisionObject co : collisionObjects) {
            if (co.getClass().equals(BlockRigidBody.class)) {
                BlockRigidBody br = (BlockRigidBody) co;
                Block block = BlockManager.getInstance().getBlock(br.getType());

                Transform t = new Transform();
                br.getMotionState().getWorldTransform(t);

                t.getOpenGLMatrix(mFloat);
                mBuffer.put(mFloat);
                mBuffer.flip();

                GL11.glPushMatrix();
                GL11.glMultMatrix(mBuffer);

                if (br.getCollisionShape() == _blockShapeHalf)
                    GL11.glScalef(0.5f, 0.5f, 0.5f);
                else if (br.getCollisionShape() == _blockShapeQuarter)
                    GL11.glScalef(0.25f, 0.25f, 0.25f);

                block.renderWithLightValue(_parent.getRenderingLightValueAt(new Vector3d(t.origin)));

                GL11.glPopMatrix();
            }
        }

        GL11.glPopMatrix();
    }

    public void update(float delta) {
        addQueuedBodies();

        try {
            _discreteDynamicsWorld.stepSimulation(delta, 3);
        } catch (Exception e) {
            _logger.log(Level.WARNING, "Somehow Bullet Physics managed to throw an exception again. Go along: " + e.toString());
        }

        updateChunks();
        removeTemporaryBlocks();
        checkForLootedBlocks();
    }

    private synchronized void addQueuedBodies() {
        while (!_insertionQueue.isEmpty()) {
            RigidBody body = _insertionQueue.poll();

            if (body instanceof BlockRigidBody)
                _blocks.add((BlockRigidBody) body);

            _discreteDynamicsWorld.addRigidBody(body);
        }
    }

    private void checkForLootedBlocks() {
        LocalPlayer player = CoreRegistry.get(LocalPlayer.class);

        for (int i = _blocks.size() - 1; i >= 0; i--) {
            BlockRigidBody b = _blocks.get(i);

            if (b._temporary)
                continue;

            // Check if the block is close enough to the player
            if (b.distanceToPlayer() < 8.0f && !b._picked) {
                // Mark it as picked and remove it from the simulation
                b._picked = true;
            }

            // Block was marked as being picked
            if (b._picked && b.distanceToPlayer() < 32.0f) {
                // Animate the movement in direction of the player
                if (b.distanceToPlayer() > 1.0) {
                    Transform t = new Transform();
                    b.getMotionState().getWorldTransform(t);

                    Matrix4f tMatrix = new Matrix4f();
                    t.getMatrix(tMatrix);

                    Vector3f blockPlayer = new Vector3f();
                    tMatrix.get(blockPlayer);
                    blockPlayer.sub(new Vector3f(player.getPosition()));
                    blockPlayer.normalize();
                    blockPlayer.scale(-16000f);

                    b.applyCentralImpulse(blockPlayer);
                } else {
                    // TODO: Handle full inventories
                    // TODO: Loot blocks should be entities
                    // Block was looted (and reached the player)
                    Block block = BlockManager.getInstance().getBlock(b.getType());
                    EntityRef blockItem = _blockItemFactory.newInstance(block.getBlockFamily());

                    player.getEntity().send(new ReceiveItemEvent(blockItem));
                    ItemComponent itemComp = blockItem.getComponent(ItemComponent.class);
                    if (itemComp != null && !itemComp.container.exists()) {
                        blockItem.destroy();
                    }
                    AudioManager.play("Loot");

                    _blocks.remove(i);
                    _discreteDynamicsWorld.removeRigidBody(b);
                }
            }
        }
    }

    private void removeTemporaryBlocks() {
        if (_blocks.size() > 0) {
            for (int i = _blocks.size() - 1; i >= 0; i--) {
                if (!_blocks.get(i)._temporary)
                    continue;

                if (!_blocks.get(i).isActive() || _blocks.get(i).calcAgeInMs() > 10000) {
                    _discreteDynamicsWorld.removeRigidBody(_blocks.get(i));
                    _blocks.remove(i);
                }
            }
        }
    }
}
