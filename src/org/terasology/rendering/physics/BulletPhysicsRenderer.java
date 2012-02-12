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
import com.bulletphysics.collision.shapes.CollisionShape;
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
import org.terasology.model.blocks.BlockManager;
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

    private final CollisionShape _blockShape = new BoxShape(new Vector3f(0.25f, 0.25f, 0.25f));

    private final CollisionDispatcher _dispatcher;
    private final BroadphaseInterface _broadphase;
    private final DefaultCollisionConfiguration _defaultCollisionConfiguration;
    private final SequentialImpulseConstraintSolver _sequentialImpulseConstraintSolver;
    private final DiscreteDynamicsWorld _discreteDynamicsWorld;

    private static final Vector3f[] _positionOffsets = new Vector3f[]{new Vector3f(1, 1, -1), new Vector3f(1, -1, -1), new Vector3f(-1, 1, -1), new Vector3f(-1, -1, -1),
            new Vector3f(1, 1, 1), new Vector3f(1, -1, 1), new Vector3f(-1, 1, 1), new Vector3f(-1, -1, 1)};

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

    public void addBlock(Vector3f position, byte type) {
        for (int i = 0; i < 8; i++) {
            Matrix3f rot = new Matrix3f();
            rot.setIdentity();

            // Position the smaller blocks
            Vector3f pos = new Vector3f(position);
            _positionOffsets[i].scale(0.25f);
            pos.add(_positionOffsets[i]);

            DefaultMotionState blockMotionState = new DefaultMotionState(new Transform(new Matrix4f(rot, pos, 1.0f)));

            Vector3f fallInertia = new Vector3f();
            _blockShape.calculateLocalInertia(100f, fallInertia);

            RigidBodyConstructionInfo blockCI = new RigidBodyConstructionInfo(100f, blockMotionState, _blockShape, fallInertia);
            blockCI.restitution = 0.0f;

            BlockRigidBody block = new BlockRigidBody(blockCI, type);
            _discreteDynamicsWorld.addRigidBody(block);

            // Make sure the blocks move at least
            FastRandom rand = Terasology.getInstance().getActiveWorldProvider().getRandom();
            block.applyCentralForce(new Vector3f(rand.randomInt() % 80000 + 40000, rand.randomInt() % 80000 + 40000, rand.randomInt() % 80000 + 40000));

            _blocks.add(block);
        }
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
        _discreteDynamicsWorld.stepSimulation(Terasology.getInstance().getDelta() / 1000f, 7);

        TextureManager.getInstance().bindTexture("terrain");
        ShaderManager.getInstance().enableShader("block");
        ShaderParameters params = ShaderManager.getInstance().getShaderParameters("block");

        Player player = Terasology.getInstance().getActiveWorldRenderer().getPlayer();
        FloatBuffer mBuffer = BufferUtils.createFloatBuffer(16);
        float[] mFloat = new float[16];

        GL11.glPushMatrix();
        GL11.glTranslated(-player.getPosition().x, -player.getPosition().y, -player.getPosition().z);

        for (BlockRigidBody b : _blocks) {
            Transform t = new Transform();
            b.getMotionState().getWorldTransform(t);

            t.getOpenGLMatrix(mFloat);
            mBuffer.put(mFloat);
            mBuffer.flip();

            GL11.glPushMatrix();
            GL11.glMultMatrix(mBuffer);
            GL11.glScalef(0.5f, 0.5f, 0.5f);

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
