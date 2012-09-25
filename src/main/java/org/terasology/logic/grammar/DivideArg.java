package org.terasology.logic.grammar;

/**
 * @author Tobias 'skaldarnar' Nett
 */
public class DivideArg {
    private Size size;
    private Shape symbol;

    public DivideArg(Size size, Shape symbol) {
        this.size = size;
        this.symbol = symbol;
    }

    public Size getSize() {
        return size;
    }

    public Shape getShape() {
        return symbol;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        if (size.isAbsolute()) {
            builder.append(size.getAbsoluteValue());
        } else {
            builder.append(size.getValue() + "%");
        }
        builder.append("] \t");
        builder.append(symbol.toString());
        return builder.toString();
    }
}
