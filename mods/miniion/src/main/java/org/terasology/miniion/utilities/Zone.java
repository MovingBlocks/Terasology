package org.terasology.miniion.utilities;

import org.terasology.math.Vector3i;
import org.terasology.miniion.minionenum.ZoneType;

public class Zone {
	
	public class ZonePosition {
        public ZonePosition(Vector3i startposition, Vector3i endposition) {
            this.startposition = startposition;
            this.endposition = endposition;
        }

        public Vector3i startposition;
        public Vector3i endposition;
    }
	
	public String Name;
	public ZoneType zonetype;
	public ZonePosition zoneposition;
	public int zoneheight;
	public int zonedepth;
	
	public Zone(){		
		
	}
	
	public Zone(Vector3i startposition, Vector3i endposition){
		zoneposition = new ZonePosition(startposition, endposition);
	}
	
/*	public void setZonePosition(Vector3i startposition, Vector3i endposition){
		zoneposition = new ZonePosition(startposition, endposition);
	}
	
	public ZonePosition getZonePosition(){
		return zoneposition;
	}*/
}