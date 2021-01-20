// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.logic.delay;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.terasology.TerasologyTestingEnvironment;
import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DelayedActionSystemTest extends TerasologyTestingEnvironment {

    long nextFakeEntityId = 1;
    int lookingForId; // Use this for ordering the time events. 0 is earliest.

    private DelayedActionSystem delayedActionSystem;
    private List<Integer> vals; // Use this for ordering the expected values.

    private Time time;

    @BeforeEach
    public void setup() throws Exception {

        delayedActionSystem = new DelayedActionSystem();
        nextFakeEntityId = 1;
        lookingForId = 0;

        time = mockTime;
        delayedActionSystem.setTime(mockTime);
    }

    private EntityRef createFakeEntityWith(ArbritaryDelayActionComponent arbritaryDelayActionComp) {
        EntityRef entRef = mock(EntityRef.class);
        when(entRef.getComponent(ArbritaryDelayActionComponent.class)).thenReturn(arbritaryDelayActionComp);
        when(entRef.exists()).thenReturn(true);
        when(entRef.getId()).thenReturn(nextFakeEntityId++);
        return entRef;
    }

    @Test
    public void test1DelayedAction() {
        ArbritaryDelayActionComponent a1 = new ArbritaryDelayActionComponent();
        a1.value = 3;

        vals = new ArrayList<Integer>(Arrays.asList(3));

        delayedActionSystem.addDelayedAction(createFakeEntityWith(a1),
                "Blah Blah", ((time.getGameTimeInMs() + 1000) - time.getGameTimeInMs()));
        nextFakeEntityId++;
    }

    @Test
    public void test2DelayedActionsInChronoOrder() {
        ArbritaryDelayActionComponent a1 = new ArbritaryDelayActionComponent();
        ArbritaryDelayActionComponent a2 = new ArbritaryDelayActionComponent();

        a1.value = 3;
        a2.value = 2;

        vals = new ArrayList<Integer>(Arrays.asList(3, 2));

        delayedActionSystem.addDelayedAction(createFakeEntityWith(a1),
                "First", (time.getGameTimeInMs() + 1000) - time.getGameTimeInMs());
        delayedActionSystem.addDelayedAction(createFakeEntityWith(a2),
                "Second", (time.getGameTimeInMs() + 1500) - time.getGameTimeInMs());
    }

    @Test
    public void test2DelayedActionsInReverseChronoOrder() {
        ArbritaryDelayActionComponent a1 = new ArbritaryDelayActionComponent();
        ArbritaryDelayActionComponent a2 = new ArbritaryDelayActionComponent();

        a1.value = 5;
        a2.value = 8;

        vals = new ArrayList<Integer>(Arrays.asList(5, 8));

        delayedActionSystem.addDelayedAction(createFakeEntityWith(a2),
                "Second", (time.getGameTimeInMs() + 1500) - time.getGameTimeInMs());
        delayedActionSystem.addDelayedAction(createFakeEntityWith(a1),
                "First", (time.getGameTimeInMs() + 1000) - time.getGameTimeInMs());
    }

    @Test
    public void testMultipleDelayedActionsInChronoOrder() {
        ArbritaryDelayActionComponent a1 = new ArbritaryDelayActionComponent();
        ArbritaryDelayActionComponent a2 = new ArbritaryDelayActionComponent();
        ArbritaryDelayActionComponent a3 = new ArbritaryDelayActionComponent();
        ArbritaryDelayActionComponent a4 = new ArbritaryDelayActionComponent();
        ArbritaryDelayActionComponent a5 = new ArbritaryDelayActionComponent();

        a1.value = 13;
        a2.value = 22;
        a3.value = 10;
        a4.value = 50;
        a5.value = 1;

        vals = new ArrayList<Integer>(Arrays.asList(13, 22, 10, 50, 1));

        delayedActionSystem.addDelayedAction(createFakeEntityWith(a1),
                "First", (time.getGameTimeInMs() + 1000) - time.getGameTimeInMs());
        delayedActionSystem.addDelayedAction(createFakeEntityWith(a2),
                "Second", (time.getGameTimeInMs() + 1500) - time.getGameTimeInMs());
        delayedActionSystem.addDelayedAction(createFakeEntityWith(a3),
                "Third", (time.getGameTimeInMs() + 2000) - time.getGameTimeInMs());
        delayedActionSystem.addDelayedAction(createFakeEntityWith(a4),
                "Fourth", (time.getGameTimeInMs() + 2500) - time.getGameTimeInMs());
        delayedActionSystem.addDelayedAction(createFakeEntityWith(a5),
                "Fifth", (time.getGameTimeInMs() + 3000) - time.getGameTimeInMs());
    }

    @Test
    public void testMultipleDelayedActionsInRandomOrder() {
        ArbritaryDelayActionComponent a3 = new ArbritaryDelayActionComponent();
        ArbritaryDelayActionComponent a5 = new ArbritaryDelayActionComponent();
        ArbritaryDelayActionComponent a2 = new ArbritaryDelayActionComponent();
        ArbritaryDelayActionComponent a1 = new ArbritaryDelayActionComponent();
        ArbritaryDelayActionComponent a4 = new ArbritaryDelayActionComponent();

        a1.value = 100;
        a2.value = 200;
        a3.value = 314;
        a4.value = 12;
        a5.value = 51;

        vals = new ArrayList<Integer>(Arrays.asList(100, 200, 314, 12, 51));

        delayedActionSystem.addDelayedAction(createFakeEntityWith(a3),
                "Third", (time.getGameTimeInMs() + 2000) - time.getGameTimeInMs());
        delayedActionSystem.addDelayedAction(createFakeEntityWith(a5),
                "Fifth", (time.getGameTimeInMs() + 3000) - time.getGameTimeInMs());
        delayedActionSystem.addDelayedAction(createFakeEntityWith(a2),
                "Second", (time.getGameTimeInMs() + 1500) - time.getGameTimeInMs());
        delayedActionSystem.addDelayedAction(createFakeEntityWith(a1),
                "First", (time.getGameTimeInMs() + 1000) - time.getGameTimeInMs());
        delayedActionSystem.addDelayedAction(createFakeEntityWith(a4),
                "Fourth", (time.getGameTimeInMs() + 2500) - time.getGameTimeInMs());
    }

    @ReceiveEvent
    public void finishWaiting(DelayedActionTriggeredEvent event, EntityRef entity, ArbritaryDelayActionComponent arbritaryDelayActionComp) {
        assertEquals(vals.get(lookingForId).intValue(), arbritaryDelayActionComp.value);
        lookingForId++;
    }
}
