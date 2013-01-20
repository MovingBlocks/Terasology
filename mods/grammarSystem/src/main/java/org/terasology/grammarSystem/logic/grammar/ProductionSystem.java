package org.terasology.grammarSystem.logic.grammar;

import org.terasology.grammarSystem.logic.grammar.shapes.Shape;
import org.terasology.grammarSystem.logic.grammar.shapes.ShapeSymbol;

import java.util.List;
import java.util.Map;

/**
 * @author Tobias 'Skaldarnar' Nett
 * @version 0.3 Date: 26.08.12
 *          <p/>
 *          A ProductionSystem consists of a mapping from ShapeSymbols to a list of successor shapes and an initial axiom. The initial axiom
 *          defines where to start a derivation process.
 */
public class ProductionSystem {
    /** Map with rule mappings from names to shape. */
    private Map<String, List<Shape>> rules;
    /** The initial axiom to start with. */
    private ShapeSymbol initialAxiom;

    public ProductionSystem(Map<String, List<Shape>> rules, ShapeSymbol initialAxiom) {
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

    public Map<String, List<Shape>> getRules() {
        return rules;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (String key : rules.keySet()) {
            List<Shape> l = rules.get(key);
            builder.append(key).append(" ::- ");
            builder.append(l.get(0).toString());
            for (int i = 1; i < l.size(); i++) {
                builder.append(" | ").append(l.get(i).toString());
            }
            builder.append("\n");
        }
        builder.append(";");
        return builder.toString();
    }
}
