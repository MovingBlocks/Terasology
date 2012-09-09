/*
 * Copyright 2012  Benjamin Glatzel <benjamin.glatzel@me.com>
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

package org.terasology.logic.console;

/**
 * Result from a console command
 * @author Immortius
 * @deprecated
 */
public class ConsoleResult {
    private boolean success = false;
    private String displayString = "";

    public ConsoleResult(boolean success) {
        this.success = success;
    }

    public ConsoleResult(boolean success, String result) {
        this.success = success;
        this.displayString = result;
    }

    /**
     * @return Whether the console command was successful
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * @return The result string to display, if any
     */
    public String getDisplayString() {
        return displayString;
    }
}
