package org.terasology.miniion.components;

import java.util.ArrayList;
import java.util.List;

import org.terasology.entitySystem.Component;
import org.terasology.miniion.utilities.Zone;

/**
 * Zonelist to be used exclusively by minionsystem no prefab should use this
 * component
 * 
 * @author od
 * 
 */
public class ZoneListComponent implements Component {
	public List<Zone> Gatherzones = new ArrayList<Zone>();
}
