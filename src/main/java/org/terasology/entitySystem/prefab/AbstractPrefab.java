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
import org.terasology.asset.AbstractAsset;
import org.terasology.asset.AssetUri;

public abstract class AbstractPrefab extends AbstractAsset<PrefabData> implements Prefab {

    public AbstractPrefab(AssetUri uri) {
        super(uri);
    }

    @Override
    public String getName() {
        return getURI().getSimpleString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof Prefab) {
            return Objects.equal(getURI(), ((Prefab) o).getURI());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getURI());
    }

    @Override
    public String toString() {
        return "Prefab(" + getURI() + "){ components: " + this.iterateComponents() + ", parent: " + this.getParent() + " }";
    }
}
