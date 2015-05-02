package org.terasology.codecity.world.structure.scale;

/**
 * This class implement a scale using a Square root
 */
public class SquaredCodeScale implements CodeScale {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getScaledSize(int size) {
		return (int)Math.sqrt(size);
	}

	@Override
	public int getScaledSize(int size, int min) {
		return Math.max(getScaledSize(size), min);
	}
}
