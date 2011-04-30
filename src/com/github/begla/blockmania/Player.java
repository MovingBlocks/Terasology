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
 *  under the License.
 */
package com.github.begla.blockmania;

import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.ArrayList;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector3f;
import static org.lwjgl.opengl.GL11.*;

/**
 * This class contains all functions regarding the player's actions,
 * movement and persective rendering.
 * 
 * @author Benjamin Glatzel <benjamin.glawwtzel@me.com>
 */
public class Player extends RenderableObject {

    private boolean _jump = false;
    private int _selectedBlockType = 0;
    private boolean _demoAutoFlyMode = false;
    private boolean _godMode = false;
    private int _wSpeed = Configuration.WALKING_SPEED;
    private double _yaw = 135d;
    private double _pitch;
    private Vector3f _acc = new Vector3f();
    private float _gravity = 0.0f;
    private World _parent = null;

    /**
     * Positions the player within the world adjusts the player's view accordingly.
     */
    @Override
    public void render() {
        glRotatef((float) _pitch, 1f, 0f, 0f);
        glRotatef((float) _yaw, 0f, 1f, 0f);
        glTranslatef(-_position.x, -_position.y, -_position.z);

        RayFaceIntersection is = calcSelectedBlock();

        if (Configuration.SHOW_PLACING_BOX) {
            // Display the currently looked at block

            if (is != null) {

//                glPointSize(5f);
//                glBegin(GL_POINTS);
//                glVertex3f(is.getIntersectPoint().x, is.getIntersectPoint().y, is.getIntersectPoint().z);
//                glEnd();

                int bpX = (int) is.getBlockPos().x;
                int bpY = (int) is.getBlockPos().y;
                int bpZ = (int) is.getBlockPos().z;

                glColor3f(1.0f, 1.0f, 1.0f);

                glBegin(GL_LINES);
                glVertex3f(bpX - 0.5f, bpY - 0.5f, bpZ - 0.5f);
                glVertex3f(bpX + 0.5f, bpY - 0.5f, bpZ - 0.5f);

                glVertex3f(bpX - 0.5f, bpY - 0.5f, bpZ + 0.5f);
                glVertex3f(bpX + 0.5f, bpY - 0.5f, bpZ + 0.5f);

                glVertex3f(bpX - 0.5f, bpY + 0.5f, bpZ + 0.5f);
                glVertex3f(bpX + 0.5f, bpY + 0.5f, bpZ + 0.5f);

                glVertex3f(bpX - 0.5f, bpY + 0.5f, bpZ - 0.5f);
                glVertex3f(bpX + 0.5f, bpY + 0.5f, bpZ - 0.5f);

                glVertex3f(bpX - 0.5f, bpY - 0.5f, bpZ - 0.5f);
                glVertex3f(bpX - 0.5f, bpY - 0.5f, bpZ + 0.5f);

                glVertex3f(bpX + 0.5f, bpY - 0.5f, bpZ - 0.5f);
                glVertex3f(bpX + 0.5f, bpY - 0.5f, bpZ + 0.5f);

                glVertex3f(bpX - 0.5f, bpY + 0.5f, bpZ - 0.5f);
                glVertex3f(bpX - 0.5f, bpY + 0.5f, bpZ + 0.5f);

                glVertex3f(bpX + 0.5f, bpY + 0.5f, bpZ - 0.5f);
                glVertex3f(bpX + 0.5f, bpY + 0.5f, bpZ + 0.5f);

                glVertex3f(bpX - 0.5f, bpY - 0.5f, bpZ - 0.5f);
                glVertex3f(bpX - 0.5f, bpY + 0.5f, bpZ - 0.5f);

                glVertex3f(bpX + 0.5f, bpY - 0.5f, bpZ - 0.5f);
                glVertex3f(bpX + 0.5f, bpY + 0.5f, bpZ - 0.5f);

                glVertex3f(bpX - 0.5f, bpY - 0.5f, bpZ + 0.5f);
                glVertex3f(bpX - 0.5f, bpY + 0.5f, bpZ + 0.5f);

                glVertex3f(bpX + 0.5f, bpY - 0.5f, bpZ + 0.5f);
                glVertex3f(bpX + 0.5f, bpY + 0.5f, bpZ + 0.5f);

                glEnd();

            }
        }
    }

    /**
     * Updates the player's position etc.
     */
    @Override
    public void update(long delta) {
        yaw(Mouse.getDX() * 0.1f);
        pitch(Mouse.getDY() * 0.1f);

        processPlayerInteraction();
        processMovement(delta);
        updatePlayerPosition(delta);
    }

    /**
     * Yaws the player's point of view.
     * @param diff Amount of yawing to be applied.
     */
    public void yaw(float diff) {
        double nYaw = (_yaw + diff) % 360;

        if (nYaw < 0) {
            nYaw += 360;
        }

        _yaw = nYaw;
    }

