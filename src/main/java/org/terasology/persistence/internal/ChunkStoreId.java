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
package org.terasology.persistence.internal;

import com.google.common.base.Objects;
import org.terasology.math.Vector3i;
import org.terasology.protobuf.EntityData;

/**
 * @author Immortius
 */
public class ChunkStoreId implements StoreId {
    private Vector3i pos;

    public ChunkStoreId(Vector3i pos) {
        this.pos = new Vector3i(pos);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof ChunkStoreId) {
            return Objects.equal(pos, ((ChunkStoreId) obj).pos);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(pos);
    }

    @Override
    public void setUpIdentity(EntityData.EntityStoreMetadata.Builder metadata) {
        metadata.setType(EntityData.StoreType.ChunkStoreType);
        metadata.addStoreIntegerId(pos.x);
        metadata.addStoreIntegerId(pos.y);
        metadata.addStoreIntegerId(pos.z);
    }
}
