package org.terasology.codecity.world.structure;

import java.io.Serializable;

public class CodeClass implements Serializable, CodeContent {
	private int variables;
	private int length;
	
	public CodeClass(int variables, int length) {
		this.variables = variables;
		this.length = length;
	}

	@Override
	public int getSize(CodeScale scale) {
		return Math.max(scale.getScaledSize(variables), 1);
	}

	@Override
	public int getHeight(CodeScale scale) {
		return Math.max(scale.getScaledSize(length), 1);
	}
}
