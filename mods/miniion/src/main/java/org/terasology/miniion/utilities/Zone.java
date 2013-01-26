package org.terasology.miniion.utilities;

import org.terasology.math.Vector3i;
import org.terasology.miniion.minionenum.ZoneType;

public class Zone {

	private Vector3i minbounds = new Vector3i(Integer.MAX_VALUE,
			Integer.MAX_VALUE, Integer.MAX_VALUE);
	private Vector3i maxbounds = new Vector3i(Integer.MIN_VALUE,
			Integer.MIN_VALUE, Integer.MIN_VALUE);

	private Vector3i startposition;
	private Vector3i endposition;

	public String Name;
	public ZoneType zonetype;
	public int zoneheight;
	public int zonedepth;
	public int zonewidth;

	public Zone() {
	}

	public Zone(Vector3i startposition, Vector3i endposition) {
		this.startposition = startposition;
		this.endposition = endposition;
		calcBounds(startposition);
		calcBounds(endposition);
	}

	private void calcBounds(Vector3i gridPosition) {
		if (gridPosition.x < minbounds.x) {
			minbounds.x = gridPosition.x;
		}
		if (gridPosition.y < minbounds.y) {
			minbounds.y = gridPosition.y;
		}
		if (gridPosition.z < minbounds.z) {
			minbounds.z = gridPosition.z;
		}

		if (gridPosition.x > maxbounds.x) {
			maxbounds.x = gridPosition.x;
		}
		if (gridPosition.y > maxbounds.y) {
			maxbounds.y = gridPosition.y;
		}
		if (gridPosition.z > maxbounds.z) {
			maxbounds.z = gridPosition.z;
		}
	}

	public Vector3i getStartPosition() {
		return startposition;
	}

	public Vector3i getEndPosition() {
		return endposition;
	}

	public Vector3i getMinBounds() {
		return minbounds;
	}

	public Vector3i getMaxBounds() {
		return maxbounds;
	}
}