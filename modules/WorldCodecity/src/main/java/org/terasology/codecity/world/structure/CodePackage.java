package org.terasology.codecity.world.structure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CodePackage implements CodeContent, Serializable {
	private List<CodeContent> contentList;
	
	/**
	 * Create a new Package representation
	 */
	public CodePackage() {
		contentList = new ArrayList<CodeContent>();
	}
	
	@Override
	public int getSize(CodeScale scale) {
		int size = 1;
		for (CodeContent code : contentList)
			size += 2 + code.getSize(scale);

		return size;
	}

	@Override
	public int getHeight(CodeScale scale) {
		return 1;
	}
	
	public void addCodeContent(CodeContent content) {
		contentList.add(content);
	}
}
