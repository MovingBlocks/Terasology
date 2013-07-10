package org.terasology.entitySystem.stubs;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.EntityRef;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class EntityRefComponent implements Component {

    public EntityRef entityRef = EntityRef.NULL;

    public EntityRefComponent() {

    }

    public EntityRefComponent(EntityRef ref) {
        this.entityRef = ref;
    }
}
