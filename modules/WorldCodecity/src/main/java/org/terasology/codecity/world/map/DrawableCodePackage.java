package org.terasology.codecity.world.map;

import java.util.ArrayList;
import java.util.List;

import org.terasology.codecity.world.structure.CodeRepresentation;
import org.terasology.codecity.world.structure.scale.CodeScale;

/**
 * This class represent a Package that can be drawed in the map
 */
public class DrawableCodePackage implements DrawableCode {
	List<DrawableCode> contentList;

	/**
	 * Create a new DrawableCodePackage 
	 * @param baseContent Content of the package
	 */
	public DrawableCodePackage(List<CodeRepresentation> baseContent) {
		contentList = new ArrayList<DrawableCode>();
		for (CodeRepresentation content : baseContent)
			contentList.add(content.getDrawableCode());
	}

	@Override
	public int getSize(CodeScale scale, CodeMapFactory factory) {
		CodeMap map = factory.generateMap(contentList);
		return 2 + map.getSize();
	}

	@Override
	public int getHeight(CodeScale scale, CodeMapFactory factory) {
		return 1;
	}
	
	@Override
	public CodeMap getSubmap(CodeScale scale, CodeMapFactory factory) {
		return factory.generateMap(contentList);
	}
}
