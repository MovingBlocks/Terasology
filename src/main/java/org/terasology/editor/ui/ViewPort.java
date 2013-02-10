/*
* Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.terasology.editor.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;

/**
 * TeraEd main class.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
@SuppressWarnings("serial")
public final class ViewPort extends Canvas {

    private static final Logger logger = LoggerFactory.getLogger(ViewPort.class);

    public ViewPort() {
        setSize(1280, 720);
        setMinimumSize(new Dimension(640, 480));
        setPreferredSize(new Dimension(1280, 720));
        setPreferredSize(new Dimension(1280, 720));
    }
}
