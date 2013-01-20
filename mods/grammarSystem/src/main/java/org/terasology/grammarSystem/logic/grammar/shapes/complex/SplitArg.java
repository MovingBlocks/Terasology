package org.terasology.grammarSystem.logic.grammar.shapes.complex;

import org.terasology.grammarSystem.logic.grammar.shapes.Shape;

/** @author Tobias 'skaldarnar' Nett */
public class SplitArg {
    public enum SplitType {
        WALLS, INNER;
    }

    private SplitType type;
    private Shape symbol;

    public SplitArg(SplitType type, Shape symbol) {
        this.type = type;
        this.symbol = symbol;
    }

    public Shape getShape() {
        return symbol;
    }

    public SplitType getType() {
        return type;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        builder.append(type.name());
        builder.append("] \t");
        builder.append(symbol.toString());
        return builder.toString();
    }
}
