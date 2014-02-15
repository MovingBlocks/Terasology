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
package org.terasology.logic.drowning;

import org.terasology.entitySystem.Component;
import org.terasology.network.FieldReplicateType;
import org.terasology.network.Replicate;

/**
 * @author Immortius
 */
public class DrowningComponent implements Component {

    @Replicate(FieldReplicateType.SERVER_TO_OWNER)
    public boolean isBreathing;
    @Replicate(FieldReplicateType.SERVER_TO_OWNER)
    public long endTime;
    @Replicate(FieldReplicateType.SERVER_TO_OWNER)
    public long startTime;
    @Replicate(FieldReplicateType.SERVER_TO_OWNER)
    public long nextDrownDamageTime;

    public float getRemainingBreath(long gameTime) {
        long capacity = (endTime - startTime);
        float percentage = (gameTime - startTime) / (float) capacity;
        if (!isBreathing) {
            percentage = 1.0f - percentage;
        }
        return Math.min(Math.max(percentage, 0f), 1f);
    }

}
