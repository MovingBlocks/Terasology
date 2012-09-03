package org.terasology.logic.grammar;

/**
 * @author Tobias 'skaldarnar' Nett
 */
public class Size {
    private float value;
    private boolean absolute;

    public Size(float value, boolean absolute) {
        this.value = value;
        this.absolute = absolute;
    }

    public float getValue() {
        return value;
    }

    /**
     * Returns the absolute size as int.
     * <p/>
     * Note that the value is _always_ rounded down.
     *
     * @return the absolute value
     */
    public int getAbsoluteValue() {
        return (int) value;
    }

    /**
     * @return true if the size is absolute, false otherwise
     */
    public boolean isAbsolute() {
        return absolute;
    }
}
