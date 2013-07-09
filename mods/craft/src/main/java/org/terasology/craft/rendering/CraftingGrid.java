package org.terasology.craft.rendering;

import org.terasology.craft.components.actions.CraftingActionComponent;
import org.terasology.game.CoreRegistry;
import org.terasology.input.CameraTargetSystem;
import org.terasology.math.AABB;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.renderer.AABBRenderer;
import org.terasology.rendering.renderer.BlockOverlayRenderer;
import org.terasology.rendering.world.WorldRenderer;

import javax.vecmath.Vector3f;
import java.util.ArrayList;

public class CraftingGrid implements BlockOverlayRenderer {

    public static final Vector3f CUBE_SIZE = new Vector3f(0.33333f, 0.33333f, 0.333333f);

    private int selectedLevel;
    private ArrayList<AABBRenderer> cellsRender = new ArrayList<AABBRenderer>();

    private AABB aabb;
    private final int countOfCells = 3;
    private int selectedItem = 0;

    public void setAABB(AABB aabb) {
        int level = CoreRegistry.get(CameraTargetSystem.class).getTarget().getComponent(CraftingActionComponent.class).getCurrentLevel();
        if ((aabb != null && !aabb.equals(this.aabb)) || selectedLevel != level) {
            dispose();
            this.aabb = aabb;
            selectedLevel = level;
            reCalculateCells();
        }
    }

    public void setAABB(AABB aabb, int level) {
        if ((aabb != null && !aabb.equals(this.aabb)) || selectedLevel != level) {
            dispose();
            this.aabb = aabb;
            selectedLevel = level;
            reCalculateCells();
        }
    }

    public void dispose() {
        for (AABBRenderer cell : cellsRender) {
            cell.dispose();
        }
        cellsRender.clear();
    }

    public void render(float lineThickness) {
        Camera camera = CoreRegistry.get(WorldRenderer.class).getActiveCamera();
        boolean catched = false;
        for (AABBRenderer cell : cellsRender) {
            if (!catched && cell.getAABB().intersectRectangle(camera.getPosition(), camera.getViewingDirection())) {
                cell.render(lineThickness * 2.5f);
                catched = true;
            } else {
                cell.render(lineThickness);
            }
        }
    }

    public int getSelectedBlock() {
        Camera camera = CoreRegistry.get(WorldRenderer.class).getActiveCamera();

        for (int i = 0; i < cellsRender.size(); i++) {
            if (cellsRender.get(i).getAABB().intersectRectangle(camera.getPosition(), camera.getViewingDirection())) {
                return i;
            }
        }

        return 0;
    }

    private void reCalculateCells() {
        AABB tAabb = null;

        Vector3f min = aabb.getMin();

        for (int x = 0; x < countOfCells; x++) {
            for (int z = 0; z < countOfCells; z++) {
                Vector3f newMin = new Vector3f(min);
                newMin.x += x * CUBE_SIZE.x;
                newMin.y += selectedLevel * CUBE_SIZE.y;
                newMin.z += z * CUBE_SIZE.z;

                Vector3f newMax = new Vector3f(newMin);
                newMax.add(CUBE_SIZE);

                tAabb = AABB.createMinMax(newMin, newMax);

                AABBRenderer aabbRenderCell = new AABBRenderer(tAabb);
                cellsRender.add(aabbRenderCell);
            }
        }
    }

}
