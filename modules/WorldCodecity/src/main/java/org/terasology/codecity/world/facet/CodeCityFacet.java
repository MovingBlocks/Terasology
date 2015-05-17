package org.terasology.codecity.world.facet;

import org.terasology.math.Region3i;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.facets.base.BaseFieldFacet2D;

public class CodeCityFacet extends BaseFieldFacet2D {
	private int base = 0;

    public CodeCityFacet(Region3i targetRegion, Border3D border, int base){
        super(targetRegion, border);
        this.base = base;
    }
    
    public int getBaseHeight(){
    	return this.base;
    }
}