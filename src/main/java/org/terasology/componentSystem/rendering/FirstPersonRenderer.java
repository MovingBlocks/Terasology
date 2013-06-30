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
package org.terasology.componentSystem.rendering;

import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glRotatef;
import static org.lwjgl.opengl.GL11.glScalef;
import static org.lwjgl.opengl.GL11.glTranslatef;

import java.util.Map;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;

import org.lwjgl.opengl.GL11;
import org.terasology.asset.Assets;
import org.terasology.componentSystem.RenderSystem;
import org.terasology.components.InventoryComponent;
import org.terasology.components.ItemComponent;
import org.terasology.components.LocalPlayerComponent;
import org.terasology.entitySystem.In;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.manager.GUIManager;
import org.terasology.rendering.gui.widgets.UIItemContainer;
import org.terasology.rendering.assets.GLSLShaderProgramInstance;
import org.terasology.world.block.BlockItemComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.RegisterComponentSystem;
import org.terasology.logic.LocalPlayer;
import org.terasology.logic.manager.ShaderManager;
import org.terasology.model.inventory.Icon;
import org.terasology.physics.character.CharacterMovementComponent;
import org.terasology.rendering.assets.Texture;
import org.terasology.rendering.primitives.Mesh;
import org.terasology.rendering.primitives.MeshFactory;
import org.terasology.rendering.primitives.Tessellator;
import org.terasology.rendering.primitives.TessellatorHelper;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.family.BlockFamily;

import com.google.common.collect.Maps;

/**
 * @author Immortius <immortius@gmail.com>
 */
@RegisterComponentSystem(headedOnly = true)
public class FirstPersonRenderer implements RenderSystem {

    @In
    private WorldProvider worldProvider;
    @In
    private LocalPlayer localPlayer;
    @In
    private WorldRenderer worldRenderer;
    private Mesh handMesh;
    private Texture handTex;

    private Map<String, Mesh> iconMeshes = Maps.newHashMap();

