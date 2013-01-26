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
package org.terasology.miniion.components;

import org.terasology.entitySystem.Component;

/**
 * Created with IntelliJ IDEA. User: Overdhose Date: 24/05/12 Time: 2:37
 * clicking only 2 points will select all blocks in the rectangle formed above
 * the height of start point clicking a 3d and /or 4th point will limit the
 * height/ set depth of the selection, depending if the block clicked was higher
 * / lower then the startpoint if only a depth point was selected, the selection
 * will only be downwards only rectangular selection with no feedback to user
 * selected blocks will get grouped by height, and minions will gather top to
 * bottom startpoint should be a miniion (waypoint) flag block
 */
public class MinionCommandComponent implements Component {

}
