package org.terasology.entityFactory;

import javax.vecmath.Vector3f;

import org.terasology.components.LocationComponent;
import org.terasology.components.MeshComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.utilities.FastRandom;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class GelatinousCubeFactory {

    private static final Vector3f[] COLORS = {new Vector3f(1.0f, 1.0f, 0.2f), new Vector3f(1.0f, 0.2f, 0.2f), new Vector3f(0.2f, 1.0f, 0.2f), new Vector3f(1.0f, 1.0f, 0.2f)};

    private FastRandom random;
    private EntityManager entityManager;

    public EntityRef generateGelatinousCube(Vector3f position) {
        EntityRef entity = entityManager.create("core:gelatinousCube");
        LocationComponent loc = entity.getComponent(LocationComponent.class);
        if (loc != null) {
            loc.setWorldPosition(position);
            loc.setLocalScale((random.randomFloat() + 1.0f) * 0.4f + 0.2f);
            entity.saveComponent(loc);
        }

        MeshComponent mesh = entity.getComponent(MeshComponent.class);
        if (mesh != null) {
            int colorId = Math.abs(random.randomInt()) % COLORS.length;
            mesh.color.set(COLORS[colorId].x, COLORS[colorId].y, COLORS[colorId].z, 1.0f);
            entity.saveComponent(mesh);
        }

        return entity;
    }

    // generates minion cubes for minion toolbar
    public EntityRef generateGelatinousMinion(Vector3f position) {
        EntityRef entity = entityManager.create("core:gelatinousMinion");
        LocationComponent loc = entity.getComponent(LocationComponent.class);
        if (loc != null) {
            loc.setWorldPosition(position);
            loc.setLocalScale((random.randomFloat() + 1.0f) * 0.4f + 0.2f);
            entity.saveComponent(loc);
        }

        MeshComponent mesh = entity.getComponent(MeshComponent.class);
        if (mesh != null) {
            int colorId = Math.abs(random.randomInt()) % COLORS.length;
            mesh.color.set(COLORS[colorId].x, COLORS[colorId].y, COLORS[colorId].z, 1.0f);
            entity.saveComponent(mesh);
        }

        return entity;
    }

    public FastRandom getRandom() {
        return random;
    }

    public void setRandom(FastRandom random) {
        this.random = random;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
}