    /**
     * Pitches the player's point of view.
     * @param diff Amount of pitching to be applied.
     */
    public void pitch(float diff) {
        double nPitch = (_pitch - diff) % 360;

        if (nPitch < 0) {
            nPitch += 360;
        }

        // TODO: Problematic if the mouse movement is very fast
        if ((nPitch > 0 && nPitch < 90) || (nPitch < 360 && nPitch > 270)) {
            _pitch = nPitch;
        }
    }

    /**
     * Moves the player forward.
     */
    public void walkForward() {
        _acc.x += (double) _wSpeed * Math.sin(Math.toRadians(_yaw)) * Math.cos(Math.toRadians(_pitch));

        if (_godMode) {
            _acc.y -= (double) _wSpeed * Math.sin(Math.toRadians(_pitch));
        }

        _acc.z -= _wSpeed * Math.cos(Math.toRadians(_yaw)) * Math.cos(Math.toRadians(_pitch));
    }

    /*
     * Moves the player backward.
     */
    public void walkBackwards() {
        _acc.x -= (double) _wSpeed * Math.sin(Math.toRadians(_yaw)) * Math.cos(Math.toRadians(_pitch));

        if (_godMode) {
            _acc.y += (double) _wSpeed * Math.sin(Math.toRadians(_pitch));
        }

        _acc.z += (double) _wSpeed * Math.cos(Math.toRadians(_yaw)) * Math.cos(Math.toRadians(_pitch));
    }

    /*
     * Lets the player strafe left.
     */
    public void strafeLeft() {
        _acc.x += (double) _wSpeed * Math.sin(Math.toRadians(_yaw - 90));
        _acc.z -= (double) _wSpeed * Math.cos(Math.toRadians(_yaw - 90));
    }

    /*
     * Lets the player strafe right.
     */
    public void strafeRight() {
        _acc.x += (double) _wSpeed * Math.sin(Math.toRadians(_yaw + 90));
        _acc.z -= (double) _wSpeed * Math.cos(Math.toRadians(_yaw + 90));
    }

    /**
     * Lets the player jump. Yey.
     */
    public void jump() {
        _jump = true;
    }

    @Override
    public String toString() {
        Vector3f vD = viewDirection();
        return String.format("player (x: %.2f, y: %.2f, z: %.2f | x: %.2f, y: %.2f, z: %.2f | b: %d | gravity: %.2f | x: %.2f, y: %.2f, z:, %.2f)", _position.x, _position.y, _position.z, vD.x, vD.y, vD.z, _selectedBlockType, _gravity, _acc.x, _acc.y, _acc.z);
    }

    public Vector3f viewDirection() {
        Vector3f vD = new Vector3f((float) Math.sin(Math.toRadians(_yaw)) * (float) Math.cos(Math.toRadians(_pitch)), -1f * (float) Math.sin(Math.toRadians(_pitch)), -1 * (float) Math.cos(Math.toRadians(_pitch)) * (float) Math.cos(Math.toRadians(_yaw)));
        vD.normalise();

        return vD;
    }

    public RayFaceIntersection calcSelectedBlock() {
        ArrayList<RayFaceIntersection> inters = new ArrayList<RayFaceIntersection>();

        Vector3f vD = viewDirection();
        for (int x = -4; x < 4; x++) {
            for (int y = -4; y < 4; y++) {
                for (int z = -4; z < 4; z++) {
                    if (x != 0 || y != 0 || z != 0) {
                        ArrayList<RayFaceIntersection> iss = _parent.rayBlockIntersection((int) _position.x + x, (int) _position.y + y, (int) _position.z + z, _position, vD);
                        if (iss != null) {
                            inters.addAll(iss);
                        }
                    }
                }
            }
        }

        /**
         * Calculated the closest intersection.
         */
        if (inters.size() > 0) {
            Collections.sort(inters);
            return inters.get(0);
        }

        return null;
    }

    /**
     * Places a block.
     */
    public void placeBlock() {
        if (getParent() != null) {
            RayFaceIntersection is = calcSelectedBlock();
            if (is != null) {
                Vector3f blockPos = is.calcAdjacentBlockPos();
                getParent().setBlock((int) blockPos.x, (int) blockPos.y, (int) blockPos.z, _selectedBlockType, true);
            }
        }
    }

    /**
     * Removes a block.
     */
    public void removeBlock() {
        if (getParent() != null) {
            RayFaceIntersection is = calcSelectedBlock();
            if (is != null) {
                Vector3f blockPos = is.getBlockPos();
                getParent().setBlock((int) blockPos.x, (int) blockPos.y, (int) blockPos.z, 0x0, true);
            }
        }
    }

