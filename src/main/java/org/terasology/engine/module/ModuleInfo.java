/*
 * Copyright 2013 MovingBlocks
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

package org.terasology.engine.module;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * Information on a module
 *
 * @author Immortius
 */
public class ModuleInfo {
    private String id = "";
    private String displayName = "";
    private String description = "";
    private boolean serversideOnly;
    private Set<String> dependencies = Sets.newLinkedHashSet();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<String> getDependencies() {
        if (dependencies == null) {
            dependencies = Sets.newLinkedHashSet();
        }
        return dependencies;
    }

    /**
     * @return Whether this module is only required server-side
     */
    public boolean isServersideOnly() {
        return serversideOnly;
    }

    public void setServersideOnly(boolean serversideOnly) {
        this.serversideOnly = serversideOnly;
    }
}
