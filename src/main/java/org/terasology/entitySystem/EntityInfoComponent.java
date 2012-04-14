package org.terasology.entitySystem;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class EntityInfoComponent extends AbstractComponent {
    public String parentPrefab;

    public EntityInfoComponent() {}

    public EntityInfoComponent(String parentPrefab) {
        this.parentPrefab = parentPrefab;
    }
}
