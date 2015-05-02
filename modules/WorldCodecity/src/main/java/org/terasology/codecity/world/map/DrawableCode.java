package org.terasology.codecity.world.map;

import org.terasology.codecity.world.structure.scale.CodeScale;

/**
 * This class represent a part of the code that can be drawed in the map
 */
public interface DrawableCode {
	/** 
	 * @param scale The scale used in the code
	 * @param factory The builder of the map
	 * @return The size of the base of the building.
	 */
	public int getSize(CodeScale scale, CodeMapFactory factory);

	/** 
	 * @param scale The scale used in the code
	 * @param factory The builder of the map
	 * @return The height of the base of the building.
	 */
	public int getHeight(CodeScale scale, CodeMapFactory factory);

	/** 
	 * @param scale The scale used in the code
	 * @param factory The builder of the map
	 * @return The submap inside the code
	 */
	public CodeMap getSubmap(CodeScale scale, CodeMapFactory factory);
}
