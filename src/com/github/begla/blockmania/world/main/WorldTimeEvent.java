/*
 * Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
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
package com.github.begla.blockmania.world.main;

/**
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public abstract class WorldTimeEvent {

    private double _executionTime;
    private boolean _repeatingEvent, _canFire;

    public WorldTimeEvent(double executionTime, boolean repeatingEvent) {
        _executionTime = executionTime;
        _repeatingEvent = repeatingEvent;
    }

    public void execute() {
        run();
    }

    public abstract void run();

    public double getExecutionTime() {
        return _executionTime;
    }

    public boolean isRepeatingEvent() {
        return _repeatingEvent;
    }

    public void setCanFire(boolean canFire) {
        _canFire = canFire;
    }

    public boolean canFire() {
        return _canFire;
    }
}
