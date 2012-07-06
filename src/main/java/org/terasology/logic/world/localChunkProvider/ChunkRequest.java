/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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

package org.terasology.logic.world.localChunkProvider;

import org.terasology.math.Region3i;

/**
 * @author Immortius
 */
public class ChunkRequest implements Comparable<ChunkRequest> {

    public enum RequestType {
        /**
         * If available, check whether the chunks can be further generated
         */
        REVIEW,
        /**
         * Retrieve the chunks from the chunk store or generate them if missing
         */
        PRODUCE,
        /**
         * End the processing of chunks
         */
        EXIT;
    }

    private RequestType type;
    private Region3i region;

    public ChunkRequest(RequestType type, Region3i region) {
        this.type = type;
        this.region = region;
    }

    public RequestType getType() {
        return type;
    }

    public Region3i getRegion() {
        return region;
    }

    @Override
    public int compareTo(ChunkRequest o) {
        return type.compareTo(o.type);
    }
}
