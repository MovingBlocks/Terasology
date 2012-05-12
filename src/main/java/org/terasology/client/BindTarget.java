package org.terasology.client;

public class BindTarget {
	private String modName;
	private String description;
	public BindTarget(String modName, String description) {
		this.modName = modName;
		this.description = description;
	}
	public void start() {}
	public void repeat() {}
	public void end() {}
	public String getDescription() {
		return description;
	}
	public String getModName() {
		return modName;
	}
}
