package org.terasology.craft.components.utility;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.terasology.entitySystem.Component;
import java.util.List;
import java.util.Map;

public class CraftRecipeComponent implements Component {

    public static enum CraftRecipeType {
        SELF,
        EXTERNAL
    }

    public CraftRecipeType type = CraftRecipeType.SELF;

    /*
     * If the type is not self, then you should specify the name if the block or item
     */
    public String result = "";

    public boolean fullMatch = true;
    public Map<String, List<String>> recipe = Maps.newHashMap();
    public Map<String, Map<String, String>> refinement = Maps.newHashMap();
    public byte resultCount = 1;
}
