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
package org.terasology.logic.title;

public interface Title {

    /**
     * Shows a title with subtitle in the middle screen
     * and gives you a option to make it disappear after some time
     * of showing it.
     *
     * @param title The title that will show in the middle of the screen
     * @param subtitle The subtitle that will show under the title with small size
     * @param stay How much do you want to that title to stay
     */
    void show(String title, String subtitle, long stay);

    /**
     * Hide the title from the screen
     */
    void hide();
}
