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
import org.terasology.game.Terasology;
import org.terasology.logic.characters.Player;
import org.terasology.logic.world.Chunk;
import org.terasology.model.blocks.Block;
import org.terasology.model.blocks.management.BlockManager;
import org.terasology.model.inventory.ItemBlock;
import org.terasology.rendering.interfaces.IGameObject;
import org.terasology.utilities.FastRandom;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Renders blocks using the Bullet physics library.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class BulletPhysicsRenderer implements IGameObject {

    /* SINGLETON */
    private static BulletPhysicsRenderer _instance;

    public enum BLOCK_SIZE {
        FULL_SIZE,
        HALF_SIZE,
        QUARTER_SIZE
    }

    private class BlockRigidBody extends RigidBody implements Comparable<BlockRigidBody> {
        private final byte _type;
        private final long _createdAt;

        public boolean _picked = false;

        public BlockRigidBody(RigidBodyConstructionInfo constructionInfo, byte type) {
            super(constructionInfo);
            _type = type;
            _createdAt = Terasology.getInstance().getTimeInMs();
        }

        public float distanceToPlayer() {
            Transform t = new Transform();
            getMotionState().getWorldTransform(t);
            Matrix4f tMatrix = new Matrix4f();
            t.getMatrix(tMatrix);

            Player player = Terasology.getInstance().getActivePlayer();

            Vector3f blockPlayer = new Vector3f();
            tMatrix.get(blockPlayer);
            blockPlayer.sub(new Vector3f(player.getPosition()));

            return blockPlayer.length();
        }

        public long calcAgeInMs() {
            return Terasology.getInstance().getTimeInMs() - _createdAt;
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

    private final ArrayList<BlockRigidBody> _temporaryBlocks = new ArrayList<BlockRigidBody>();
    private final ArrayList<BlockRigidBody> _lootableBlocks = new ArrayList<BlockRigidBody>();

    private HashSet<RigidBody> _chunks = new HashSet<RigidBody>();

    private final BoxShape _blockShape = new BoxShape(new Vector3f(0.5f, 0.5f, 0.5f));
    private final BoxShape _blockShapeHalf = new BoxShape(new Vector3f(0.25f, 0.25f, 0.25f));
    private final BoxShape _blockShapeQuarter = new BoxShape(new Vector3f(0.125f, 0.125f, 0.125f));

    private final CollisionDispatcher _dispatcher;
    private final BroadphaseInterface _broadphase;
    private final DefaultCollisionConfiguration _defaultCollisionConfiguration;
    private final SequentialImpulseConstraintSolver _sequentialImpulseConstraintSolver;
    private final DiscreteDynamicsWorld _discreteDynamicsWorld;

    public static BulletPhysicsRenderer getInstance() {
        if (_instance == null)
            _instance = new BulletPhysicsRenderer();

        return _instance;
    }

    private BulletPhysicsRenderer() {
        _broadphase = new DbvtBroadphase();
        _defaultCollisionConfiguration = new DefaultCollisionConfiguration();
        _dispatcher = new CollisionDispatcher(_defaultCollisionConfiguration);
        _sequentialImpulseConstraintSolver = new SequentialImpulseConstraintSolver();
        _discreteDynamicsWorld = new DiscreteDynamicsWorld(_dispatcher, _broadphase, _sequentialImpulseConstraintSolver, _defaultCollisionConfiguration);
        _discreteDynamicsWorld.setGravity(new Vector3f(0f, -10f, 0f));
    }

    public BlockRigidBody[] addLootableBlocks(Vector3f position, Block block) {
        FastRandom rand = Terasology.getInstance().getActiveWorldProvider().getRandom();
        BlockRigidBody result[] = new BlockRigidBody[8];

        for (int i = 0; i < block.getLootAmount(); i++) {
            // Position the smaller blocks
            Vector3f offsetPossition = new Vector3f((float) rand.randomDouble() * 0.5f, (float) rand.randomDouble() * 0.5f, (float) rand.randomDouble() * 0.5f);
            offsetPossition.add(position);

            result[i] = addBlock(offsetPossition, block.getId(), new Vector3f(0.0f, 4000f, 0.0f), BLOCK_SIZE.QUARTER_SIZE);

            if (result[i] != null)
                _lootableBlocks.add(result[i]);
        }

        return result;
    }

    public BlockRigidBody addTemporaryBlock(Vector3f position, byte type, BLOCK_SIZE size) {
        BlockRigidBody result = addBlock(position, type, size);

        if (result != null)
            _temporaryBlocks.add(result);

        return result;
    }

    public BlockRigidBody addTemporaryBlock(Vector3f position, byte type, Vector3f impulse, BLOCK_SIZE size) {
        BlockRigidBody result = addBlock(position, type, impulse, size);

        if (result != null)
            _temporaryBlocks.add(result);

        return result;
    }

    public BlockRigidBody addBlock(Vector3f position, byte type, BLOCK_SIZE size) {
        return addBlock(position, type, new Vector3f(0f, 0f, 0f), size);
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
    public BlockRigidBody addBlock(Vector3f position, byte type, Vector3f impulse, BLOCK_SIZE size) {
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

        _discreteDynamicsWorld.addRigidBody(rigidBlock);

        // Apply impulse
        rigidBlock.applyImpulse(impulse, new Vector3f(0.0f, 0.0f, 0.0f));

        return rigidBlock;
    }

    public void updateChunks() {
        ArrayList<Chunk> chunks = Terasology.getInstance().getActiveWorldRenderer().getChunksInProximity();
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
        _discreteDynamicsWorld.stepSimulation(1.0f / 60f, 7);

        FloatBuffer mBuffer = BufferUtils.createFloatBuffer(16);
        float[] mFloat = new float[16];

        GL11.glPushMatrix();
        Player player = Terasology.getInstance().getActiveWorldRenderer().getPlayer();
        GL11.glTranslated(-player.getPosition().x, -player.getPosition().y, -player.getPosition().z);

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

                block.renderWithLightValue(Terasology.getInstance().getActiveWorldRenderer().getRenderingLightValueAt(new Vector3d(t.origin)));

                GL11.glPopMatrix();
            }
        }

        GL11.glPopMatrix();
    }

    public void update() {
        updateChunks();
        removeTemporaryBlocks();
        checkForLootedBlocks();
    }

    private void checkForLootedBlocks() {
        Player player = Terasology.getInstance().getActivePlayer();

        for (int i = _lootableBlocks.size() - 1; i >= 0; i--) {
            BlockRigidBody b = _lootableBlocks.get(i);

            // Check if the block is close enough to the player
            if (b.distanceToPlayer() < 8.0f && !b._picked) {
                // Mark it as picked and remove it from the simulation
                b._picked = true;
            }

            // Block was marked as being picked
            if (b._picked) {
                // Animate the movement into the direction of the player
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
                    // Block was looted (and reached the player)
                    Block block = BlockManager.getInstance().getBlock(b.getType());
                    player.getInventory().addItem(new ItemBlock(block.getBlockGroup()), 1);

                    _lootableBlocks.remove(i);
                    _discreteDynamicsWorld.removeRigidBody(b);
                }
            }
        }
    }

    private void removeTemporaryBlocks() {
        if (_temporaryBlocks.size() > 0) {
            for (int i = _temporaryBlocks.size() - 1; i >= 0; i--) {
                if (!_temporaryBlocks.get(i).isActive() || _temporaryBlocks.get(i).calcAgeInMs() > 10000) {
                    _discreteDynamicsWorld.removeRigidBody(_temporaryBlocks.get(i));
                    _temporaryBlocks.remove(i);
                }
            }

            while (_temporaryBlocks.size() > 128) {
                _discreteDynamicsWorld.removeRigidBody(_temporaryBlocks.remove(0));
            }
        }
    }
}
