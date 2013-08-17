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
package org.terasology.rendering.gui.widgets;

import org.newdawn.slick.Color;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.ClickListener;

import javax.vecmath.Vector2f;

/**
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 *         TODO needs some work.
 */
public class UIMessageBox extends UIDialog {
    private UILabel infoText;
    private UIButton buttonOk;

    private final Vector2f minSize = new Vector2f(384f, 128f);

    public UIMessageBox(String title, String text) {
        super(new Vector2f());
        setTitle(title);
        setModal(true);

        float width = 0f;
        float heigh = 0f;

        buttonOk = new UIButton(new Vector2f(128f, 32f), UIButton.ButtonType.NORMAL);
        buttonOk.getLabel().setText("Ok");
        buttonOk.setVisible(true);

        infoText = new UILabel(text);
        infoText.setVisible(true);
        infoText.setColor(Color.black);

        width = infoText.getSize().x + 15f > minSize.x ? infoText.getSize().x + 15f : minSize.x;
        heigh = infoText.getSize().y + 75f > minSize.y ? infoText.getSize().y + 75f : minSize.y;
        setSize(new Vector2f(width, heigh));
        resetPosition();

        infoText.setPosition(new Vector2f(getSize().x / 2 - infoText.getSize().x / 2, getSize().y / 2 - infoText.getSize().y / 2));
        buttonOk.setPosition(new Vector2f(getSize().x / 2 - buttonOk.getSize().x / 2, getSize().y - buttonOk.getSize().y - 10f));
        buttonOk.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                close();
            }
        });

        addDisplayElement(infoText);
        addDisplayElement(buttonOk);
    }
}
