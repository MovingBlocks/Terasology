package org.terasology.mods.miniions.componentsystem.entityfactory;

import org.terasology.components.LocationComponent;
import org.terasology.components.MeshComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.utilities.FastRandom;

import javax.vecmath.Vector3f;

/**
 * copied from @author Immortius
 * modified by @author Overdhose
 */
public class MiniionFactory {

    private static final Vector3f[] COLORS = {new Vector3f(1.0f, 1.0f, 0.2f), new Vector3f(1.0f, 0.2f, 0.2f), new Vector3f(0.2f, 1.0f, 0.2f), new Vector3f(1.0f, 1.0f, 0.2f)};

    private FastRandom random;
    private EntityManager entityManager;

    // generates minion cubes for minion toolbar
    public EntityRef generateMiniion(Vector3f position, int index) {
        EntityRef entity = null;
        switch (index) {
            case 0: {
                entity = entityManager.create("miniion:monkeyMinion1");
                break;
            }
            case 1: {
                entity = entityManager.create("miniion:monkeyMinion2");
                break;
            }
            case 2: {
                entity = entityManager.create("miniion:monkeyMinion3");
                break;
            }
            case 3: {
                entity = entityManager.create("miniion:monkeyMinion4");
                break;
            }
            case 4: {
                entity = entityManager.create("miniion:monkeyMinion5");
                break;
            }
            case 5: {
                entity = entityManager.create("miniion:monkeyMinion6");
                break;
            }
            case 6: {
                entity = entityManager.create("miniion:monkeyMinion7");
                break;
            }
            case 7: {
                entity = entityManager.create("miniion:monkeyMinion8");
                break;
            }
            case 8: {
                entity = entityManager.create("miniion:monkeyMinion9");
                break;
            }
            default:
                entityManager.create("miniion:monkeyMinion1");
        }
        if (entity == null) {
            return null;
        }
        LocationComponent loc = entity.getComponent(LocationComponent.class);
        if (loc != null) {
            loc.setWorldPosition(position);
            loc.setLocalScale(((random.randomFloat() + 1.0f) / 2.0f) * 0.8f + 0.2f);
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
