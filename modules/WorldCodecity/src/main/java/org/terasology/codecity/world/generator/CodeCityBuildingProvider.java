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
import org.terasology.codecity.world.structure.scale.SquareRootCodeScale;
import org.terasology.math.ChunkMath;
import org.terasology.math.Rect2i;
import org.terasology.math.Region3i;
import org.terasology.math.Vector2i;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.Facet;
import org.terasology.world.generation.FacetProvider;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Produces;
import org.terasology.world.generation.Requires;
import org.terasology.world.generation.Updates;
import org.terasology.world.generation.facets.SurfaceHeightFacet;

import com.sun.jna.platform.unix.X11.Drawable;

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

    private final CodeScale scale = new SquareRootCodeScale();
    private final CodeMapFactory factory = new CodeMapFactory(scale);
	
	public CodeCityBuildingProvider() {
        CodeClass c = new CodeClass(10, 1500);
        CodeClass c2 = new CodeClass(50, 150);
        CodePackage p3 = new CodePackage();
        CodePackage p1 = new CodePackage();
        CodePackage p2 = new CodePackage();
        p1.addCodeContent(c2);
        p1.addCodeContent(c2);
        p1.addCodeContent(c2);
        p1.addCodeContent(c2);
        p2.addCodeContent(c);
        p3.addCodeContent(p1);
        p3.addCodeContent(p2);

        List<DrawableCode> code = new ArrayList<DrawableCode>();
        code.add(p1.getDrawableCode());
        code.add(p2.getDrawableCode());
        code.add(p2.getDrawableCode());
        code.add(p2.getDrawableCode());
        code.add(p2.getDrawableCode());
        code.add(p3.getDrawableCode());

        codeMap = factory.generateMap(code);
	}

    @Override
    public void setSeed(long seed) {

    }

    @Override
    public void process(GeneratingRegion region) {
        Border3D border = region.getBorderForFacet(CodeCityFacet.class);
        int base = (int) region.getRegionFacet(SurfaceHeightFacet.class).get(0, 0);
        CodeCityFacet facet = new CodeCityFacet(region.getRegion(), border, base);
        Rect2i processRegion = facet.getWorldRegion();

        processMap(facet, processRegion, codeMap, Vector2i.zero(), base);
        // give our newly created and populated facet to the region
        region.setRegionFacet(CodeCityFacet.class, facet);
    }

    /**
     * Update the height of the indicated position given a CodeMap.
     * @param facet Surface where the height should be updated.
     * @param region Region where the facet must be updated
     * @param map Map with the height information.
     * @param offset Offset of the current coordinates
     * @param level Current height
     */
    private void processMap(CodeCityFacet facet, Rect2i region, CodeMap map, Vector2i offset, int level) {
        for (MapObject obj : map.getMapObjects()) {
            int x = obj.getPositionX() + offset.getX();
            int y = obj.getPositionZ() + offset.getY();
            int z = obj.getHeight(scale, factory) + level;

            if (region.contains(x, y) && facet.getWorld(x, y) < z)
                facet.setWorld(x, y, z);
            
            if (obj.isOrigin())
                processMap(facet, region, obj.getObject().getSubmap(scale, factory), new Vector2i(x+1, y+1), z+1);
        }
    }
}
