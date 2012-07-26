package org.terasology.components;

import org.terasology.entitySystem.Component;
import org.terasology.logic.world.chunks.Chunk;

import org.terasology.entitySystem.EntityRef;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.vecmath.Vector3f;

/**
 * @author Andre Herber <andre.herber@yahoo.de>
 */
public class RadarComponent implements Component {
	
	//TODO optional dynamic Componentfilter for the detection of the Entities (maybe also with deepObject for property values if thats needed???)\\used for mods ???
	//General Filter for all detectionareas
	//TODO filter by example
	public ArrayList<? extends org.terasology.entitySystem.Component> objectFilter; 
	//TODO filter by Type
	public ArrayList<Class<? extends org.terasology.entitySystem.Component>> componentFilter; 
	//TODO Area specific Filter
	Map<Vector3f,Set<Class<? extends org.terasology.entitySystem.Component>>> areaComponentFilter;
	Map<Vector3f,Set<? extends org.terasology.entitySystem.Component>> areaFilterByExample;
	
	public Set<EntityRef> detected;
	//TODO several ordered detectionareas for different Range dependent Entity Interactions.(For example Melee- and Ranged Attacks and things alike).
	//Should be ordered by the extents of the different detectionAreas.
	Map<Vector3f,Set<EntityRef>> areaDetected;
	Vector3f detectionArea;
	
    /**
	 * Generates a RadarComponent with a dectionarea equal to the current viewing distance. 
	 */
    public RadarComponent() {
		int activeViewingDistance = org.terasology.logic.manager.Config.getInstance().getActiveViewingDistance();
        this.detected = new CopyOnWriteArraySet<EntityRef>();
        float x = activeViewingDistance * Chunk.SIZE_X;
        float z = activeViewingDistance * Chunk.SIZE_Z;
        float y = Chunk.SIZE_Y;
        this.detectionArea = new Vector3f(x, y, z);
	}
    
    /**
	 * Generates a RadarComponent with a detectionarea based on the passed dimensions
	 * @param float x = x-Dimension of the detectionarea
	 * @param float y = y-Dimension of the detectionarea
	 * @param float y = z-Dimension of the detectionarea
	 */
    public RadarComponent(float x, float y, float z) {
        this.detected = new CopyOnWriteArraySet<EntityRef>();
        this.detectionArea.set(x, y, z);
	}

    public Set<EntityRef> getDetected(){
    	return this.detected;
    }
    
    public void add(EntityRef entity){
    	detected.add(entity);
    }
    
    public void remove(EntityRef entity){
    	detected.remove(entity);
    }
    
    public Vector3f getDetectionArea(){
    	return this.detectionArea;
    }
    
    public void setDetectionArea(Vector3f detectionArea){
    	this.detectionArea = detectionArea;
    }
}
