package org.terasology.entitySystem;

/**
 * A component is a collection of data that can be attached to an entity. The existence of a component and the data
 * it contains is then used by systems to determine the behaviour of the entity.
 *
 * @author Immortius <immortius@gmail.com>
 */
public interface Component extends Cloneable {
    public String getName();

    public Component clone();
}
