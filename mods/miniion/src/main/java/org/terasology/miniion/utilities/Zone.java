/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
		if(endposition != null){
			calcBounds(endposition);
		}
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