package org.terasology.pathfinding.rendering;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.Color;
import org.terasology.asset.Assets;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.manager.ShaderManager;
import org.terasology.math.Vector3i;
import org.terasology.model.structures.BlockPosition;
import org.terasology.rendering.assets.Font;
import org.terasology.rendering.world.WorldRenderer;

import javax.vecmath.Vector3f;
import java.nio.FloatBuffer;
import java.util.*;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.glDepthMask;
import static org.lwjgl.opengl.GL11.glEnable;

/**
 * @author synopia
 */
public class InfoGrid {
    private Font font;
    private HashMap<Vector3i, GridPosition> grid = new HashMap<Vector3i, GridPosition>();
    private List<Category> categories = new ArrayList<Category>();
    private Stack<Color> colors = new Stack<Color>();

    private class GridPosition {
        private Vector3i position;
        private HashMap<String, String> entries = new HashMap<String, String>();
    }
    private class Category {
        private String name;
        private Color color;
    }

    public InfoGrid() {
        font = Assets.getFont("engine:default");
        colors.push(Color.orange);
        colors.push(Color.yellow);
        colors.push(Color.cyan);
        colors.push(Color.blue);
        colors.push(Color.red);
        colors.push(Color.darkGray);
    }

    private GridPosition create( Vector3i pos ) {
        GridPosition gp = grid.get(pos);
        if( gp==null ) {
            gp = new GridPosition();
            gp.position = pos;
            grid.put(pos, gp);
        }
        return gp;
    }

    public Category addCategory( String name ) {
        for (Category category : categories) {
            if( category.name.equals(name) ) {
                return category;
            }
        }
        Category category = new Category();
        category.name = name;
        category.color = colors.peek();
        categories.add(category);
        return category;
    }

    public void removeInfo( Vector3i pos, String category) {
        GridPosition gridPosition = create(pos);
        gridPosition.entries.remove(category);
    }
    public void removeCategory(String category) {
        for (GridPosition gridPosition : grid.values()) {
            gridPosition.entries.remove(category);
        }
        Category category1 = addCategory(category);
        categories.remove(category1);
    }
    public void addInfo( Vector3i pos, String category, String info ) {
        GridPosition gridPosition = create(pos);
        gridPosition.entries.put(category, info);
        addCategory(category);
    }

    public void render() {
        int height = font.getHeight("ABC");
        for (GridPosition gp : grid.values()) {
            GL11.glPushMatrix();

            Vector3f cameraPosition = CoreRegistry.get(WorldRenderer.class).getActiveCamera().getPosition();
            Vector3f worldPos = new Vector3f(gp.position.x, gp.position.y, gp.position.z);
            worldPos.sub(cameraPosition);
            worldPos.y += 1f;
            renderBillboardBegin(-1, worldPos, null, new Vector3f(0.005f, -0.005f, 0.005f));
            int pos = 0;
            int cat = 0;

            for (Category category : categories) {
                String text = gp.entries.get(category.name);
                if( text!=null ) {
                    for (String line : text.split("\n")) {
                        font.drawString(0, pos, line, categories.get(cat).color);
                        pos += height;
                    }
                    cat++;
                }
            }
            renderBillboardEnd();

            GL11.glPopMatrix();
        }
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

}
