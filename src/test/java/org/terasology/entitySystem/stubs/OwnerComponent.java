package org.terasology.entitySystem.stubs;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.Owns;

/**
 * @author Immortius
 */
public class OwnerComponent implements Component {
    @Owns
    public EntityRef child = EntityRef.NULL;
}
