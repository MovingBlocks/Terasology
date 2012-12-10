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
package org.terasology.miniion.events;

import org.terasology.entitySystem.AbstractEvent;
import org.terasology.miniion.utilities.MinionMessage;

/**
 * Created with IntelliJ IDEA.
 * User: Overdhose
 * Date: 24/05/12
 * Time: 4:03
 * Message event : send info to player message queue
 */
public class MinionMessageEvent extends AbstractEvent {

    private MinionMessage minionMessage;

    public MinionMessageEvent() {
        minionMessage = null;
    }

    public MinionMessageEvent(MinionMessage minionmessage) {
        minionMessage = minionmessage;
    }

    public MinionMessage getMinionMessage() {
        return minionMessage;
    }
}
