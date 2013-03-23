package org.terasology.components.utility;

import com.google.common.collect.Maps;
import org.terasology.entitySystem.Component;

import java.util.List;
import java.util.Map;

public class CraftRecipeComponent implements Component {
    public boolean fullMatch = true;
    public Map<String, List<String>> recipe = Maps.newHashMap();
    public Map<String, String> refinement = Maps.newHashMap();
    public byte resultCount = 1;
}
