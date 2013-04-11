package org.terasology.componentSystem.rendering;

import com.bulletphysics.linearmath.Transform;
import com.google.common.collect.Sets;
import org.lwjgl.BufferUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.componentSystem.RenderSystem;
import org.terasology.components.utility.DroppedItemTypeComponent;
import org.terasology.components.world.LocationComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.RegisterComponentSystem;
import org.terasology.entitySystem.event.AddComponentEvent;
import org.terasology.entitySystem.event.RemovedComponentEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.manager.ShaderManager;
import org.terasology.math.AABB;
import org.terasology.rendering.logic.MeshComponent;
import org.terasology.rendering.logic.MeshRenderer;
import org.terasology.rendering.shader.ShaderProgram;
import org.terasology.rendering.world.WorldRenderer;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import java.nio.FloatBuffer;
import java.util.Set;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.glMultMatrix;

/*
* @author Small-Jeeper
*/

@RegisterComponentSystem(headedOnly = true)
public class DroppedItemsRenderer  implements RenderSystem, EventHandlerSystem {
    private static final Logger logger = LoggerFactory.getLogger(MeshRenderer.class);
    private WorldRenderer worldRenderer;
    private Set< EntityRef> itemMesh = Sets.newHashSet();

    @Override
    public void initialise() {
        worldRenderer = CoreRegistry.get(WorldRenderer.class);
    }


    @ReceiveEvent(components = {MeshComponent.class})
    public void onNewMesh(AddComponentEvent event, EntityRef entity) {
        DroppedItemTypeComponent item = entity.getComponent(DroppedItemTypeComponent.class);

        if( item != null){
            MeshComponent meshComponent = entity.getComponent(MeshComponent.class);
            itemMesh.add(entity);
        }
    }

    @ReceiveEvent(components = {MeshComponent.class})
    public void onDestroyMesh(RemovedComponentEvent event, EntityRef entity) {
        DroppedItemTypeComponent item = entity.getComponent(DroppedItemTypeComponent.class);

        if( item != null){
            MeshComponent meshComponent = entity.getComponent(MeshComponent.class);
            itemMesh.remove(entity);
        }
    }

    @Override
    public void shutdown() {
    }

    private void render(boolean shadows) {
        Vector3f cameraPosition = worldRenderer.getActiveCamera().getPosition();

        Quat4f worldRot = new Quat4f();
        Vector3f worldPos = new Vector3f();
        AxisAngle4f rot = new AxisAngle4f();
        Matrix4f matrix = new Matrix4f();
        Transform trans = new Transform();
        Transform normTrans = new Transform();

        ShaderProgram shader;

        if (!shadows) {
            shader = ShaderManager.getInstance().getShaderProgram("block");
            shader.setInt("textured", 0);
            shader.setFloat("light", worldRenderer.getRenderingLightValue());
        } else {
            shader = ShaderManager.getInstance().getShaderProgram("shadowMap");
        }

        shader.enable();

        glPushMatrix();

        if (!shadows) {
            glTranslated(-cameraPosition.x, -cameraPosition.y, -cameraPosition.z);
        } else {
            Vector3f lightCamPos = worldRenderer.getLightCamera().getPosition();
            glTranslated(-lightCamPos.x, -lightCamPos.y, -lightCamPos.z);
        }

        float[] openglMat = new float[16];
        FloatBuffer mBuffer = BufferUtils.createFloatBuffer(16);

        for (EntityRef entity : itemMesh) {
            //Basic rendering
            MeshComponent meshComp = entity.getComponent(MeshComponent.class);
            LocationComponent location = entity.getComponent(LocationComponent.class);

            if (location == null || meshComp.mesh == null) {
                continue;
            }

            if (meshComp.mesh.isDisposed()) {
                logger.error("Attempted to render disposed ITEM mesh");
                continue;
            }

            location.getWorldRotation(worldRot);
            location.getWorldPosition(worldPos);
            float worldScale = location.getWorldScale();
            matrix.set(worldRot, worldPos, worldScale);
            trans.set(matrix);
            AABB aabb = meshComp.mesh.getAABB().transform(trans);

            boolean visible;
            if (shadows) {
                visible = worldRenderer.isAABBVisibleLight(aabb);
            } else {
                visible = worldRenderer.isAABBVisible(aabb);
            }

            if (visible) {
                glPushMatrix();
                trans.getOpenGLMatrix(openglMat);
                mBuffer.put(openglMat);
                mBuffer.flip();
                glMultMatrix(mBuffer);

                meshComp.mesh.render();
                glPopMatrix();
            }
        }
        glPopMatrix();
    }

    @Override
    public void renderOpaque() {
        render(false);
    }

    @Override
    public void renderTransparent() {
    }

    @Override
    public void renderOverlay() {
    }

    @Override
    public void renderFirstPerson() {
    }

    @Override
    public void renderShadows() {
        //render(true);
    }
}
