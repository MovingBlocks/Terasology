package org.terasology.logic.drowning;

import org.terasology.entitySystem.Component;
import org.terasology.network.FieldReplicateType;
import org.terasology.network.Replicate;

/**
 * @author Immortius
 */
public class DrowningComponent implements Component {
    @Replicate(FieldReplicateType.SERVER_TO_OWNER)
    public long startDrowningTime = 0;

    public long nextDrownDamageTime = 0;
}
