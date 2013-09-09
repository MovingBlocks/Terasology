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

package org.terasology.questing.gui;

import org.lwjgl.input.Keyboard;
import org.newdawn.slick.Color;
import org.terasology.asset.Assets;
import org.terasology.rendering.gui.widgets.UIImage;
import org.terasology.rendering.gui.widgets.UILabel;
import org.terasology.rendering.gui.widgets.UIWindow;

import javax.vecmath.Vector2f;

public class UIScreenQuest extends UIWindow {

    private final UIImage background;
    public static UILabel qName;
    public static UILabel qGoal;
    public static String questName = "";
    public static String questGoal = "";

    public UIScreenQuest() {
        setId("journal");
        setBackgroundColor(new Color(0, 0, 0, 200));
        setModal(true);
        maximize();
        setCloseBinds(new String[] {"engine:useHeldItem"});
        setCloseKeys(new int[]{Keyboard.KEY_ESCAPE});

        background = new UIImage(Assets.getTexture("engine:openbook"));
        background.setHorizontalAlign(EHorizontalAlign.CENTER);
        background.setVerticalAlign(EVerticalAlign.CENTER);
        background.setSize(new Vector2f(500, 300));
        background.setVisible(true);
        addDisplayElement(background);

        qName = new UILabel(questName);
        qName.setHorizontalAlign(EHorizontalAlign.CENTER);
        qName.setPosition(new Vector2f(-100f, 20f));
        qName.setVisible(true);
        background.addDisplayElement(qName);

        qGoal = new UILabel(questGoal);
        qGoal.setHorizontalAlign(EHorizontalAlign.CENTER);
        qGoal.setPosition(new Vector2f(-100f, 40f));
        qGoal.setVisible(true);
        background.addDisplayElement(qGoal);

    }

}
