package org.terasology.logic.grammar;

import org.terasology.model.structures.BlockCollection;

/**
 * @author Tobias 'skaldarnar' Nett
 */
public class TerminalShape extends Shape {
    /**
     * The block collection this TerminalShape represents.
     */
    private BlockCollection value;

    public TerminalShape(BlockCollection value) {
        this.value = value;
    }

    public BlockCollection getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TerminalShape)) return false;

        TerminalShape that = (TerminalShape) o;

        if (value != null ? !value.equals(that.value) : that.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }
}