    @Override
    public void initialise() {
        Vector2f texPos = new Vector2f(40.0f * 0.015625f, 32.0f * 0.03125f);
        Vector2f texWidth = new Vector2f(4.0f * 0.015625f, -12.0f * 0.03125f);

        Tessellator tessellator = new Tessellator();
        TessellatorHelper.addBlockMesh(tessellator, new Vector4f(1, 1, 1, 1), texPos, texWidth, 1.0f, 1.0f, 0.9f, 0.0f, 0.0f, 0.0f);
        handMesh = tessellator.generateMesh();
        handTex = Assets.getTexture("engine:char");
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void renderOpaque() {

    }

    @Override
    public void renderAlphaBlend() {
    }

    @Override
    public void renderFirstPerson() {
        CharacterMovementComponent charMoveComp = localPlayer.getEntity().getComponent(CharacterMovementComponent.class);
        float bobOffset = calcBobbingOffset(charMoveComp.footstepDelta / charMoveComp.distanceBetweenFootsteps, (float) java.lang.Math.PI / 8f, 0.05f, 1f);
        float handMovementAnimationOffset = localPlayer.getEntity().getComponent(LocalPlayerComponent.class).handAnimation;

        UIItemContainer toolbar = (UIItemContainer) CoreRegistry.get(GUIManager.class).getWindowById("hud").getElementById("toolbar");
        int invSlotIndex = localPlayer.getEntity().getComponent(LocalPlayerComponent.class).selectedTool + toolbar.getSlotStart();
        EntityRef heldItem = localPlayer.getEntity().getComponent(InventoryComponent.class).itemSlots.get(invSlotIndex);
        ItemComponent heldItemComp = heldItem.getComponent(ItemComponent.class);
        BlockItemComponent blockItem = heldItem.getComponent(BlockItemComponent.class);
        if (blockItem != null && blockItem.blockFamily != null) {
            renderBlock(blockItem.blockFamily, bobOffset, handMovementAnimationOffset);
        } else if (heldItemComp != null && heldItemComp.renderWithIcon) {
            renderIcon(heldItemComp.icon, bobOffset, handMovementAnimationOffset);
        } else {
            renderHand(bobOffset, handMovementAnimationOffset);
        }

    }

    @Override
    public void renderShadows() {
    }

    @Override
    public void renderOverlay() {

    }

    private void renderHand(float bobOffset, float handMovementAnimationOffset) {
        GLSLShaderProgramInstance shader = ShaderManager.getInstance().getShaderProgramInstance("block");
        shader.setActiveFeatures(GLSLShaderProgramInstance.ShaderProgramFeatures.FEATURE_DEFERRED_LIGHTING.getValue()
            | GLSLShaderProgramInstance.ShaderProgramFeatures.FEATURE_USE_MATRIX_STACK.getValue());

        shader.enable();
        shader.setFloat("light", worldRenderer.getRenderingLightValue());
        glBindTexture(GL11.GL_TEXTURE_2D, handTex.getId());

        glPushMatrix();
        glTranslatef(0.8f, -0.8f + bobOffset - handMovementAnimationOffset * 0.5f, -1.0f - handMovementAnimationOffset * 0.5f);
        glRotatef(-45f - handMovementAnimationOffset * 64.0f, 1.0f, 0.0f, 0.0f);
        glRotatef(35f, 0.0f, 1.0f, 0.0f);
        glTranslatef(0f, 0.25f, 0f);
        glScalef(0.3f, 0.6f, 0.3f);

        handMesh.render();

        glPopMatrix();

        shader.setActiveFeatures(0);
    }

    private void renderIcon(String iconName, float bobOffset, float handMovementAnimationOffset) {
        GLSLShaderProgramInstance shader = ShaderManager.getInstance().getShaderProgramInstance("block");
        shader.setActiveFeatures(GLSLShaderProgramInstance.ShaderProgramFeatures.FEATURE_DEFERRED_LIGHTING.getValue()
                | GLSLShaderProgramInstance.ShaderProgramFeatures.FEATURE_USE_MATRIX_STACK.getValue());

        shader.enable();

        shader.setBoolean("textured", false);
        shader.setFloat("light", worldRenderer.getRenderingLightValue());

        glPushMatrix();

        glTranslatef(1.0f, -0.7f + bobOffset - handMovementAnimationOffset * 0.5f, -1.5f - handMovementAnimationOffset * 0.5f);
        glRotatef(-handMovementAnimationOffset * 64.0f, 1.0f, 0.0f, 0.0f);
        glRotatef(-20f, 1.0f, 0.0f, 0.0f);
        glRotatef(-80f, 0.0f, 1.0f, 0.0f);
        glRotatef(45f, 0.0f, 0.0f, 1.0f);
        glScalef(0.75f, 0.75f, 0.75f);

        Mesh itemMesh = iconMeshes.get(iconName);
        if (itemMesh == null) {
            Icon icon = Icon.get(iconName);            
            itemMesh = MeshFactory.getInstance().generateItemMesh(icon.getTextureSimpleUri(), icon.getX(), icon.getY());
            iconMeshes.put(iconName, itemMesh);
        }

        itemMesh.render();

        glPopMatrix();

        shader.setActiveFeatures(0);
    }

    private void renderBlock(BlockFamily blockFamily, float bobOffset, float handMovementAnimationOffset) {
        Block activeBlock = blockFamily.getArchetypeBlock();
        //Vector3f playerPos = localPlayer.getPosition();

        // Adjust the brightness of the block according to the current position of the player
        GLSLShaderProgramInstance shader = ShaderManager.getInstance().getShaderProgramInstance("block");
        shader.setActiveFeatures(GLSLShaderProgramInstance.ShaderProgramFeatures.FEATURE_DEFERRED_LIGHTING.getValue()
            | GLSLShaderProgramInstance.ShaderProgramFeatures.FEATURE_USE_MATRIX_STACK.getValue());

        shader.enable();

        // Apply biome and overall color offset
        // TODO: Make me look nice not ugly!
        //Vector4f color = activeBlock.calcColorOffsetFor(BlockPart.CENTER, worldProvider.getBiomeProvider().getTemperatureAt(TeraMath.floorToInt(playerPos.x), TeraMath.floorToInt(playerPos.z)), worldProvider.getBiomeProvider().getHumidityAt(TeraMath.floorToInt(playerPos.x), TeraMath.floorToInt(playerPos.z)));
        //shader.setFloat3("colorOffset", color.x, color.y, color.z);

        glPushMatrix();

        glTranslatef(1.0f, -0.7f + bobOffset - handMovementAnimationOffset * 0.5f, -1.5f - handMovementAnimationOffset * 0.5f);
        glRotatef(-25f - handMovementAnimationOffset * 64.0f, 1.0f, 0.0f, 0.0f);
        glRotatef(35f, 0.0f, 1.0f, 0.0f);
        glTranslatef(0f, 0.1f, 0f);
        glScalef(0.75f, 0.75f, 0.75f);

        float lightValue = worldRenderer.getRenderingLightValue();

        //  Blocks with a luminance > 0.0 shouldn't be affected by block light
        if (blockFamily.getArchetypeBlock().getLuminance() > 0.0) {
            lightValue = 1.0f;
        }

        activeBlock.renderWithLightValue(lightValue);

        glPopMatrix();

        shader.setActiveFeatures(0);
    }

    private float calcBobbingOffset(float counter, float phaseOffset, float amplitude, float frequency) {
        return (float) java.lang.Math.sin(2 * Math.PI * counter * frequency + phaseOffset) * amplitude;
    }


}
