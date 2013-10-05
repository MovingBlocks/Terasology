package org.terasology.math;

public class LSystemRule {
    private final String axiom;
    private final float probability;

    public LSystemRule(String axiom, float probability) {
        this.axiom = axiom;
        this.probability = probability;
    }

    public String getAxiom() {
        return axiom;
    }

    public float getProbability() {
        return probability;
    }
}
