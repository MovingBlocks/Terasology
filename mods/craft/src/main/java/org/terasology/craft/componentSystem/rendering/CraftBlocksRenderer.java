package org.terasology.craft.componentSystem.rendering;

import org.terasology.math.*;
import com.google.common.collect.Maps;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.Color;
import org.terasology.asset.Assets;
import org.terasology.componentSystem.RenderSystem;
import org.terasology.components.InventoryComponent;
import org.terasology.components.ItemComponent;
import org.terasology.craft.components.actions.CraftingActionComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.*;
import org.terasology.game.CoreRegistry;
import org.terasology.input.CameraTargetSystem;
import org.terasology.logic.manager.GUIManager;
import org.terasology.logic.manager.ShaderManager;
import org.terasology.math.AABB;
import org.terasology.math.TeraMath;
import org.terasology.model.inventory.Icon;
import org.terasology.rendering.assets.Font;
import org.terasology.rendering.assets.Texture;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.widgets.UIItemContainer;
import org.terasology.rendering.primitives.Mesh;
import org.terasology.rendering.primitives.MeshFactory;
import org.terasology.rendering.shader.ShaderProgram;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.BlockItemComponent;
import org.terasology.world.block.BlockPart;
import org.terasology.world.block.family.BlockFamily;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.glEnd;

/**
 * @author Small-Jeeper
 */
@RegisterComponentSystem(headedOnly = true)
public class CraftBlocksRenderer implements RenderSystem, EventHandlerSystem  {
    private Texture toolTipTexture;
    private Texture terrainTex;
    private WorldProvider worldProvider;
    private EntityManager entityManager;

    //flags for animate of nails
    private float[] animationValue = {0f, 0f, 0f};

    //offset position for nails
    private Vector3f[] mainOffset = {new Vector3f(0.6f, 1.6f, 0f), new Vector3f(0.3f, 0.6f, 0f), new Vector3f(0.1f, 0.3f, 0f)};

    //additional parametr for directing animation
    private int[] animationMarker = {-1, 1, -1};

    //distance from player to craft Block target
    private float distanceToTarget;

    //link to the result Entity
    private EntityRef resultItemContainer;

    //container which shows the result of craft
    private UIItemContainer guiCraftElement;
    private UIDisplayElement craftingCloudBackground;
    private UIDisplayElement craftingResultBackground;
    private UIDisplayElement craftingArrow;

    //cache for mesh
    private Map<String, Mesh> iconMeshes = Maps.newHashMap();
    private Font font = Assets.getFont("engine:default");
    @In
    private WorldRenderer worldRenderer;

    @Override
    public void initialise() {
        worldProvider = CoreRegistry.get(WorldProvider.class);
        entityManager = CoreRegistry.get(EntityManager.class);
        toolTipTexture = Assets.getTexture("craft:gui_craft");
        terrainTex = Assets.getTexture("engine:items");
        guiCraftElement = null;
        craftingCloudBackground = null;
        craftingResultBackground = null;
        craftingArrow = null;
    }

