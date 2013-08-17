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
package org.terasology.persistence.internal;

import org.terasology.protobuf.EntityData;

import java.util.Objects;

/**
 * @author Immortius
 */
public class PlayerStoreId implements StoreId {
    private String id;

    public PlayerStoreId(String playerId) {
        this.id = playerId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof PlayerStoreId) {
            return Objects.equals(((PlayerStoreId) obj).id, id);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public void setUpIdentity(EntityData.EntityStoreMetadata.Builder metadata) {
        metadata.setType(EntityData.StoreType.PlayerStoreType);
        metadata.setStoreStringId(id);
    }
}
