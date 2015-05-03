package org.terasology.codecity.world.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.terasology.codecity.world.facet.CodeCityFacet;
import org.terasology.codecity.world.map.CodeMap;
import org.terasology.codecity.world.map.CodeMapFactory;
import org.terasology.codecity.world.map.DrawableCode;
import org.terasology.codecity.world.map.MapObject;
import org.terasology.codecity.world.structure.CodeClass;
import org.terasology.codecity.world.structure.CodePackage;
import org.terasology.codecity.world.structure.scale.CodeScale;
import org.terasology.codecity.world.structure.scale.SquaredCodeScale;
import org.terasology.math.ChunkMath;
import org.terasology.math.Rect2i;
import org.terasology.math.Vector2i;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.Facet;
import org.terasology.world.generation.FacetProvider;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Produces;
import org.terasology.world.generation.Requires;
import org.terasology.world.generation.Updates;
import org.terasology.world.generation.facets.SurfaceHeightFacet;

/**
 * Creates a new surface for buildings using an squared scale and a CodeMap.
 * This surface is created just above the ground.
 * @author alstrat
 *
 */

@Produces(CodeCityFacet.class)
@Requires(@Facet(SurfaceHeightFacet.class))
public class CodeCityBuildingProvider implements FacetProvider {
	
	private CodeMap codeMap;

    private final CodeScale scale = new SquaredCodeScale();
    private final CodeMapFactory factory = new CodeMapFactory(scale);
	
	public CodeCityBuildingProvider() {
		CodeClass c = new CodeClass(100, 150);
	    CodePackage p = new CodePackage();
	    p.addCodeContent(c);

	    List<DrawableCode> code = new ArrayList<DrawableCode>();
	    code.add(c.getDrawableCode());
	    code.add(p.getDrawableCode());

	    codeMap = factory.generateMap(code);
	}

    @Override
    public void setSeed(long seed) {

    }

    @Override
    public void process(GeneratingRegion region) {
        Border3D border = region.getBorderForFacet(CodeCityFacet.class);
        int base = (int) region.getRegionFacet(SurfaceHeightFacet.class).get(0, 0);
        CodeCityFacet facet = new CodeCityFacet(region.getRegion(),
                border, base);
        Rect2i processRegion = facet.getWorldRegion();
        //Assigns the height to every position in the region
        for (Vector2i position : processRegion) {
        	//Just positions in the CodeMap has a building height associated.
            if(codeMap.isUsed(position.x, position.y)){
            	int height = base+codeMap.getMapObject(position.x, position.y).getHeight(scale, factory);
            	facet.setWorld(position.x, position.y, height);
            }
            
        }
        // give our newly created and populated facet to the region
        region.setRegionFacet(CodeCityFacet.class, facet);
    }

}
