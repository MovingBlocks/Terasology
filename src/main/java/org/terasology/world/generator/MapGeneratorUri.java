/*
 * Copyright 2013 Moving Blocks
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
package org.terasology.world.generator;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import org.terasology.engine.AbstractBaseUri;

/**
 * A URI to identify a map generator. This URI is always in the form: <package-name>:<generator-name>
 *
 * @author synopia
 */
public class MapGeneratorUri extends AbstractBaseUri {
    private String moduleName = "";
    private String generatorName = "";

    private String normalisedModuleName = "";
    private String normalisedGeneratorName = "";

    public MapGeneratorUri() {
    }

    public MapGeneratorUri(String moduleName, String generatorName) {
        Preconditions.checkNotNull(moduleName);
        Preconditions.checkNotNull(generatorName);
        this.moduleName = moduleName;
        this.generatorName = generatorName;
        this.normalisedModuleName = normalise(moduleName);
        this.normalisedGeneratorName = normalise(generatorName);
    }

    public MapGeneratorUri(String simpleUri) {
        String[] split = simpleUri.split(MODULE_SEPARATOR, 2);
        if (split.length > 1) {
            moduleName = split[0];
            normalisedModuleName = normalise(split[0]);
            generatorName = split[1];
            normalisedGeneratorName = normalise(split[1]);
        }
    }

    @Override
    public String getModuleName() {
        return moduleName;
    }

    @Override
    public String getNormalisedModuleName() {
        return normalisedModuleName;
    }

    public String getGeneratorName() {
        return generatorName;
    }

    public String getNormalisedGeneratorName() {
        return normalisedGeneratorName;
    }

    public boolean isValid() {
        return !normalisedModuleName.isEmpty() && !normalisedGeneratorName.isEmpty();
    }

    @Override
    public String toString() {
        if (!isValid()) {
            return "";
        }
        return moduleName + MODULE_SEPARATOR + generatorName;
    }

    @Override
    public String toNormalisedString() {
        if (!isValid()) {
            return "";
        }
        return normalisedModuleName + MODULE_SEPARATOR + normalisedGeneratorName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof MapGeneratorUri) {
            MapGeneratorUri other = (MapGeneratorUri) obj;
            return Objects.equal(normalisedModuleName, other.normalisedModuleName) && Objects.equal(normalisedGeneratorName, other.normalisedGeneratorName);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(normalisedModuleName, normalisedGeneratorName);
    }

}
