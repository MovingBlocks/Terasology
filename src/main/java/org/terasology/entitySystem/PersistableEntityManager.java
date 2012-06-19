package org.terasology.entitySystem;

import gnu.trove.list.TIntList;

/**
 * @author Immortius <immortius@gmail.com>
 */
public interface PersistableEntityManager extends EntityManager {

    EntityRef createEntityRefWithId(int id);

    int getNextId();

    void setNextId(int id);

    TIntList getFreedIds();
}
