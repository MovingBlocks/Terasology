package org.terasology.componentSystem.rendering;

import com.google.common.collect.*;
import org.terasology.componentSystem.RenderSystem;
import org.terasology.components.rendering.MeshComponent;
import org.terasology.components.world.LocationComponent;
import org.terasology.entitySystem.*;
import org.terasology.entitySystem.event.AddComponentEvent;
import org.terasology.entitySystem.event.RemovedComponentEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.LocalPlayer;
import org.terasology.logic.manager.ShaderManager;
import org.terasology.math.TeraMath;
import org.terasology.math.AABB;
import org.terasology.rendering.assets.Material;
import org.terasology.rendering.primitives.Mesh;
import org.terasology.rendering.primitives.Tessellator;
import org.terasology.rendering.primitives.TessellatorHelper;
import org.terasology.rendering.shader.ShaderProgram;
import org.terasology.rendering.world.WorldRenderer;

import javax.vecmath.*;

import java.util.Set;

import static org.lwjgl.opengl.GL11.*;

/**
 * TODO: This should be made generic (no explicit shader or mesh) and ported directly into WorldRenderer?
 *
 * @author Immortius <immortius@gmail.com>
 */
@RegisterComponentSystem(headedOnly = true)
public class MeshRenderer implements RenderSystem, EventHandlerSystem {
    private EntityManager manager;
    private Mesh mesh;
    private WorldRenderer worldRenderer;

    private Multimap<Material, EntityRef> opaqueMesh = ArrayListMultimap.create();
    private Multimap<Material, EntityRef> translucentMesh = HashMultimap.create();
    private Set<EntityRef> gelatinous = Sets.newHashSet();

