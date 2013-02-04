package org.terasology.miniion.components;

import org.terasology.entitySystem.Component;

public class OreonCropComponent implements Component{
	public long lastgrowthcheck = -1;
	public byte stages;
	public boolean fullgrown = false;
}
