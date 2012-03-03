package org.terasology.logic.systems;

import org.terasology.components.AABBCollisionComponent;
import org.terasology.components.LocationComponent;
import org.terasology.components.MeshComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.game.Terasology;
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
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;

/**
 * TODO: This should be made generic (no explicit shader or mesh) and ported directly into WorldRenderer
 * @author Immortius <immortius@gmail.com>
 */
public class MeshRenderer {
    private EntityManager manager;
    private Mesh mesh;

    public MeshRenderer(EntityManager manager) {
        this.manager = manager;

        Tessellator tessellator = new Tessellator();
        TessellatorHelper.addBlockMesh(tessellator, new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), 0.8f, 0.8f, 0.6f, 0f, 0f, 0f);
        TessellatorHelper.addBlockMesh(tessellator, new Vector4f(1.0f, 1.0f, 1.0f, 0.6f), 1.0f, 1.0f, 0.8f, 0f, 0f, 0f);
        mesh = tessellator.generateMesh();
    }

    public void render() {
        WorldRenderer worldRenderer = Terasology.getInstance().getActiveWorldRenderer();
        if (worldRenderer == null) return;

        Vector3d playerPosition = Terasology.getInstance().getActivePlayer().getPosition();
        for (EntityRef entity : manager.iteratorEntities(MeshComponent.class, AABBCollisionComponent.class, LocationComponent.class)) {
            // TODO: Probably don't need this collision component, there should be some sort of AABB built into the mesh
            AABBCollisionComponent collision = entity.getComponent(AABBCollisionComponent.class);
            LocationComponent location = entity.getComponent(LocationComponent.class);
            MeshComponent meshComp = entity.getComponent(MeshComponent.class);

            Vector3f worldPos = LocationHelper.localToWorldPos(location);
            Vector3d extents = new Vector3d(collision.extents);
            extents.scale(LocationHelper.totalScale(location));
            AABB aabb = new AABB(new Vector3d(worldPos), new Vector3d(collision.extents));

            if (worldRenderer.isAABBVisible(aabb)) {
                glPushMatrix();

                glTranslated(location.position.x - playerPosition.x, location.position.y - playerPosition.y, location.position.z - playerPosition.z);
                AxisAngle4f rot = new AxisAngle4f();
                rot.set(location.rotation);
                glRotatef(TeraMath.RAD_TO_DEG * rot.angle, rot.x, rot.y, rot.z);
                glScalef(location.scale, location.scale, location.scale);

                ShaderProgram shader = ShaderManager.getInstance().getShaderProgram("gelatinousCube");

                shader.enable();
                shader.setFloat4("colorOffset", meshComp.color.x, meshComp.color.y, meshComp.color.z, meshComp.color.w);
                shader.setFloat("light", worldRenderer.getRenderingLightValueAt(new Vector3d(location.position)));

                mesh.render();

                glPopMatrix();
            }
        }
    }

}
