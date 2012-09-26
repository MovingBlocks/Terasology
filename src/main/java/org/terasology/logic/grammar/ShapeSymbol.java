package org.terasology.logic.grammar;

import org.terasology.math.TeraMath;

import java.util.List;

/**
 * A shape symbol describes a grammar element that can be derived directly via a grammar rule.
 * <p/>
 * It has a (unique) label - typically the symbol you use in the grammar. There is no further information
 * a ShapeSymbol holds except for the geometric information of a Shape itself.
 *
 * @author Tobias 'skaldarnar' Nett
 */
public class ShapeSymbol extends Shape {

    /**
     * The string representation of this label.
     */
    private String label = "";

    /**
     * A new ShapeSymbol is created with the specified label.
     * The default probability of 1.0 is used for this symbol.
     *
     * @param label the symbol's label - not null
     */
    public ShapeSymbol(String label) {
        // JAVA7 : Objects.requireNonNull(label);
        if (label == null) {
            throw new IllegalArgumentException("ShapeSymbol cannot be null.");
        }
        this.label = label;
    }

    public ShapeSymbol(String label, float probability) {
        this(label);
        this.probability = TeraMath.clamp(probability, 0f, 1f);
    }

    @Override
    public String toString() {
        return label;
    }

    public List<Shape> getElements() {
        return null;
    }
}

