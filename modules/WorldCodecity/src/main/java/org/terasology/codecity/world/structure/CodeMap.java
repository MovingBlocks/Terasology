package org.terasology.codecity.world.structure;

public class CodeMap {
	private CodeScale scale;
	
	public CodeMap() {
		scale = new SquaredCodeScale();
	}
	public void calculate(CodeContent content) {
		int size = content.getSize(scale);
	}
	
	public void build(CodeContent content) {
		int base = content.getSize(scale);
		int height = content.getHeight(scale);
	}
}
