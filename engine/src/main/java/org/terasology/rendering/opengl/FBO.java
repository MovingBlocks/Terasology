/*
 * Copyright 2015 MovingBlocks
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
package org.terasology.rendering.opengl;

import org.lwjgl.opengl.GL11;

import static org.lwjgl.opengl.EXTFramebufferObject.GL_FRAMEBUFFER_EXT;
import static org.lwjgl.opengl.EXTFramebufferObject.glBindFramebufferEXT;

/**
 * Created by manu on 15.03.2015.
 */
public class FBO {
    public int fboId;
    public int textureId;
    public int depthStencilTextureId;
    public int depthStencilRboId;
    public int normalsTextureId;
    public int lightBufferTextureId;

    public int width;
    public int height;

    public void bind() {
        //if (this != currentlyBoundFbo) {
            glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, fboId);
        //    currentlyBoundFbo = this;
        //}
    }

    public void unbind() {
        //if (currentlyBoundFbo != null) {
            glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, 0);
        //    currentlyBoundFbo = null;
        //}
    }

    public void bindDepthTexture() {
        //if (currentlyBoundTextureId != depthStencilTextureId) {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, depthStencilTextureId);
        //currentlyBoundTextureId = depthStencilTextureId;
        //}
    }

    public void bindTexture() {
        //if (currentlyBoundTextureId != textureId) {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        //currentlyBoundTextureId = textureId;
        //}
    }

    public void bindNormalsTexture() {
        //if (currentlyBoundTextureId != normalsTextureId) {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, normalsTextureId);
        //currentlyBoundTextureId = normalsTextureId;
        //}
    }

    public void bindLightBufferTexture() {
        //if (currentlyBoundTextureId != lightBufferTextureId) {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, lightBufferTextureId);
        //currentlyBoundTextureId = lightBufferTextureId;
        //}
    }

    public void unbindTexture() {
        //if (currentlyBoundTextureId != 0) {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        //currentlyBoundTextureId = 0;
        //}
    }
}