    @Override
    public void shutdown() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void renderTransparent() {
        Vector3f cubeSize = new Vector3f(0.3f, 0.3f, 0.3f);
        Vector3f cameraPosition = CoreRegistry.get(WorldRenderer.class).getActiveCamera().getPosition();
        EntityRef target = CoreRegistry.get(CameraTargetSystem.class).getTarget();
        boolean foundedTargetWithCraft = false;
        boolean foundedTarget          = false;

        for (EntityRef entity : entityManager.iteratorEntities(CraftingActionComponent.class)) {
            AABB aabb = null;
            CraftingActionComponent craftingActionComponent = entity.getComponent(CraftingActionComponent.class);

            foundedTarget = false;

            if( craftingActionComponent.getAllElements().size() == 0){
                continue;
            }


            BlockComponent blockComp = entity.getComponent(BlockComponent.class);
            if (blockComp != null) {
                Vector3f blockPos = new Vector3f(blockComp.getPosition().toVector3f());
                Block block = worldProvider.getBlock(blockPos);
                aabb = block.getBounds(blockPos);

                if( ! target.equals(EntityRef.NULL) && target.equals(entity)){
                    if( !craftingActionComponent.possibleItem.equals(EntityRef.NULL) ){
                        foundedTargetWithCraft = true;

                        if(resultItemContainer == null){
                            initResultItem();
                        }

                        if( !resultItemContainer.getComponent(InventoryComponent.class).itemSlots.get(0).equals(craftingActionComponent.possibleItem) ){
                            InventoryComponent inventoryComponent = resultItemContainer.getComponent(InventoryComponent.class);
                            inventoryComponent.itemSlots.set(0, craftingActionComponent.possibleItem);
                            resultItemContainer.saveComponent(inventoryComponent);
                        }

                        if( guiCraftElement == null ){
                            guiCraftElement = (UIItemContainer)CoreRegistry.get(GUIManager.class).getWindowById("hud").getElementById("craftElement");
                            craftingCloudBackground = CoreRegistry.get(GUIManager.class).getWindowById("hud").getElementById("craftingCloudBackground");
                            craftingResultBackground = CoreRegistry.get(GUIManager.class).getWindowById("hud").getElementById("craftingResultBackground");
                            craftingArrow = CoreRegistry.get(GUIManager.class).getWindowById("hud").getElementById("craftingArrow");
                        }

                      //  Vector3f distanceToBlock = new Vector3f(blockPos);
                       // distanceToBlock.sub(cameraPosition);
                       // distanceToTarget = distanceToBlock.length();

                        Vector3i distanceToBlock = new Vector3i(blockPos);
                        distanceToTarget = distanceToBlock.distance(new Vector3i(cameraPosition));

                        renderToolTip(blockPos, craftingActionComponent.possibleItem, !craftingActionComponent.isRefinement);
                    }

                    foundedTarget = true;
                }

                boolean notCurrentLevel = false;

                for( int y = 0; y < 3; y++ ){
                    ArrayList<EntityRef> currentItems = craftingActionComponent.getLevelElements(y);
                    notCurrentLevel = craftingActionComponent.getCurrentLevel() != y && foundedTarget;

                    if( currentItems == null ){
                        continue;
                    }

                    for( int x = 0; x < 3; x++ ){
                        for( int z = 0; z < 3; z++ ){
                            Vector3f bobOffset = new Vector3f(aabb.getMin());
                            bobOffset.add(new Vector3f(0.15f, 0.15f, 0.15f));
                            bobOffset.x += x*cubeSize.x;
                            bobOffset.y += y*cubeSize.y;
                            bobOffset.z += z*cubeSize.z;

                            int i = x*3 + z;

                            if( !currentItems.get(i).equals(EntityRef.NULL) ){
                                BlockItemComponent blockItem = currentItems.get(i).getComponent(BlockItemComponent.class);
                                ItemComponent heldItemComp = currentItems.get(i).getComponent(ItemComponent.class);
                                if (blockItem != null && blockItem.blockFamily != null) {
                                    renderBlock(blockItem.blockFamily, bobOffset, cameraPosition, notCurrentLevel);
                                }else if(heldItemComp != null){
                                    renderIcon(heldItemComp.icon, bobOffset, cameraPosition, notCurrentLevel);
                                }

                                if( !notCurrentLevel && heldItemComp != null && foundedTarget){
                                    renderToolTipCount(bobOffset,heldItemComp.stackCount>1?heldItemComp.stackCount:1);
                                }

                            }
                        }
                    }
                }
            }
        }

        if( guiCraftElement != null && !foundedTargetWithCraft && isGUIVisible() ){
            guiCraftElement.setEntity(EntityRef.NULL);
            guiCraftElement.setVisible(false);
            craftingCloudBackground.setVisible(false);
            craftingResultBackground.setVisible(false);
            craftingArrow.setVisible(false);
        }
    }

