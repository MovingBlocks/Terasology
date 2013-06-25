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
package org.terasology.entitySystem.prefab;

import com.google.common.base.Objects;
import org.terasology.asset.AssetUri;

/**
 * @todo javadoc
 */
public abstract class AbstractPrefab implements Prefab {

    private AssetUri uri;

    protected AbstractPrefab(AssetUri uri) {
        this.uri = uri;
    }

    @Override
    public String getName() {
        return uri.getSimpleString();
    }

    @Override
    public AssetUri getURI() {
        return uri;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof Prefab) {
            return Objects.equal(uri, ((Prefab) o).getURI());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(uri);
    }

    @Override
    public String toString() {
        return "Prefab(" + uri + "){ components: " + this.iterateComponents() + ", parent: " + this.getParent() + " }";
    }
}
