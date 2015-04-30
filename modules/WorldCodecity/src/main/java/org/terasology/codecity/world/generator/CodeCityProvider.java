package org.terasology.codecity.world.generator;

import org.terasology.math.Rect2i;
import org.terasology.math.Vector2i;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.FacetProvider;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Produces;
import org.terasology.world.generation.facets.SurfaceHeightFacet;

@Produces(SurfaceHeightFacet.class)
public class CodeCityProvider implements FacetProvider {

	@Override
	public void setSeed(long seed) {
		
	}

	@Override
	public void process(GeneratingRegion region) {
        // Create our surface height facet (we will get into borders later)
        Border3D border = region.getBorderForFacet(SurfaceHeightFacet.class);
        SurfaceHeightFacet facet = new SurfaceHeightFacet(region.getRegion(), border);

        // loop through every position on our 2d array
        Rect2i processRegion = facet.getWorldRegion();
        for(Vector2i position : processRegion) {
            facet.setWorld(position, 10f);
        }

        // give our newly created and populated facet to the region
        region.setRegionFacet(SurfaceHeightFacet.class, facet);
	}

}
