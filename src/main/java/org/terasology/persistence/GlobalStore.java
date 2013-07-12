package org.terasology.persistence;

import org.terasology.entitySystem.EntityRef;

/**
 * @author Immortius
 */
public interface GlobalStore {

    public void store(EntityRef entity);

    public void save();
}