    private void renderBlock(BlockFamily blockFamily, Vector3f blockPos, Vector3f cameraPos, boolean notCurrentLevel) {
        // Adjust the brightness of the block according to the current position of the player
        ShaderProgram shader = ShaderManager.getInstance().getShaderProgram("block");
        shader.enable();

        Block activeBlock = blockFamily.getArchetypeBlock();

        // Apply biome and overall color offset
        Vector4f color = activeBlock.calcColorOffsetFor(BlockPart.CENTER, worldProvider.getBiomeProvider().getTemperatureAt(TeraMath.floorToInt(cameraPos.x), TeraMath.floorToInt(cameraPos.z)), worldProvider.getBiomeProvider().getHumidityAt(TeraMath.floorToInt(cameraPos.x), TeraMath.floorToInt(cameraPos.z)));
        shader.setFloat3("colorOffset", color.x, color.y, color.z);

        if(notCurrentLevel){
            shader.setFloat("alpha", 0.3f);
        }

        shader.setInt("textured", 1);

        glEnable(GL11.GL_BLEND);

        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glAlphaFunc(GL_GREATER, 0.1f);
        if (activeBlock.isTranslucent()) {
            glEnable(GL11.GL_ALPHA_TEST);
        }

        glMatrixMode(GL11.GL_MODELVIEW);
        glPushMatrix();
        glTranslatef(blockPos.x - cameraPos.x, blockPos.y - cameraPos.y, blockPos.z - cameraPos.z);
        glScalef(0.3f, 0.3f, 0.3f);
        activeBlock.renderWithLightValue(worldRenderer.getRenderingLightValue());
        glPopMatrix();

        if (activeBlock.isTranslucent()) {
            glDisable(GL11.GL_ALPHA_TEST);
        }
        glDisable(GL11.GL_BLEND);
        if(notCurrentLevel){
            shader.setFloat("alpha", 1.0f);
        }
    }

    private void renderIcon(String iconName, Vector3f offset, Vector3f cameraPos, boolean notCurrentLevel) {
        ShaderProgram shader = ShaderManager.getInstance().getShaderProgram("block");
        shader.enable();

        if(notCurrentLevel){
            shader.setFloat("alpha", 0.3f);
        }

        shader.setInt("textured", 0);
        shader.setFloat("light", worldRenderer.getRenderingLightValue());

        Mesh itemMesh = iconMeshes.get(iconName);
        if (itemMesh == null) {
            Icon icon = Icon.get(iconName);
            itemMesh = MeshFactory.getInstance().generateItemMesh(icon.getTextureSimpleUri(), icon.getX(), icon.getY());
            iconMeshes.put(iconName, itemMesh);
        }
        glPushMatrix();
        glTranslatef(offset.x - cameraPos.x, offset.y - cameraPos.y + 0.15f, offset.z - cameraPos.z);
        glScalef(0.1f, 0.1f, 0.1f);
        glRotatef(90f, 1f, 0f, 0f);
        itemMesh.render();
        glPopMatrix();
    }

    private void renderToolTip(Vector3f worldPos, EntityRef entity, boolean renderFar){

        if(distanceToTarget >= 3.5f){
            if(renderFar){
                updateOffsetAnimation(0,  0.002f, 0.2f);
                updateOffsetAnimation(1,  0.001f, 0.1f);
                updateOffsetAnimation(2,  0.0005f, 0.05f);

                if( isGUIVisible() ){
                    guiCraftElement.setVisible(false);
                    craftingCloudBackground.setVisible(false);
                    craftingResultBackground.setVisible(false);
                    craftingArrow.setVisible(false);
                }

                renderBackgroundToolTip(worldPos, mainOffset[0], null);
                renderIconTollTip(worldPos, mainOffset[0], entity);
                renderNail(worldPos);
            }
        }else{
            if( !guiCraftElement.getEntity().equals(resultItemContainer) ){
                guiCraftElement.setEntity(resultItemContainer);
            }
            if( !isGUIVisible() ){
                guiCraftElement.setVisible(true);
                craftingCloudBackground.setVisible(true);
                craftingResultBackground.setVisible(true);
                craftingArrow.setVisible(true);
            }
        }

    }

