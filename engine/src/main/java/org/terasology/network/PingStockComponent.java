/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.network;

import org.terasology.entitySystem.Component;

/**
 * PingStockComponent stock the ping information of one user.
 * <p>
 * Might be used to stock ping information and display it in future.
 */
public class PingStockComponent implements Component {

    // For now this component just stock one value
    public long pingValue;

    public PingStockComponent(long pingValue) {
        this.pingValue = pingValue;
    }
}
