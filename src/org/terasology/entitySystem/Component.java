package org.terasology.entitySystem;

import org.terasology.persistence.interfaces.Persistable;

/**
 * A component is a collection of data that can be attached to an entity. The existence of a component and the data
 * it contains is then used by systems to determine the behaviour of the entity.
 *
 * @author Immortius <immortius@gmail.com>
 */
public interface Component extends Persistable {
    public String getName();
}
