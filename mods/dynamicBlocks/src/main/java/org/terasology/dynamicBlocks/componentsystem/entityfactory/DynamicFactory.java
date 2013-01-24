package org.terasology.dynamicBlocks.componentsystem.entityfactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.components.world.LocationComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.dynamicBlocks.components.DynamicBlockComponent;

import javax.vecmath.Vector3f;

/**
 * Created with IntelliJ IDEA.
 * User: Pencilcheck
 * Date: 12/22/12
 * Time: 6:14 AM
 * To change this template use File | Settings | File Templates.
 */
public class DynamicFactory {

    private static final Logger logger = LoggerFactory.getLogger(DynamicFactory.class);

    private EntityManager entityManager;

    public EntityRef generateDynamicBlock(Vector3f position, DynamicBlockComponent.DynamicType type) {
        EntityRef entity = null;
        switch (type) {
            case Train: {
                entity = entityManager.create("dynamicBlocks:train");
                break;
            }
            case Boat: {
                entity = entityManager.create("dynamicBlocks:boat");
                break;
            }
            default:
                entity = entityManager.create("dynamicBlocks:train");
        }
        if (entity == null)
            return null;

        LocationComponent loc = entity.getComponent(LocationComponent.class);
        if (loc != null) {
            loc.setWorldPosition(position);
            entity.saveComponent(loc);
        }

        return entity;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
}