    @Override
    public void initialise() {
        manager = CoreRegistry.get(EntityManager.class);
        worldRenderer = CoreRegistry.get(WorldRenderer.class);

        Tessellator tessellator = new Tessellator();
        TessellatorHelper.addBlockMesh(tessellator, new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), 0.8f, 0.8f, 0.6f, 0f, 0f, 0f);
        TessellatorHelper.addBlockMesh(tessellator, new Vector4f(1.0f, 1.0f, 1.0f, 0.6f), 1.0f, 1.0f, 0.8f, 0f, 0f, 0f);
        mesh = tessellator.generateMesh();
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components = {MeshComponent.class})
    public void onNewMesh(AddComponentEvent event, EntityRef entity) {
        MeshComponent meshComp = entity.getComponent(MeshComponent.class);
        if (meshComp.renderType == MeshComponent.RenderType.GelatinousCube) {
            gelatinous.add(entity);
        } else {
            opaqueMesh.put(meshComp.material, entity);
        }
    }

    @ReceiveEvent(components = {MeshComponent.class})
    public void onDestroyMesh(RemovedComponentEvent event, EntityRef entity) {
        MeshComponent meshComponent = entity.getComponent(MeshComponent.class);
        if (meshComponent.renderType == MeshComponent.RenderType.GelatinousCube) {
            gelatinous.remove(entity);
        } else {
            opaqueMesh.remove(meshComponent.material, entity);
        }
    }

    @Override
    public void renderTransparent() {

        Vector3f cameraPosition = worldRenderer.getActiveCamera().getPosition();
        ShaderProgram shader = ShaderManager.getInstance().getShaderProgram("gelatinousCube");
        shader.enable();

        for (EntityRef entity : gelatinous) {
            MeshComponent meshComp = entity.getComponent(MeshComponent.class);
            LocationComponent location = entity.getComponent(LocationComponent.class);
            if (location == null) continue;

            Quat4f worldRot = location.getWorldRotation();
            Vector3f worldPos = location.getWorldPosition();
            float worldScale = location.getWorldScale();
            AABB aabb = mesh.getAABB().transform(worldRot, worldPos, worldScale);
            if (worldRenderer.isAABBVisible(aabb)) {
                glPushMatrix();

                glTranslated(worldPos.x - cameraPosition.x, worldPos.y - cameraPosition.y, worldPos.z - cameraPosition.z);
                AxisAngle4f rot = new AxisAngle4f();
                rot.set(worldRot);
                glRotatef(TeraMath.RAD_TO_DEG * rot.angle, rot.x, rot.y, rot.z);
                glScalef(worldScale, worldScale, worldScale);

                shader.setFloat4("colorOffset", meshComp.color.x, meshComp.color.y, meshComp.color.z, meshComp.color.w);
                shader.setFloat("light", worldRenderer.getRenderingLightValueAt(worldPos));

                mesh.render();

                glPopMatrix();
            }
        }
    }

    @Override
    public void renderOpaque() {
        boolean carryingTorch = CoreRegistry.get(LocalPlayer.class).isCarryingTorch();
        Vector3f cameraPosition = worldRenderer.getActiveCamera().getPosition();

        for (Material material : opaqueMesh.keys()) {
            material.enable();
            material.setInt("carryingTorch", carryingTorch ? 1 : 0);
            material.bindTextures();

            for (EntityRef entity : opaqueMesh.get(material)) {
                MeshComponent meshComp = entity.getComponent(MeshComponent.class);
                LocationComponent location = entity.getComponent(LocationComponent.class);
                if (location == null) continue;

                Quat4f worldRot = location.getWorldRotation();
                Vector3f worldPos = location.getWorldPosition();
                float worldScale = location.getWorldScale();
                AABB aabb = meshComp.mesh.getAABB().transform(worldRot, worldPos, worldScale);

                if (worldRenderer.isAABBVisible(aabb)) {
                    glPushMatrix();

                    glTranslated(worldPos.x - cameraPosition.x, worldPos.y - cameraPosition.y, worldPos.z - cameraPosition.z);
                    AxisAngle4f rot = new AxisAngle4f();
                    rot.set(worldRot);
                    glRotatef(TeraMath.RAD_TO_DEG * rot.angle, rot.x, rot.y, rot.z);
                    glScalef(worldScale, worldScale, worldScale);

                    material.setFloat("light", worldRenderer.getRenderingLightValueAt(worldPos));
                    meshComp.mesh.render();

                    glPopMatrix();
                }
            }
        }

        /*for (EntityRef entity : manager.iteratorEntities(MeshComponent.class)) {
            // TODO: Probably don't need this collision component, there should be some sort of AABB built into the mesh
            MeshComponent meshComp = entity.getComponent(MeshComponent.class);
            if (meshComp.renderType != MeshComponent.RenderType.Normal || meshComp.mesh == null) continue;

            LocationComponent location = entity.getComponent(LocationComponent.class);
            if (location == null) continue;



            Quat4f worldRot = location.getWorldRotation();
            Vector3f worldPos = location.getWorldPosition();
            float worldScale = location.getWorldScale();
            AABB aabb = meshComp.mesh.getAABB().transform(worldRot, worldPos, worldScale);

            if (worldRenderer.isAABBVisible(aabb)) {
                glPushMatrix();

                glTranslated(worldPos.x - cameraPosition.x, worldPos.y - cameraPosition.y, worldPos.z - cameraPosition.z);
                AxisAngle4f rot = new AxisAngle4f();
                rot.set(location.getWorldRotation());
                glRotatef(TeraMath.RAD_TO_DEG * rot.angle, rot.x, rot.y, rot.z);
                glScalef(worldScale, worldScale, worldScale);

                meshComp.material.enable();
                meshComp.material.setFloat("light", worldRenderer.getRenderingLightValueAt(worldPos));
                meshComp.material.setInt("carryingTorch", carryingTorch ? 1 : 0);
                meshComp.material.bindTextures();
                meshComp.mesh.render();

                glPopMatrix();
            }
        }*/
    }

    @Override
    public void renderOverlay() {
    }

    @Override
    public void renderFirstPerson() {
    }
}