/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.rendering.world.dag;

import org.lwjgl.opengl.GL11;
import org.terasology.context.Context;
import org.terasology.rendering.backdrop.BackdropRenderer;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.FrameBuffersManager;
import org.terasology.rendering.opengl.OpenGLUtil;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;

/**
 * Diagram of this node can be viewed from:
 * TODO: move diagram to the wiki when this part of the code is stable
 * - https://docs.google.com/drawings/d/1Iz7MA8Y5q7yjxxcgZW-0antv5kgx6NYkvoInielbwGU/edit?usp=sharing
 */
public class WorldReflectionNode implements Node {

    private FBO sceneReflected;
    private Context context;
    private BackdropRenderer backdropRenderer;
    private Camera playerCamera;

    public WorldReflectionNode(Context context, Camera playerCamera) {
        this.context = context;
        this.backdropRenderer = context.get(BackdropRenderer.class);
        this.playerCamera = playerCamera;
    }

    @Override
    public void initialise() {

    }

    @Override
    public void update(float deltaInSeconds) {
        this.sceneReflected = context.get(FrameBuffersManager.class).getFBO("sceneReflected");
    }

    @Override
    public void process() {
        OpenGLUtil.setViewportToSizeOf(sceneReflected);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        GL11.glCullFace(GL11.GL_FRONT);

        playerCamera.setReflected(true);

    }

    @Override
    public void postRender() {

    }

    @Override
    public void onDisposal() {
        //  TODO: long term plan to add deleteFBO("sceneReflected"); here
    }

    @Override
    public void onRemoval() {

    }

    @Override
    public void onInsert() {

    }

}
