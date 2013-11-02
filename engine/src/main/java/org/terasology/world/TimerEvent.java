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
package org.terasology.world;

/**
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public abstract class TimerEvent {

    private final double executionTime;
    private final boolean repeatingEvent;
    private boolean canFire;

    public TimerEvent(double executionTime, boolean repeatingEvent) {
        this.executionTime = executionTime;
        this.repeatingEvent = repeatingEvent;
    }

    public void execute() {
        run();
    }

    public abstract void run();

    public double getExecutionTime() {
        return executionTime;
    }

    public boolean isRepeatingEvent() {
        return repeatingEvent;
    }

    public void setCanFire(boolean canFire) {
        this.canFire = canFire;
    }

    public boolean canFire() {
        return canFire;
    }
}
