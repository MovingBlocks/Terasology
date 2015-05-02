package org.terasology.codecity.world.structure;

import org.terasology.codecity.world.map.DrawableCode;
import org.terasology.codecity.world.map.DrawableCodeClass;

/**
 * This class represent a Class of a project, saving the variables and length
 */
public class CodeClass implements CodeRepresentation {
    private int variables;
    private int length;

    /**
     * Create a neew CodeClass Object.
     * 
     * @param variables
     *            Number of variables in the class.
     * @param length
     *            Number of lines in the class.
     */
    public CodeClass(int variables, int length) {
        this.variables = variables;
        this.length = length;
    }

    /**
     * @return Number of variables in the code
     */
    public int getVariableNumber() {
        return variables;
    }

    /**
     * @return Number of lines in the code
     */
    public int getClassLength() {
        return length;
    }

    @Override
    public DrawableCode getDrawableCode() {
        return new DrawableCodeClass(this);
    }
}
