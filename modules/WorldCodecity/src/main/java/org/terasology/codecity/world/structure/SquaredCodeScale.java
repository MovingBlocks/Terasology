package org.terasology.codecity.world.structure;

public class SquaredCodeScale implements CodeScale {

	@Override
	public int getScaledSize(int size) {
		return (int)Math.sqrt(size);
	}
}
