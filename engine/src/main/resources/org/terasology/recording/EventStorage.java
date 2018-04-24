/*
 * Copyright 2018 MovingBlocks
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
package org.terasology.recording;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//this class is a singleton
public class EventStorage {

    private static EventStorage instance = null;
    private List<RecordedEvent> events;

    private EventStorage() {
        events = new ArrayList<>();
    }

    public static EventStorage getInstance() {
        if (instance == null) {
            instance = new EventStorage();
        }
        return instance;
    }

    public boolean add(RecordedEvent event) {
        return events.add(event);
    }

    //Testing purposes
    public List<RecordedEvent> getEvents() {
        return events;
    }

    public void saveEvents() {
        StringBuffer sb = new StringBuffer();
        for (RecordedEvent re : this.events) {
            saveEvent(re, sb);
        }
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File("Recorded_Events.txt")));
            writer.write(sb.toString());
            writer.flush();
            writer.close();

        } catch (IOException exception) {
            exception.printStackTrace(); // change this to logger
        }


    }

    private void saveEvent(RecordedEvent re, StringBuffer sb) {
        sb.append("==================================================\n");
        sb.append("Position: " + re.getPosition() + " Timestamp:" + re.getTimestamp() + "\n");
        sb.append("Event: " + re.getPendingEvent().getEvent().toString() + "\n");
        sb.append("Entity: " + re.getPendingEvent().getEntity().toString() + "\n");
        if (re.getPendingEvent().getComponent() != null) {
            sb.append("Component: " + re.getPendingEvent().getComponent().toString() + "\n");
        }

    }


}