    private boolean isGUIVisible(){
        return  guiCraftElement.isVisible() && craftingCloudBackground.isVisible() && craftingResultBackground.isVisible() && craftingArrow.isVisible();
    }
    
    private void updateOffsetAnimation(int index, float step,  float maxOffset){
        animationValue[index] += step;

        if(animationValue[index] <= 0 || animationValue[index] >= maxOffset){
            animationMarker[index] *= -1;
            animationValue[index] = 0f;
        }

        mainOffset[index].y += animationMarker[index]*step;
    }

    private void applyOrientation(){
        // Fetch the current modelview matrix
        final FloatBuffer model = BufferUtils.createFloatBuffer(16);
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, model);

        // And undo all rotations and scaling
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (i == j)
                    model.put(i * 4 + j, 1.0f);
                else
                    model.put(i * 4 + j, 0.0f);
            }
        }

        GL11.glLoadMatrix(model);
    }

    private void initResultItem(){
        resultItemContainer = entityManager.create("core:chest");
        InventoryComponent inventory = resultItemContainer.getComponent(InventoryComponent.class);
        inventory.itemSlots.clear();
        inventory.itemSlots.add(EntityRef.NULL);
        resultItemContainer.saveComponent(inventory);
    }

    private void renderBackgroundToolTip(Vector3f worldPos, Vector3f offset, Vector3f scale){
        WorldRenderer worldRenderer = CoreRegistry.get(WorldRenderer.class);
        Vector3f cameraPosition = worldRenderer.getActiveCamera().getPosition();
        Vector3f position = new Vector3f(worldPos.x - cameraPosition.x, worldPos.y - cameraPosition.y, worldPos.z - cameraPosition.z);
        renderBillboardBegin( (toolTipTexture != null ? toolTipTexture.getId() : 0), position, offset, scale);
        glScalef(1.2f, 1f, 1f);
        drawQuad(new Vector2f(0f, 0f), new Vector2f(0.4335f, 0.3554f));
        renderBillboardEnd();

    }

    private void renderToolTipCount(Vector3f worldPos, float count){
        WorldRenderer worldRenderer = CoreRegistry.get(WorldRenderer.class);
        Vector3f cameraPosition = worldRenderer.getActiveCamera().getPosition();
        worldPos.sub(cameraPosition);
        Vector3f position = new Vector3f(worldPos);
        position.y += 0.2f;

        renderBillboardBegin(-1, position, null, new Vector3f(0.005f, -0.005f, 0.005f));
        font.drawString(0,0, String.format("%.0f",count), Color.orange);
        renderBillboardEnd();

    }

    private void renderIconTollTip(Vector3f worldPos, Vector3f offset, EntityRef entity){

        ItemComponent itemComponent  = entity.getComponent(ItemComponent.class);

        if (itemComponent == null)
            return;

        WorldRenderer worldRenderer = CoreRegistry.get(WorldRenderer.class);
        Vector3f cameraPosition = worldRenderer.getActiveCamera().getPosition();

        Block block = null;
        Icon icon   = null;


        if (itemComponent.icon.isEmpty()) {
            BlockItemComponent blockItem = entity.getComponent(BlockItemComponent.class);
            if (blockItem != null) {
                block = blockItem.blockFamily.getArchetypeBlock();
            }
        } else {
            icon = Icon.get(itemComponent.icon);
        }

        if( icon!=null ){
            glDisable(GL11.GL_CULL_FACE);
            glDepthMask ( false );
            ShaderManager.getInstance().enableDefaultTextured();
            glBindTexture(GL11.GL_TEXTURE_2D, terrainTex != null ? terrainTex.getId() : 0);
            glEnable(GL11.GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        }

        glPushMatrix();
        glTranslated(worldPos.x - cameraPosition.x, worldPos.y - cameraPosition.y, worldPos.z - cameraPosition.z);
        glPushMatrix();

        applyOrientation();
        if(offset != null){
            glTranslatef(offset.x, offset.y, offset.z);
        }
        glScalef(0.2f, 0.2f, 0.2f);

        if(block != null){
            GL11.glRotatef(170f, -1f, 0f, 0f);
            GL11.glRotatef(-16f, 0f, 1f, 0f);
            block.renderWithLightValue(1.0f);
        }else{
            float offsetX = icon.getX() * 0.0625f,
                  offsetY = icon.getY() * 0.0625f;
            float sizeX   = icon.getX() * 0.0625f + 0.0624f,
                  sizeY   = icon.getY() * 0.0625f + 0.0624f;

            glBegin(GL_QUADS);
            GL11.glTexCoord2f(offsetX, sizeY);
            GL11.glVertex3f(-0.5f, -0.5f, 0.0f);

            GL11.glTexCoord2f(sizeX, sizeY);
            GL11.glVertex3f(0.5f, -0.5f, 0.0f);

            GL11.glTexCoord2f(sizeX, offsetY);
            GL11.glVertex3f(0.5f, 0.5f, 0.0f);

            GL11.glTexCoord2f(offsetX, offsetY);
            GL11.glVertex3f(-0.5f, 0.5f, 0.0f);
            glEnd();
        }

        glPopMatrix();
        glPopMatrix();

        if( icon!=null ){
            glDisable(GL11.GL_BLEND);
            glDepthMask ( true );
            glEnable(GL11.GL_CULL_FACE);
        }
    }

    private void renderNail(Vector3f wordPos){
        renderBackgroundToolTip(wordPos, mainOffset[1], new Vector3f( 0.6f,0.6f,0.6f));
        renderBackgroundToolTip(wordPos, mainOffset[2], new Vector3f( 0.3f,0.3f,0.3f));
    }

    private void drawQuad(Vector2f texturePosition, Vector2f textureSize){
        glBegin(GL_QUADS);
        glColor4f(1f, 1f, 1f, 1f);
        GL11.glTexCoord2f(texturePosition.x, texturePosition.y + textureSize.y);
        GL11.glVertex3f(-0.5f, -0.5f, 0.0f);

        GL11.glTexCoord2f(texturePosition.x + textureSize.x, texturePosition.y + textureSize.y);
        GL11.glVertex3f(0.5f, -0.5f, 0.0f);

        GL11.glTexCoord2f(texturePosition.x + textureSize.x, texturePosition.y);
        GL11.glVertex3f(0.5f, 0.5f, 0.0f);

        GL11.glTexCoord2f(texturePosition.x, texturePosition.y);
        GL11.glVertex3f(-0.5f, 0.5f, 0.0f);
        glEnd();
    }

    private void renderBillboardBegin(int textureId, Vector3f position, Vector3f offset, Vector3f scale){

        glDisable(GL11.GL_CULL_FACE);

        if( textureId>=0 ){
            ShaderManager.getInstance().enableDefaultTextured();
            glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        }

        glDepthMask (false);
        glEnable(GL11.GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glPushMatrix();
        glTranslated(position.x, position.y, position.z);

        glPushMatrix();
        applyOrientation();

        if(offset != null){
            glTranslatef(offset.x, offset.y, offset.z);
        }

        if(scale != null){
            glScalef(scale.x, scale.y, scale.z);
        }
    }

    private void renderBillboardEnd(){
        glPopMatrix();
        glPopMatrix();
        glDisable(GL11.GL_BLEND);
        glDepthMask ( true );
        glEnable(GL11.GL_CULL_FACE);
    }

    @Override
    public void renderOverlay() {
    }

    @Override
    public void renderFirstPerson() {
    }

    @Override
    public void renderShadows() {
    }

    @Override
    public void renderOpaque() {
    }
}
