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

import org.terasology.engine.modes.GameState;
import org.terasology.game.GameManifest;
import org.terasology.input.events.InputEvent;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

//this class is a singleton
public class EventStorage {

    private static EventStorage instance = null;
    private List<RecordedEvent> events;
    public static boolean isRecording = false;
    public static boolean isReplaying = false;
    public static int recordCount;
    public static GameManifest gameManifest;


    private EventStorage() {
        events = new ArrayList<>();
        this.recordCount = 0;
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

    /*public void saveEvents() {
        boolean check1 = false;
        boolean check2 = false;
        try {
            FileOutputStream f = new FileOutputStream(new File("EventObjects" + this.recordCount +".txt"));
            this.recordCount++;
            ObjectOutputStream o = new ObjectOutputStream(f);
            for (RecordedEvent re : this.events) {
                if (re.getPosition() == 0) {
                    if(check1) {
                        check2 = true;
                    } else {
                        check1 = true;
                    }
                }
                if(re.getPendingEvent().getEvent() instanceof InputEvent) {
                    continue;
                }
                if (check1 && check2) {
                    System.out.println(re.getPendingEvent().getEvent().toString());
                    o.writeObject(re);
                }

            }
            o.close();
            f.close();
            // test
            loadEvents();
            saveEventsString();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    public void loadEvents() {
        try {
            this.events = new ArrayList<>();
            FileInputStream fi = new FileInputStream(new File("EventObjects.txt"));
            ObjectInputStream oi = new ObjectInputStream(fi);
            Object aux;
            while( (aux = oi.readObject()) != null) {
                RecordedEvent re = (RecordedEvent) aux;
                add(re);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void saveEventsString() {
        StringBuffer sb = new StringBuffer();
        for (RecordedEvent re : this.events) {
            saveEventString(re, sb);
        }
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File("Recorded_Events"+this.recordCount+".txt")));
            this.recordCount++;
            writer.write(sb.toString());
            writer.flush();
            writer.close();

        } catch (IOException exception) {
            exception.printStackTrace(); // change this to logger
        }


    }

    private void saveEventString(RecordedEvent re, StringBuffer sb) {
        sb.append("==================================================\n");
        sb.append("Position: " + re.getPosition() + " Timestamp:" + re.getTimestamp() + "\n");
        sb.append("Event: " + re.getPendingEvent().getEvent().toString() + "\n");
        sb.append("Entity: " + re.getPendingEvent().getEntity().toString() + "\n");
        if (re.getPendingEvent().getComponent() != null) {
            sb.append("Component: " + re.getPendingEvent().getComponent().toString() + "\n");
        }

    }


}
