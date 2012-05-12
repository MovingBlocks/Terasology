package org.terasology.componentSystem.rendering;

import org.terasology.componentSystem.RenderSystem;
import org.terasology.components.AABBCollisionComponent;
import org.terasology.components.LocationComponent;
import org.terasology.components.MeshComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.game.CoreRegistry;
import org.terasology.game.Terasology;
import org.terasology.logic.LocalPlayer;
import org.terasology.logic.manager.ShaderManager;
import org.terasology.math.TeraMath;
import org.terasology.model.structures.AABB;
import org.terasology.rendering.primitives.Mesh;
import org.terasology.rendering.primitives.Tessellator;
import org.terasology.rendering.primitives.TessellatorHelper;
import org.terasology.rendering.shader.ShaderProgram;
import org.terasology.rendering.world.WorldRenderer;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import static org.lwjgl.opengl.GL11.*;

/**
 * TODO: This should be made generic (no explicit shader or mesh) and ported directly into WorldRenderer?
 * @author Immortius <immortius@gmail.com>
 */
public class MeshRenderer implements RenderSystem {
    private EntityManager manager;
    private Mesh mesh;
    private WorldRenderer worldRenderer;

    public void initialise() {
        manager = CoreRegistry.get(EntityManager.class);
        worldRenderer = CoreRegistry.get(WorldRenderer.class);

        Tessellator tessellator = new Tessellator();
        TessellatorHelper.addBlockMesh(tessellator, new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), 0.8f, 0.8f, 0.6f, 0f, 0f, 0f);
        TessellatorHelper.addBlockMesh(tessellator, new Vector4f(1.0f, 1.0f, 1.0f, 0.6f), 1.0f, 1.0f, 0.8f, 0f, 0f, 0f);
        mesh = tessellator.generateMesh();
    }

    public void renderTransparent() {

        Vector3d cameraPosition = worldRenderer.getActiveCamera().getPosition();
        for (EntityRef entity : manager.iteratorEntities(MeshComponent.class, AABBCollisionComponent.class, LocationComponent.class)) {
            // TODO: Probably don't need this collision component, there should be some sort of AABB built into the mesh
            MeshComponent meshComp = entity.getComponent(MeshComponent.class);
            if (meshComp.renderType == MeshComponent.RenderType.Normal) continue;

            AABBCollisionComponent collision = entity.getComponent(AABBCollisionComponent.class);
            LocationComponent location = entity.getComponent(LocationComponent.class);

            Vector3f worldPos = location.getWorldPosition();
            Vector3d extents = new Vector3d(collision.getExtents());
            float worldScale = location.getWorldScale();
            extents.scale(worldScale);
            AABB aabb = new AABB(new Vector3d(worldPos), new Vector3d(collision.getExtents()));

            if (worldRenderer.isAABBVisible(aabb)) {
                glPushMatrix();

                glTranslated(worldPos.x - cameraPosition.x, worldPos.y - cameraPosition.y, worldPos.z - cameraPosition.z);
                AxisAngle4f rot = new AxisAngle4f();
                rot.set(location.getWorldRotation());
                glRotatef(TeraMath.RAD_TO_DEG * rot.angle, rot.x, rot.y, rot.z);
                glScalef(worldScale, worldScale, worldScale);

                ShaderProgram shader = ShaderManager.getInstance().getShaderProgram("gelatinousCube");

                shader.enable();
                shader.setFloat4("colorOffset", meshComp.color.x, meshComp.color.y, meshComp.color.z, meshComp.color.w);
                shader.setFloat("light", worldRenderer.getRenderingLightValueAt(new Vector3d(worldPos)));

                mesh.render();

                glPopMatrix();
            }
        }
    }

    public void renderOpaque() {
        boolean carryingTorch = CoreRegistry.get(LocalPlayer.class).isCarryingTorch();
        Vector3d cameraPosition = worldRenderer.getActiveCamera().getPosition();
        for (EntityRef entity : manager.iteratorEntities(MeshComponent.class, AABBCollisionComponent.class, LocationComponent.class)) {
            // TODO: Probably don't need this collision component, there should be some sort of AABB built into the mesh
            MeshComponent meshComp = entity.getComponent(MeshComponent.class);
            if (meshComp.renderType != MeshComponent.RenderType.Normal || meshComp.mesh == null) continue;

            AABBCollisionComponent collision = entity.getComponent(AABBCollisionComponent.class);
            LocationComponent location = entity.getComponent(LocationComponent.class);

            Vector3f worldPos = location.getWorldPosition();
            Vector3d extents = new Vector3d(collision.getExtents());
            float worldScale = location.getWorldScale();
            extents.scale(worldScale);
            AABB aabb = new AABB(new Vector3d(worldPos), new Vector3d(collision.getExtents()));

            if (worldRenderer.isAABBVisible(aabb)) {
                glPushMatrix();

                glTranslated(worldPos.x - cameraPosition.x, worldPos.y - cameraPosition.y, worldPos.z - cameraPosition.z);
                AxisAngle4f rot = new AxisAngle4f();
                rot.set(location.getWorldRotation());
                glRotatef(TeraMath.RAD_TO_DEG * rot.angle, rot.x, rot.y, rot.z);
                glScalef(worldScale, worldScale, worldScale);

                meshComp.material.enable();
                meshComp.material.setFloat("light", worldRenderer.getRenderingLightValueAt(new Vector3d(worldPos)));
                meshComp.material.setInt("carryingTorch",carryingTorch ? 1 : 0);
                meshComp.material.bindTextures();
                meshComp.mesh.render();

                glPopMatrix();
            }
        }
    }

    public void renderOverlay() {
    }

    public void renderFirstPerson() {
    }
}