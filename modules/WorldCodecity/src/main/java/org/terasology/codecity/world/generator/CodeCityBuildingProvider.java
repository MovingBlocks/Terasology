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
	    CodePackage facet = new CodePackage();
        CodePackage generator = new CodePackage();
        CodePackage map = new CodePackage();
        CodePackage structure = new CodePackage();
        CodePackage scale = new CodePackage();
        CodePackage terasology = new CodePackage();
        
        CodeClass fac = new CodeClass(1, 18);
        facet.addCodeContent(fac);
        
        CodeClass bProv = new CodeClass(3, 122);
        CodeClass bRast = new CodeClass(1, 54);
        CodeClass gProv = new CodeClass(0, 37);
        CodeClass gRast = new CodeClass(1, 34);
        CodeClass wGen = new CodeClass(0, 24);
        generator.addCodeContent(bProv);
        generator.addCodeContent(bRast);
        generator.addCodeContent(gProv);
        generator.addCodeContent(gRast);
        generator.addCodeContent(wGen);
        terasology.addCodeContent(generator);
 
        CodeClass cMap = new CodeClass(0, 83);
        CodeClass cMapF = new CodeClass(1,101);
        CodeClass cMapH = new CodeClass(3,147);
        CodeClass cMapN = new CodeClass(0,57);
        CodeClass cMapC = new CodeClass(0,36);
        CodeClass cMapCC = new CodeClass(1,34);
        CodeClass cMapCP = new CodeClass(1,43);
        CodeClass cMapO = new CodeClass(4,67);
        map.addCodeContent(cMap);
        map.addCodeContent(cMapF);
        map.addCodeContent(cMapH);
        map.addCodeContent(cMapN);
        map.addCodeContent(cMapC);
        map.addCodeContent(cMapCC);
        map.addCodeContent(cMapCP);
        map.addCodeContent(cMapO);
        terasology.addCodeContent(map);
 
        CodeClass cClas = new CodeClass(2, 45);
        CodeClass cPac = new CodeClass(1,34);
        CodeClass cRep = new CodeClass(0,17);
        structure.addCodeContent(cClas);
        structure.addCodeContent(cPac);
        structure.addCodeContent(cRep);
        terasology.addCodeContent(structure);
 
        CodeClass cSca = new CodeClass(0,28);
        CodeClass cLin = new CodeClass(0,16);
        CodeClass cSqu = new CodeClass(0,21);
        scale.addCodeContent(cSca);
        scale.addCodeContent(cLin);
        scale.addCodeContent(cSqu);
        structure.addCodeContent(scale);
 
        List<DrawableCode> code = new ArrayList<DrawableCode>();
        code.add(terasology.getDrawableCode());
 
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
                processMap(facet, region, obj.getObject().getSubmap(scale, factory), new Vector2i(x+1, y+1), z);
        }
    }
}
