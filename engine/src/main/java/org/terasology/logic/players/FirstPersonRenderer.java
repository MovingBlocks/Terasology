/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.logic.players;

import org.lwjgl.opengl.GL11;
import org.terasology.asset.Assets;
import org.terasology.assets.Asset;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.RenderSystem;
import org.terasology.logic.characters.CharacterHeldItemComponent;
import org.terasology.logic.characters.CharacterMovementComponent;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.math.geom.Vector2f;
import org.terasology.math.geom.Vector4f;
import org.terasology.registry.In;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.mesh.Mesh;
import org.terasology.rendering.assets.shader.ShaderProgramFeature;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.assets.texture.TextureRegion;
import org.terasology.rendering.iconmesh.IconMeshFactory;
import org.terasology.rendering.primitives.Tessellator;
import org.terasology.rendering.primitives.TessellatorHelper;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.world.block.Block;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.items.BlockItemComponent;

import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glRotatef;
import static org.lwjgl.opengl.GL11.glScalef;
import static org.lwjgl.opengl.GL11.glTranslatef;

/**
 */
@RegisterSystem(RegisterMode.CLIENT)
public class FirstPersonRenderer extends BaseComponentSystem implements RenderSystem {

    @In
    private LocalPlayer localPlayer;
    @In
    private WorldRenderer worldRenderer;

    private Mesh handMesh;
    private Texture handTex;

    @Override
    public void initialise() {
        Vector2f texPos = new Vector2f(40.0f * 0.015625f, 32.0f * 0.03125f);
        Vector2f texWidth = new Vector2f(4.0f * 0.015625f, -12.0f * 0.03125f);

        Tessellator tessellator = new Tessellator();
        TessellatorHelper.addBlockMesh(tessellator, new Vector4f(1, 1, 1, 1), texPos, texWidth, 1.0f, 1.0f, 0.9f, 0.0f, 0.0f, 0.0f);
        handMesh = tessellator.generateMesh();
        handTex = Assets.getTexture("engine:char").get();
    }

    @Override
    public void renderOpaque() {

    }

    @Override
    public void renderAlphaBlend() {

    }

    @Override
    public void renderFirstPerson() {
        CharacterHeldItemComponent characterHeldItemComponent = localPlayer.getCharacterEntity().getComponent(CharacterHeldItemComponent.class);
        if (characterHeldItemComponent == null) {
            return;
        }
        CharacterMovementComponent charMoveComp = localPlayer.getCharacterEntity().getComponent(CharacterMovementComponent.class);
        if (charMoveComp == null) {
            return;
        }
        float bobOffset = calcBobbingOffset(charMoveComp.footstepDelta, (float) java.lang.Math.PI / 8f, 0.05f);
        float handMovementAnimationOffset = characterHeldItemComponent.handAnimation;

        EntityRef heldItem = characterHeldItemComponent.selectedItem;
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
        if (handTex.isLoaded()) {
            Material shader = Assets.getMaterial("engine:prog.block").get();
            shader.activateFeature(ShaderProgramFeature.FEATURE_USE_MATRIX_STACK);

            shader.enable();
            shader.setFloat("sunlight", worldRenderer.getSunlightValue(), true);
            shader.setFloat("blockLight", worldRenderer.getBlockLightValue(), true);

            glBindTexture(GL11.GL_TEXTURE_2D, handTex.getId());

            glPushMatrix();
            glTranslatef(0.8f, -0.8f + bobOffset - handMovementAnimationOffset * 0.5f, -1.0f - handMovementAnimationOffset * 0.5f);
            glRotatef(-45f - handMovementAnimationOffset * 64.0f, 1.0f, 0.0f, 0.0f);
            glRotatef(35f, 0.0f, 1.0f, 0.0f);
            glTranslatef(0f, 0.25f, 0f);
            glScalef(0.3f, 0.6f, 0.3f);

            handMesh.render();

            glPopMatrix();

            shader.deactivateFeature(ShaderProgramFeature.FEATURE_USE_MATRIX_STACK);
        }
    }

    private void renderIcon(TextureRegion iconTexture, float bobOffset, float handMovementAnimationOffset) {
        if (iconTexture != null) {
            Material shader = Assets.getMaterial("engine:prog.block").get();
            shader.activateFeature(ShaderProgramFeature.FEATURE_USE_MATRIX_STACK);

            shader.enable();

            shader.setBoolean("textured", false, true);

            shader.setFloat("sunlight", worldRenderer.getSunlightValue(), true);
            shader.setFloat("blockLight", worldRenderer.getBlockLightValue(), true);

            glPushMatrix();

            glTranslatef(1.0f, -0.7f + bobOffset - handMovementAnimationOffset * 0.5f, 0.25f + (-1.5f - handMovementAnimationOffset * 0.5f));
            glRotatef(-handMovementAnimationOffset * 64.0f, 1.0f, 0.0f, 0.0f);
            glRotatef(-20f, 1.0f, 0.0f, 0.0f);
            glRotatef(-80f, 0.0f, 1.0f, 0.0f);
            glRotatef(45f, 0.0f, 0.0f, 1.0f);
            float scale = 0.75f;
            glScalef(scale, scale, scale);

            if (iconTexture instanceof Asset<?>) {
                Mesh itemMesh = IconMeshFactory.getIconMesh(iconTexture);
                itemMesh.render();
            }

            glPopMatrix();

            shader.deactivateFeature(ShaderProgramFeature.FEATURE_USE_MATRIX_STACK);
        }
    }

    private void renderBlock(BlockFamily blockFamily, float bobOffset, float handMovementAnimationOffset) {
        Block activeBlock = blockFamily.getArchetypeBlock();

        // Adjust the brightness of the block according to the current position of the player
        Material shader = Assets.getMaterial("engine:prog.block").get();
        shader.activateFeature(ShaderProgramFeature.FEATURE_USE_MATRIX_STACK);

        shader.enable();

        glPushMatrix();

        glTranslatef(1.0f, -0.7f + bobOffset - handMovementAnimationOffset * 0.5f, -1.5f - handMovementAnimationOffset * 0.5f);
        glRotatef(-25f - handMovementAnimationOffset * 64.0f, 1.0f, 0.0f, 0.0f);
        glRotatef(35f, 0.0f, 1.0f, 0.0f);
        glTranslatef(0f, 0.1f, 0f);
        glScalef(0.75f, 0.75f, 0.75f);

        float blockLight = worldRenderer.getBlockLightValue();
        float sunlight = worldRenderer.getSunlightValue();

        //  Blocks with a luminance > 0.0 shouldn't be affected by block light
        if (blockFamily.getArchetypeBlock().getLuminance() > 0.0) {
            blockLight = 1.0f;
        }

        activeBlock.renderWithLightValue(sunlight, blockLight);

        glPopMatrix();

        shader.deactivateFeature(ShaderProgramFeature.FEATURE_USE_MATRIX_STACK);
    }

    private float calcBobbingOffset(float counter, float phaseOffset, float amplitude) {
        return (float) java.lang.Math.sin(2 * Math.PI * counter + phaseOffset) * amplitude;
    }


}
