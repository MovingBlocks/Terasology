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
import org.terasology.logic.manager.ShaderManager;
import org.terasology.logic.manager.TextureManager;
import org.terasology.logic.world.Chunk;
import org.terasology.model.blocks.Block;
import org.terasology.model.blocks.management.BlockManager;
import org.terasology.rendering.interfaces.IGameObject;
import org.terasology.rendering.shader.ShaderParameters;
import org.terasology.utilities.FastRandom;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashSet;

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

        public BlockRigidBody(RigidBodyConstructionInfo constructionInfo, byte type) {
            super(constructionInfo);
            _type = type;
            _createdAt = Terasology.getInstance().getTime();
        }

        public long calcAgeInMs() {
            return Terasology.getInstance().getTime() - _createdAt;
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

    public BlockRigidBody[] addHarvestedMiniBlocks(Vector3f position, byte type) {
        FastRandom rand = Terasology.getInstance().getActiveWorldProvider().getRandom();
        BlockRigidBody result[] = new BlockRigidBody[8];

        for (int i = 0; i < 8; i++) {
            // Position the smaller blocks
            Vector3f offsetPossition = new Vector3f((float) rand.randomDouble() * 0.5f, (float) rand.randomDouble() * 0.5f, (float) rand.randomDouble() * 0.5f);
            offsetPossition.add(position);

            result[i] = addBlock(offsetPossition, type, new Vector3f(0.0f, 4000f, 0.0f), BLOCK_SIZE.QUARTER_SIZE);
        }

        return result;
    }

    public BlockRigidBody addBlock(Vector3f position, byte type, BLOCK_SIZE size) {
        return addBlock(position, type, new Vector3f(0f, 0f, 0f), size);
    }

    public BlockRigidBody addBlock(Vector3f position, byte type, Vector3f impulse, BLOCK_SIZE size) {
        BoxShape shape = _blockShape;
        Block block = BlockManager.getInstance().getBlock(type);

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

        _blocks.add(rigidBlock);

        // Apply impulse
        rigidBlock.applyImpulse(impulse, new Vector3f(0.0f, 0.0f, 0.0f));

        return rigidBlock;
    }

    public void updateChunks() {
        ArrayList<Chunk> chunks = Terasology.getInstance().getActiveWorldRenderer().getChunksInProximity();
        HashSet<RigidBody> newBodies = new HashSet<RigidBody>();

        for (int i = 0; i < 16 && i < chunks.size(); i++) {
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

        TextureManager.getInstance().bindTexture("terrain");
        ShaderManager.getInstance().enableShader("block");
        ShaderParameters params = ShaderManager.getInstance().getShaderParameters("block");

        FloatBuffer mBuffer = BufferUtils.createFloatBuffer(16);
        float[] mFloat = new float[16];

        GL11.glPushMatrix();
        Player player = Terasology.getInstance().getActiveWorldRenderer().getPlayer();
        GL11.glTranslated(-player.getPosition().x, -player.getPosition().y, -player.getPosition().z);

        for (BlockRigidBody b : _blocks) {
            Transform t = new Transform();
            b.getMotionState().getWorldTransform(t);

            t.getOpenGLMatrix(mFloat);
            mBuffer.put(mFloat);
            mBuffer.flip();

            GL11.glPushMatrix();
            GL11.glMultMatrix(mBuffer);

            if (b.getCollisionShape() == _blockShapeHalf)
                GL11.glScalef(0.5f, 0.5f, 0.5f);
            else if (b.getCollisionShape() == _blockShapeQuarter)
                GL11.glScalef(0.25f, 0.25f, 0.25f);

            float lightValue = Terasology.getInstance().getActiveWorldRenderer().getRenderingLightValueAt(new Vector3d(t.origin));
            params.setFloat("light", lightValue);

            BlockManager.getInstance().getBlock(b.getType()).render();

            GL11.glPopMatrix();
        }

        GL11.glPopMatrix();

        ShaderManager.getInstance().enableShader(null);
    }

    public void update() {
        updateChunks();
        removeBlocks();
    }

    private void removeBlocks() {
        if (_blocks.size() > 0) {
            for (int i = _blocks.size() - 1; i >= 0; i--) {
                if (!_blocks.get(i).isActive() || _blocks.get(i).calcAgeInMs() > 10000) {
                    _discreteDynamicsWorld.removeRigidBody(_blocks.get(i));
                    _blocks.remove(i);
                }
            }

            while (_blocks.size() > 128) {
                _discreteDynamicsWorld.removeRigidBody(_blocks.remove(0));
            }
        }
    }
}
