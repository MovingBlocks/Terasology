package org.terasology.logic.systems;

import org.terasology.components.LocationComponent;
import org.terasology.components.MeshComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.game.Terasology;
import org.terasology.logic.manager.ShaderManager;
import org.terasology.rendering.primitives.Mesh;
import org.terasology.rendering.primitives.Tessellator;
import org.terasology.rendering.primitives.TessellatorHelper;
import org.terasology.rendering.shader.ShaderProgram;
import org.terasology.rendering.world.WorldRenderer;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector4f;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class MeshRenderer {
    private EntityManager manager;
    private WorldRenderer worldRenderer;
    private Mesh mesh;

    public MeshRenderer(WorldRenderer worldRenderer, EntityManager manager) {
        this.worldRenderer = worldRenderer;
        this.manager = manager;

        Tessellator tessellator = new Tessellator();
        TessellatorHelper.addBlockMesh(tessellator, new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), 0.8f, 0.8f, 0.6f, 0f, 0f, 0f);
        TessellatorHelper.addBlockMesh(tessellator, new Vector4f(1.0f, 1.0f, 1.0f, 0.6f), 1.0f, 1.0f, 0.8f, 0f, 0f, 0f);
        mesh = tessellator.generateMesh();
    }

    public void render() {
        Vector3d playerPosition = Terasology.getInstance().getActivePlayer().getPosition();
        for (EntityRef entity : manager.iteratorEntities(MeshComponent.class, LocationComponent.class)) {
            LocationComponent location = entity.getComponent(LocationComponent.class);
            MeshComponent meshComp = entity.getComponent(MeshComponent.class);

            glPushMatrix();

            glTranslated(location.position.x - playerPosition.x, location.position.y - playerPosition.y, location.position.z - playerPosition.z);
            AxisAngle4f rot = new AxisAngle4f();
            rot.set(location.rotation);
            glRotatef(rot.angle, rot.x, rot.y, rot.z);
            glScalef(location.scale, location.scale, location.scale);

            ShaderProgram shader = ShaderManager.getInstance().getShaderProgram("gelatinousCube");

            shader.enable();
            shader.setFloat4("colorOffset", meshComp.color.x, meshComp.color.y, meshComp.color.z, meshComp.color.w);
            shader.setFloat("light", worldRenderer.getRenderingLightValueAt(location.position));

            mesh.render();

            glPopMatrix();
        }
    }
    
}
