package org.terasology.logic.grammar;

import java.util.List;

/**
 * @author Tobias 'skaldarnar' Nett
 */
public abstract class ComplexRule extends Shape {

    /**
     * Returns the successor elements of this complex shape.
     * <p/>
     * The successor elements of a complex shape are for example the arguments of a _divide_ or _repeat_
     * rule.
     * The successor elements cannot be simply looked up in the grammar rules but rather depend on the
     * specific complex shape rule/command they belong to.
     *
     * @return the successor elements of the complex shape rule
     */
    abstract public List<Shape> getElements();
}
