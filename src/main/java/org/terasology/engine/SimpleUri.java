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
package org.terasology.engine;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import org.terasology.engine.module.UriUtil;

/**
 * A URI to identify standard objects in Terasology - components, events, etc. These URIs are always in the form: <module-name>:<object-name>
 *
 * @author synopia
 */
public class SimpleUri extends AbstractBaseUri {
    private String moduleName = "";
    private String objectName = "";

    private String normalisedModuleName = "";
    private String normalisedObjectName = "";

    public SimpleUri() {
    }

    public SimpleUri(String moduleName, String objectName) {
        Preconditions.checkNotNull(moduleName);
        Preconditions.checkNotNull(objectName);
        this.moduleName = moduleName;
        this.objectName = objectName;
        this.normalisedModuleName = UriUtil.normalise(moduleName);
        this.normalisedObjectName = UriUtil.normalise(objectName);
    }

    public SimpleUri(String simpleUri) {
        String[] split = simpleUri.split(MODULE_SEPARATOR, 2);
        if (split.length > 1) {
            moduleName = split[0];
            normalisedModuleName = UriUtil.normalise(split[0]);
            objectName = split[1];
            normalisedObjectName = UriUtil.normalise(split[1]);
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

    public String getObjectName() {
        return objectName;
    }

    public String getNormalisedObjectName() {
        return normalisedObjectName;
    }

    public boolean isValid() {
        return !normalisedModuleName.isEmpty() && !normalisedObjectName.isEmpty();
    }

    @Override
    public String toString() {
        if (!isValid()) {
            return "";
        }
        return moduleName + MODULE_SEPARATOR + objectName;
    }

    @Override
    public String toNormalisedString() {
        if (!isValid()) {
            return "";
        }
        return normalisedModuleName + MODULE_SEPARATOR + normalisedObjectName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof SimpleUri) {
            SimpleUri other = (SimpleUri) obj;
            return Objects.equal(normalisedModuleName, other.normalisedModuleName) && Objects.equal(normalisedObjectName, other.normalisedObjectName);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(normalisedModuleName, normalisedObjectName);
    }

}
