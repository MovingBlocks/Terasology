package org.terasology.miniion.components;

import org.terasology.entitySystem.Component;
import org.terasology.math.Vector3i;
import org.terasology.rendering.world.BlockGrid;

public class ZoneSelectionComponent implements Component {
	public Vector3i startpos;
	public Vector3i endpos;
	public BlockGrid blockGrid = new BlockGrid();
}
