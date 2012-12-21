package org.terasology.components;

import org.terasology.entitySystem.Component;

/**
 * @author Immortius
 */
public class DisplayInformationComponent implements Component {
    public String name;
    public String description;


    public String toString() {
        return String.format("DisplayInformation(name = '%s', description = '%s')", name, description);
    }
}