    private void processPlayerInteraction() {
        while (Keyboard.next() && Keyboard.getEventKeyState()) {
            if (Keyboard.getEventKey() == Keyboard.KEY_E) {
                placeBlock();
            } else if (Keyboard.getEventKey() == Keyboard.KEY_Q) {
                removeBlock();
            } else if (Keyboard.getEventKey() == Keyboard.KEY_R) {
                resetPlayer();
            } else if (Keyboard.getEventKey() == Keyboard.KEY_T) {
                RayFaceIntersection is = calcSelectedBlock();
                if (is != null) {
                    Vector3f blockPos = is.getBlockPos();
                    _parent.generateTree((int) blockPos.x, (int) blockPos.y, (int) blockPos.z, true);
                }
            } else if (Keyboard.getEventKey() == Keyboard.KEY_U) {
                _parent.updateAllChunks();
            } else if (Keyboard.getEventKey() == Keyboard.KEY_G) {
                this._godMode = !_godMode;
            } else if (Keyboard.getEventKey() == Keyboard.KEY_H) {
                this._demoAutoFlyMode = !_demoAutoFlyMode;
            } else if (Keyboard.getEventKey() == Keyboard.KEY_P) {
                Configuration.SHOW_PLACING_BOX = !Configuration.SHOW_PLACING_BOX;
            } else if (Keyboard.getEventKey() == Keyboard.KEY_I) {
                Configuration.SHOW_CHUNK_OUTLINES = !Configuration.SHOW_CHUNK_OUTLINES;
            } else if (Keyboard.getEventKey() == Keyboard.KEY_UP) {
                cycleBlockTypes(1);
            } else if (Keyboard.getEventKey() == Keyboard.KEY_DOWN) {
                cycleBlockTypes(-1);
            } else if (Keyboard.getEventKey() == Keyboard.KEY_SPACE) {
                if (!Keyboard.isRepeatEvent()) {
                    jump();
                }
            }
        }

        while (Mouse.next()) {
            if (Mouse.getEventButton() == 0 && Mouse.getEventButtonState() == true) {
                placeBlock();
            } else if (Mouse.getEventButton() == 1 && Mouse.getEventButtonState() == true) {
                removeBlock();
            }
        }
    }

    private void processMovement(long delta) {
        if (getParent() == null) {
            return;
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
            walkForward();
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
            walkBackwards();
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
            strafeLeft();
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
            strafeRight();
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
            _wSpeed = Configuration.RUNNING_SPEED;
        } else {
            _wSpeed = Configuration.WALKING_SPEED;
        }
    }

    private boolean verticalHitTest(Vector3f oldPosition, float delta) {
        int blockType = _parent.getBlock((int) (getPosition().x + 0.5f), (int) (getPosition().y - 1), (int) (getPosition().z + 0.5f));

        if (_gravity > -Configuration.MAX_GRAVITY) {
            _gravity -= 0.05f * delta;
        }

        if (blockType > 0 || _godMode) {
            _gravity = 0.0f;

            if (!_godMode) {
                _position.y = oldPosition.y;
            }

            return true;
        }

        return false;
    }

    private boolean horizontalHitTest(Vector3f oldPosition, float delta) {
        Vector3f dir = _acc.normalise(null);
        Vector3f blockPos = new Vector3f((int) (getPosition().x + 0.5f + dir.x * 0.1f), (int) (getPosition().y - 1), (int) (getPosition().z + 0.5f + dir.z * 0.1f));
        int blockType = _parent.getBlock((int) blockPos.x, (int) blockPos.y, (int) blockPos.z);

        getPosition().x += (_acc.x / 1000.0f) * delta;
        getPosition().z += (_acc.z / 1000.0f) * delta;

        if (blockType > 0) {
            _position = oldPosition;
            return true;
        }

        return false;
    }

    private void updatePlayerPosition(float delta) {
        Vector3f oldPosition = new Vector3f(_position);

        if (_demoAutoFlyMode) {
            _acc.x = 32.f;
            _acc.z = 32.f;
        }

        getPosition().y += (_acc.y / 1000.0f) * delta;
        getPosition().y += (_gravity / 1000.0f) * delta;

        boolean vHit = verticalHitTest(oldPosition, delta);

        if (vHit && _jump) {
            _jump = false;
            _gravity = Configuration.JUMP_INTENSITY;
        }

        horizontalHitTest(oldPosition, delta);

        _acc.x = 0;
        _acc.y = 0;
        _acc.z = 0;
    }

    /**
     * Resets the player's position.
     */
    public void resetPlayer() {
        _position = Helper.getInstance().calcPlayerOrigin();
    }

    /**
     * Returns the parent world.
     * @return the parent world
     */
    public World getParent() {
        return _parent;
    }

    /**
     * Sets the parent world an resets the player.
     * @param parent the parent world
     */
    public void setParent(World parent) {
        this._parent = parent;
        resetPlayer();
    }

    public void cycleBlockTypes(int upDown) {
        _selectedBlockType += upDown;

        if (_selectedBlockType < 0) {
            _selectedBlockType = 0;
        }
    }
}
