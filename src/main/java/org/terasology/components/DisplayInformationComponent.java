package org.terasology.components;

import org.terasology.entitySystem.Component;
import org.terasology.network.Replicate;

/**
 * @author Immortius
 */
public class DisplayInformationComponent implements Component {
    @Replicate
    public String name;
    @Replicate
    public String description;


    public String toString() {
        return String.format("DisplayInformation(name = '%s', description = '%s')", name, description);
    }
}
