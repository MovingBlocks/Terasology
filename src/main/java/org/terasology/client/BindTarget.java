package org.terasology.client;

public abstract class BindTarget {
	public void start() {}
	public void repeat() {}
	public void end() {}
	public abstract String getDescription();
	public abstract String getCategory(); 
}
