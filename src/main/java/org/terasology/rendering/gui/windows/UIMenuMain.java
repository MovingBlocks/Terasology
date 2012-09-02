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
package org.terasology.rendering.gui.windows;

import javax.vecmath.Vector2f;

import org.terasology.asset.AssetManager;
import org.terasology.game.CoreRegistry;
import org.terasology.game.GameEngine;
import org.terasology.logic.manager.GUIManager;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.ClickListener;
import org.terasology.rendering.gui.widgets.UIButton;
import org.terasology.rendering.gui.widgets.UIImage;
import org.terasology.rendering.gui.widgets.UIText;
import org.terasology.rendering.gui.widgets.UITextWrapNew;
import org.terasology.rendering.gui.widgets.UIWindow;

/**
 * Main menu screen.
 *
 * @author Anton Kireev <adeon.k87@gmail.com>
 */
public class UIMenuMain extends UIWindow {

    private final UIImage _title;

    private final UIButton _exitButton;
    private final UIButton _singlePlayerButton;
    private final UIButton _configButton;

    final UIText _version;

    public UIMenuMain() {
        setId("main");
        setBackgroundImage("engine:menubackground");
        setModal(true);
        maximize();
        
        _title = new UIImage(AssetManager.loadTexture("engine:terasology"));
        _title.setSize(new Vector2f(512f, 128f));
        _title.setHorizontalAlign(EHorizontalAlign.CENTER);
        _title.setPosition(new Vector2f(0f, 128f));
        _title.setVisible(true);

        _version = new UIText("Pre Alpha");
        _version.setHorizontalAlign(EHorizontalAlign.CENTER);
        _version.setPosition(new Vector2f(0f, 230f));
        _version.setVisible(true);

        _exitButton = new UIButton(new Vector2f(256f, 32f), UIButton.eButtonType.NORMAL);
        _exitButton.getLabel().setText("Exit Terasology");
        _exitButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                CoreRegistry.get(GameEngine.class).shutdown();
            }
        });
        _exitButton.setHorizontalAlign(EHorizontalAlign.CENTER);
        _exitButton.setPosition(new Vector2f(0f, 300f + 4 * 40f));
        _exitButton.setVisible(true);
        
        _configButton = new UIButton(new Vector2f(256f, 32f), UIButton.eButtonType.NORMAL);
        _configButton.getLabel().setText("Settings");
        _configButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                GUIManager.getInstance().openWindow("config");
            }
        });
        _configButton.setHorizontalAlign(EHorizontalAlign.CENTER);
        _configButton.setPosition(new Vector2f(0f, 300f + 2 * 40f));
        _configButton.setVisible(true);

        _singlePlayerButton = new UIButton(new Vector2f(256f, 32f), UIButton.eButtonType.NORMAL);
        _singlePlayerButton.getLabel().setText("Single player");
        _singlePlayerButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                GUIManager.getInstance().openWindow("selectWorld");
            }
        });
        _singlePlayerButton.setHorizontalAlign(EHorizontalAlign.CENTER);
        _singlePlayerButton.setPosition(new Vector2f(0f, 300f + 40f));
        _singlePlayerButton.setVisible(true);

        addDisplayElement(_title);
        addDisplayElement(_version);
        addDisplayElement(_configButton);
        addDisplayElement(_exitButton);
        addDisplayElement(_singlePlayerButton);
        
        final UITextWrapNew text = new UITextWrapNew(new Vector2f(400, 430));
        text.setText("Weit hinten, hinter den Wortbergen, fern der Länder \n\nVokalien und Konsonantien leben die Blindtexte. Abgeschieden wohnen Sie in Buchstabhausen an der Küste des Semantik, eines großen Sprachozeans. Ein kleines Bächlein namens Duden fließt durch ihren Ort und versorgt sie mit den nötigen Regelialien. Es ist ein paradiesmatisches Land, in dem einem gebratene Satzteile in den Mund fliegen. Nicht einmal von der allmächtigen Interpunktion werden die Blindtexte beherrscht ein geradezu unorthographisches Leben. Eines Tages aber beschloß eine kleine Zeile Blindtext, ihr Name war Lorem Ipsum, hinaus zu gehen in die weite Grammatik. Der große Oxmox riet ihr davon ab, da es dort wimmele von bösen Kommata, wilden Fragezeichen und hinterhältigen Semikoli, doch das Blindtextchen ließ sich nicht beirren. Es packte seine sieben Versalien, schob sich sein Initial in den Gürtel und machte sich auf den Weg. Als es die ersten Hügel des Kursivgebirges erklommen hatte, warf es einen letzten Blick zurück auf die Skyline seiner Heimatstadt Buchstabhausen, die Headline von Alphabetdorf und die Subline seiner eigenen Straße, der Zeilengasse. Wehmütig lief ihm eine rhetorische Frage über die Wange, dann setzte es seinen Weg fort. Unterwegs traf es eine Copy. Die Copy warnte das Blindtextchen, da, wo sie herkäme wäre sie test test test test test test test");
        text.setPosition(new Vector2f(20, 20));
        text.setVisible(true);
        
        UIButton b = new UIButton(new Vector2f(32f, 32f), UIButton.eButtonType.NORMAL);
        b.getLabel().setText("!");
        b.setPosition(new Vector2f(450f, 20f));
        b.setVisible(true);
        b.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                if (button == 0)
                    text.appendText(" test test test test test test test test test test test");
                else
                    text.setText(text.getText().substring(0, text.getText().length() - 5));
            }
        });
        
        addDisplayElement(text);
        addDisplayElement(b);
    }
}
