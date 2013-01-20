package org.terasology.grammarSystem.logic.grammar.shapes.complex;

/**
 * This class represents a size object for the org.terasology.logic.grammar system.
 * <p/>
 * A size can either be relative or absolute. In the first case, it holds a float value from the interval [0,1], in the latter case the size
 * should be obtained via {@link Size#getAbsoluteValue()} and is an integer value.
 *
 * @author Tobias 'skaldarnar' Nett
 */
public class Size {
    private float value;
    private boolean absolute;

    /**
     * Generate a new Size object which is specified by the parameters. Set the second parameter to false if you want to create a relative
     * value. If the size is relative the must be from the interval [0,1].
     *
     * @param value    the value for the size, must be from [0,1] if size is relative
     * @param absolute
     */
    public Size(float value, boolean absolute) {
        this.absolute = absolute;
        if (!absolute) {
            if (value < 0 || value > 1)
                throw new IllegalArgumentException("The value for relative sizes must be from the interval [0,1] but was " + value);
        }
        this.value = value;
    }

    /**
     * Returns the value as it is, should be used only if the size is relative.
     *
     * @return
     */
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

    /** @return true if the size is absolute, false otherwise */
    public boolean isAbsolute() {
        return absolute;
    }
}
