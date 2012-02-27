/*
 *  Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.terasology.logic.characters;

import org.newdawn.slick.openal.Audio;
import org.terasology.game.Terasology;
import org.terasology.logic.manager.AudioManager;
import org.terasology.logic.manager.ShaderManager;
import org.terasology.model.structures.AABB;
import org.terasology.rendering.primitives.Mesh;
import org.terasology.rendering.primitives.Tessellator;
import org.terasology.rendering.primitives.TessellatorHelper;
import org.terasology.rendering.shader.ShaderProgram;
import org.terasology.rendering.world.WorldRenderer;

import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import static org.lwjgl.opengl.GL11.*;

/**
 * Funny slime wobbling around in the world. Sweet!
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public final class GelatinousCube extends Character {

    private final Mesh _mesh;
    private static final Vector3f[] COLORS = {new Vector3f(1.0f, 1.0f, 0.2f), new Vector3f(1.0f, 0.2f, 0.2f), new Vector3f(0.2f, 1.0f, 0.2f), new Vector3f(1.0f, 1.0f, 0.2f)};

    private long _lastChangeOfDirectionAt = Terasology.getInstance().getTimeInMs();
    private final Vector3d _movementTarget = new Vector3d();
    private boolean _followingPlayer = false;

    public final int _randomColorId;
    public float _randomSize = 1.0f;

    public GelatinousCube(WorldRenderer parent) {
        super(parent);

        _randomSize = (float) (((_parent.getWorldProvider().getRandom().randomDouble() + 1.0) / 2.0) * 0.8 + 0.2);
        _randomColorId = Math.abs(_parent.getWorldProvider().getRandom().randomInt()) % COLORS.length;

        Tessellator tessellator = new Tessellator();
        TessellatorHelper.addBlockMesh(tessellator, new Vector4f(COLORS[_randomColorId].x, COLORS[_randomColorId].y, COLORS[_randomColorId].y, 1.0f), 0.8f * _randomSize, 0.8f, 0.6f, 0f, 0f, 0f);
        TessellatorHelper.addBlockMesh(tessellator, new Vector4f(COLORS[_randomColorId].x, COLORS[_randomColorId].y, COLORS[_randomColorId].y, 0.6f), 1.0f * _randomSize, 1.0f, 0.8f, 0f, 0f, 0f);
        _mesh = tessellator.generateMesh();
    }

    @Override
    protected void initAudio() {
        _footstepSounds = new Audio[5];
        _footstepSounds[0] = AudioManager.getInstance().loadSound("Slime1");
        _footstepSounds[1] = AudioManager.getInstance().loadSound("Slime2");
        _footstepSounds[2] = AudioManager.getInstance().loadSound("Slime3");
        _footstepSounds[3] = AudioManager.getInstance().loadSound("Slime4");
        _footstepSounds[4] = AudioManager.getInstance().loadSound("Slime5");
    }

    public void update(double delta) {
        super.update(delta);
    }

    public void render() {
        super.render();

        glPushMatrix();

        Vector3d playerPosition = Terasology.getInstance().getActivePlayer().getPosition();
        glTranslated(getPosition().x - playerPosition.x, getPosition().y - playerPosition.y, getPosition().z - playerPosition.z);
        glRotatef((float) _yaw, 0f, 1f, 0f);

        ShaderProgram shader = ShaderManager.getInstance().getShaderProgram("gelatinousCube");

        shader.enable();
        shader.setFloat4("colorOffset", COLORS[_randomColorId].x, COLORS[_randomColorId].y, COLORS[_randomColorId].z, 1.0f);
        shader.setFloat("light", _parent.getRenderingLightValueAt(getPosition()));

        _mesh.render();

        glPopMatrix();
    }

    public void processMovement() {
        _movementDirection.set(0, 0, 0);

        double distanceToPlayer = distanceSquaredTo(_parent.getPlayer().getPosition());

        if (distanceToPlayer > 6 && distanceToPlayer < 16) {
            _movementTarget.set(_parent.getPlayer().getPosition());
            _followingPlayer = true;
        } else {
            if (Terasology.getInstance().getTimeInMs() - _lastChangeOfDirectionAt > 12000 || _followingPlayer) {
                _movementTarget.set(getPosition().x + _parent.getWorldProvider().getRandom().randomDouble() * 500, getPosition().y, getPosition().z + _parent.getWorldProvider().getRandom().randomDouble() * 500);
                _lastChangeOfDirectionAt = Terasology.getInstance().getTimeInMs();
                _followingPlayer = false;
            }
        }

        lookAt(_movementTarget);
        walkForward();
    }

    protected AABB generateAABBForPosition(Vector3d p) {
        float sizeHalf = _randomSize / 2f;
        return new AABB(p, new Vector3d(sizeHalf, sizeHalf, sizeHalf));
    }

    public AABB getAABB() {
        return generateAABBForPosition(getPosition());
    }

    @Override
    protected void handleVerticalCollision() {
        // Do nothing
    }

    @Override
    protected void handleHorizontalCollision() {
        jump();
    }
}