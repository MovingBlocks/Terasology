package org.terasology.codecity.world.structure;

/**
 * Esta clase representa una porcion de codigo como un conjunto de bloques
 */
public interface CodeContent {
	/**
	 * Obtiene el tamaño de la base del edificio
	 * @param scale Escala a utilizar
	 * @return
	 */
	public int getSize(CodeScale scale);
	
	/**
	 * Obtiene la altura del edificio
	 * @param scale Escala a utilizar
	 * @return
	 */
	public int getHeight(CodeScale scale);
}
