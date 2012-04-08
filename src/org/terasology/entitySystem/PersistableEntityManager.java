package org.terasology.entitySystem;

/**
 * @author Immortius <immortius@gmail.com>
 */
public interface PersistableEntityManager extends EntityManager {

    EntityRef createEntityWithId(int id);
}
