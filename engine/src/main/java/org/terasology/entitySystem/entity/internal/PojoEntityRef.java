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
package org.terasology.entitySystem.entity.internal;

import com.google.common.base.Objects;
import org.terasology.asset.AssetUri;
import org.terasology.entitySystem.entity.LowLevelEntityManager;
import org.terasology.network.NetworkComponent;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class PojoEntityRef extends BaseEntityRef {
    private int id;
    private boolean exists = true;

    PojoEntityRef(LowLevelEntityManager manager, int id) {
        super(manager);
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public boolean exists() {
        return exists;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof PojoEntityRef) {
            PojoEntityRef other = (PojoEntityRef) o;
            return !exists() && !other.exists() || getId() == other.getId();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        AssetUri prefabUri = getPrefabURI();
        StringBuilder builder = new StringBuilder();
        builder.append("EntityRef{id = ");
        builder.append(id);
        NetworkComponent networkComponent = getComponent(NetworkComponent.class);
        if (networkComponent != null) {
            builder.append(", netId = ");
            builder.append(networkComponent.getNetworkId());
        }
        if (prefabUri != null) {
            builder.append(", prefab = '");
            builder.append(prefabUri.toSimpleString());
            builder.append("'");
        }
        builder.append("}");
        return builder.toString();
    }

    @Override
    public void invalidate() {
        super.invalidate();
        exists = false;
    }
}
