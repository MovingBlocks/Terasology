package org.terasology.functional.componentsystem.entityfactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.components.world.LocationComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.Prefab;
import org.terasology.rendering.logic.MeshComponent;
import org.terasology.utilities.FastRandom;

import javax.vecmath.Color4f;
import javax.vecmath.Vector3f;

/**
 * Created with IntelliJ IDEA.
 * User: Pencilcheck
 * Date: 12/22/12
 * Time: 6:14 AM
 * To change this template use File | Settings | File Templates.
 */
public class LocomotiveFactory {

    private static final Vector3f[] COLORS = {new Vector3f(1.0f, 1.0f, 0.2f), new Vector3f(1.0f, 0.2f, 0.2f), new Vector3f(0.2f, 1.0f, 0.2f), new Vector3f(1.0f, 1.0f, 0.2f)};

    private static final Logger logger = LoggerFactory.getLogger(LocomotiveFactory.class);

    private FastRandom random;
    private EntityManager entityManager;

    public EntityRef generateLocomotiveCube(Vector3f position, int index) {
        EntityRef entity = null;
        switch (index) {
            case 0: {
                entity = entityManager.create("functional:train");
                break;
            }
            case 1: {
                entity = entityManager.create("functional:boat");
                break;
            }
            default:
                entity = entityManager.create("functional:train");
        }
        if (entity == null) {
            return null;
        }
        LocationComponent loc = entity.getComponent(LocationComponent.class);
        if (loc != null) {
            loc.setWorldPosition(position);
            entity.saveComponent(loc);
        }

        return entity;
    }

    public void setRandom(FastRandom random) {
        this.random = random;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
}
