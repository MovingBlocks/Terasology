/*
 * Copyright 2015 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.world.block.loader;

import com.google.api.client.util.Maps;
import com.google.common.collect.Lists;
import org.terasology.assets.AssetData;
import org.terasology.module.sandbox.API;
import org.terasology.world.block.family.BlockFamilyFactory;

import java.util.List;
import java.util.Map;

/**
 */
@API
public class BlockFamilyDefinitionData implements AssetData {
    private boolean template;

    private SectionDefinitionData baseSection = new SectionDefinitionData();
    private Map<String, SectionDefinitionData> sections = Maps.newLinkedHashMap();
    private BlockFamilyFactory familyFactory;

    private List<String> categories = Lists.newArrayList();

    public BlockFamilyDefinitionData() {

    }

    public BlockFamilyDefinitionData(BlockFamilyDefinitionData other) {
        baseSection = new SectionDefinitionData(other.getBaseSection());
        for (Map.Entry<String, SectionDefinitionData> entry : other.getSections().entrySet()) {
            sections.put(entry.getKey(), new SectionDefinitionData(entry.getValue()));
        }
        this.familyFactory = other.familyFactory;
        this.categories = Lists.newArrayList(other.categories);
    }

    public boolean isValid() {
        return familyFactory != null;
    }

    public boolean isTemplate() {
        return template;
    }

    public void setTemplate(boolean template) {
        this.template = template;
    }

    public SectionDefinitionData getBaseSection() {
        return baseSection;
    }

    public Map<String, SectionDefinitionData> getSections() {
        return sections;
    }

    public boolean hasSection(String section) {
        return sections.containsKey(section);
    }

    /**
     * @param section
     * @return Retrieves the data for the given section. If the section is not defined, retrieves the base section instead
     */
    public SectionDefinitionData getSection(String section) {
        SectionDefinitionData result = sections.get(section);
        if (result == null) {
            return baseSection;
        }
        return result;
    }

    public BlockFamilyFactory getFamilyFactory() {
        return familyFactory;
    }

    public void setFamilyFactory(BlockFamilyFactory familyFactory) {
        this.familyFactory = familyFactory;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }
}
