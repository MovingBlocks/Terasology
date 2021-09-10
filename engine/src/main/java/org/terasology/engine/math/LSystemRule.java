// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.math;

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
