package org.terasology.world.generator.tree;

import java.util.ArrayList;
import java.util.List;

public class SimpleAxionElementReplacement implements AxionElementReplacement {
    private float probabilitySum = 0;
    private String defaultReplacement;
    private List<Float> probabilities = new ArrayList<Float>();
    private List<String> replacements = new ArrayList<String>();

    public SimpleAxionElementReplacement(String defaultReplacement) {
        this.defaultReplacement = defaultReplacement;
        
        probabilities.add(1f);
        replacements.add(null);
    }

    public void addReplacement(float probability, String replacement) {
        if (probabilitySum+probability>1f)
            throw new IllegalArgumentException("Sum of probabilities exceeds 1");
        probabilitySum+=probability;

        probabilities.add(1-probabilitySum);
        replacements.add(replacement);
    }

    @Override
    public String getReplacement(float random) {
        for (int i=0, size = probabilities.size(); i<size-1; i++) {
            if (probabilities.get(i)>random && probabilities.get(i+1)<=random)
                return replacements.get(i+1);
        }
        return defaultReplacement;
    }
}
