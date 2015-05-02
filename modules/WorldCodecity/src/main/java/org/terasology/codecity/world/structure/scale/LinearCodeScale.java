package org.terasology.codecity.world.structure.scale;

public class LinearCodeScale implements CodeScale {

	@Override
	public int getScaledSize(int size) {
		return size;
	}

	@Override
	public int getScaledSize(int size, int min) {
		return Math.max(getScaledSize(size), min);
	}

}
