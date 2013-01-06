package org.terasology.network;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.EntityRef;

/**
 * @author Immortius
 */
public class ClientComponent implements Component {
    public boolean local = false;

    @Replicate
    public EntityRef clientInfo = EntityRef.NULL;

}
