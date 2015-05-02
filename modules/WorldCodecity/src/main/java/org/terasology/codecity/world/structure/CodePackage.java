package org.terasology.codecity.world.structure;

import java.util.ArrayList;
import java.util.List;

import org.terasology.codecity.world.map.DrawableCode;
import org.terasology.codecity.world.map.DrawableCodePackage;

public class CodePackage implements CodeRepresentation {
	protected List<CodeRepresentation> contentList;

	/**
	 * Create a new Package representation
	 */
	public CodePackage() {
		contentList = new ArrayList<CodeRepresentation>();
	}
	
	/**
	 * Add an object to the package
	 * @param content Object to be added
	 */
	public void addCodeContent(CodeRepresentation content) {
		contentList.add(content);
	}

	@Override
	public DrawableCode getDrawableCode() {
		return new DrawableCodePackage(contentList);
	}
}
