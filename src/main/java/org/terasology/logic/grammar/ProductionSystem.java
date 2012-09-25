package org.terasology.logic.grammar;

import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: tobias
 * Date: 26.08.12
 * Time: 23:43
 * To change this template use File | Settings | File Templates.
 */
public class ProductionSystem {
    // A map with rules. Keys have to be non terminal symbols.
    private Map<ShapeSymbol, List<Shape>> rules;
    // initial axiom to start with
    private ShapeSymbol initialAxiom;

    public ProductionSystem(Map<ShapeSymbol, List<Shape>> rules, ShapeSymbol initialAxiom) {
        // JAVA7 : Object.requireNonNull(…);
        if (rules == null || initialAxiom == null) {
            throw new IllegalArgumentException("no null params allowed.");
        }
        this.rules = rules;
        this.initialAxiom = initialAxiom;
    }

    public ShapeSymbol getInitialAxiom() {
        return initialAxiom;
    }

    public Map<ShapeSymbol, List<Shape>> getRules() {
        return rules;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (ShapeSymbol key : rules.keySet()) {
            List<Shape> l = rules.get(key);
            builder.append(key.toString() + " ::- ");
            builder.append(l.get(0).toString());
            for (int i = 1; i < l.size(); i++) {
                builder.append(" | " + l.get(i).toString());
            }
            builder.append("\n");
        }
        builder.append(";");
        return builder.toString();
    }
}
