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
package org.terasology.rendering.gui.components;

import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glColor4f;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glVertex2f;

import java.util.ArrayList;

import javax.vecmath.Vector2f;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.newdawn.slick.Color;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.input.events.KeyEvent;
import org.terasology.logic.manager.AudioManager;
import org.terasology.rendering.gui.framework.IInputDataElement;
import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.ClickListener;
import org.terasology.rendering.gui.framework.events.FocusListener;
import org.terasology.rendering.gui.framework.events.InputListener;
import org.terasology.rendering.gui.framework.events.MouseButtonListener;
import org.terasology.rendering.gui.framework.events.MouseMoveListener;

/**
 * A simple graphical input
 *
 * @author Anton Kireev <adeon.k87@gmail.com>
 * @version 0.23
 */
public class UIInput extends UIDisplayContainer implements IInputDataElement {
    //    TODO: Add text selection and paste from clipboard
    private final ArrayList<InputListener> _inputListeners = new ArrayList<InputListener>();

    private final StringBuffer _inputValue = new StringBuffer();
    private final UIText _inputText;
    private final UITextCursor _textCursor;
    private final Vector2f _padding = new Vector2f(10f, 10f);

    private int _cursorPosition = 0;  //The current position of the carriage
    private int _maxLength = 255; //The maximum number of characters that will be accepted as input
    private float _textWidthInContainer = 0;  //The width of the text inside the INPUT field.

    private String _prevInputValue = new String();
    
    private UIInput _inputObj = this;
    
    private class UITextCursor extends UIDisplayElement {

        public UITextCursor(){
            setSize(new Vector2f(2f,15f));
        }

        public void render() {
            glPushMatrix();
            glColor4f(0.0f, 0.0f, 0.0f, 1.0f);
            glBegin(GL_QUADS);
            glVertex2f(getPosition().x, getPosition().y);
            glVertex2f(getPosition().x + 2f, getPosition().y);
            glVertex2f(getPosition().x + 2f, getPosition().y + 15f);
            glVertex2f(getPosition().x, getPosition().y + 15f);
            glEnd();
            glPopMatrix();
        }

        public void update() {

        }

        @Override
        public void layout() {

        }
    }

    public UIInput(Vector2f size) {
        setSize(size);
        setCrop(true);
        setStyle("background-image", "engine:gui_menu 256/512 30/512 0 90/512");
        
        addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                if (!isDisabled()) {
                    setFocus(_inputObj);
                    setStyle("background-position", "0 120/512");
                    
                    if (_inputValue.length() > 0 && _inputText.getTextWidth() > 0) {
                        Vector2f absolutePosition = _inputText.calcAbsolutePosition();
                        float positionRelativeElement = absolutePosition.x + _inputText.getTextWidth() - new Vector2f(Mouse.getX(), Display.getHeight() - Mouse.getY()).x;
                        float averageSymbols = _inputText.getTextWidth() / _inputValue.length();
    
                        int pos = Math.abs((int) (positionRelativeElement / averageSymbols) - _inputValue.length());
    
                        if (pos > _inputValue.length()) {
                            pos = _inputValue.length();
                        } else if (pos < 0) {
                            pos = 0;
                        }
                        
                        _cursorPosition = pos;
                    }
                }
            }
        });
        addMouseButtonListener(new MouseButtonListener() {                            
            @Override
            public void up(UIDisplayElement element, int button, boolean intersect) {

            }
            
            @Override
            public void down(UIDisplayElement element, int button, boolean intersect) {
                if (isFocused() && !intersect)
                    setFocus(null);
            }
            
            @Override
            public void wheel(UIDisplayElement element, int wheel, boolean intersect) {

            }
        });
        addMouseMoveListener(new MouseMoveListener() {        
            @Override
            public void leave(UIDisplayElement element) {
                setStyle("background-position", "0 90/512");
            }
            
            @Override
            public void hover(UIDisplayElement element) {

            }
            
            @Override
            public void enter(UIDisplayElement element) {
                AudioManager.play(new AssetUri(AssetType.SOUND, "engine:PlaceBlock"));
            }

            @Override
            public void move(UIDisplayElement element) {

            }
        });
        addFocusListener(new FocusListener() {
            @Override
            public void focusOn(UIDisplayElement element) {
                if (!isDisabled())
                    _textCursor.setVisible(true);
            }
            
            @Override
            public void focusOff(UIDisplayElement element) {
                _textCursor.setVisible(false);
            }
        });
        
        _inputText = new UIText();
        _inputText.setVisible(true);
        _inputText.setColor(Color.black);
        _inputText.setPosition(new Vector2f(getPosition().x + _padding.x, getPosition().y));

        _textCursor = new UITextCursor();
        _textCursor.setVisible(true);
        _textCursor.setPosition(new Vector2f(getPosition().x + _padding.x, getPosition().y));
        _textCursor.setVisible(false);

        addDisplayElement(_inputText);
        addDisplayElement(_textCursor);
    }

    @Override
    public void update() {
        _inputText.setPosition(new Vector2f(_padding.x, getSize().y/2 - _inputText.getTextHeight()/2));
        _textCursor.setPosition(new Vector2f(_textCursor.getPosition().x, getSize().y/2 - _textCursor.getSize().y/1.5f));

        updateTextShift();
        super.update();
    }

    @Override
    public void processKeyboardInput(KeyEvent event) {
        if (isFocused() && !isDisabled() && event.isDown()) {
            if (event.getKey() == Keyboard.KEY_BACK) {


                _cursorPosition--;

                if (_cursorPosition < 0) {
                    _cursorPosition = 0;
                }

                if (_inputValue.length() > 0) {
                    _prevInputValue = _inputValue.toString();
                    _inputValue.deleteCharAt(_cursorPosition);
                }
            } else if (event.getKey() == Keyboard.KEY_LEFT) {
                _cursorPosition--;
                if (_cursorPosition < 0) {
                    _cursorPosition = 0;
                }
            } else if (event.getKey() == Keyboard.KEY_RIGHT) {
                _cursorPosition++;
                if (_cursorPosition > _inputValue.length()) {
                    _cursorPosition = _inputValue.length();
                }
            } else {
                if (_inputValue.length() > _maxLength) {
                    return;
                }
                char c = Keyboard.getEventCharacter();
                if (c >= 'a' && c < 'z' + 1 || c >= '0' && c < '9' + 1 || c >= 'A' && c < 'Z' + 1 || c == ' ' || c == '_' || c == '.' || c == ',' || c == '!' || c == '-' || c == '(' || c == ')' || c == '"' || c == '\'' || c == ';' || c == '+') {
                    _prevInputValue = _inputValue.toString();
                    _inputValue.insert(_cursorPosition, c);
                    _cursorPosition++;
                }
            }
            _inputText.setText(_inputValue.toString());
            updateTextShift();
        }
    }

    public void keyPressed() {
        for (int i = 0; i < _inputListeners.size(); i++) {
            _inputListeners.get(i).keyPressed(this);
        }
    }

    /*
    * Get current input value
    */
    @Override
    public String getValue() {
        return _inputValue.toString();
    }

    /*
    * Clear _inputValue; set cursor position to "0"; set input text to "";
    */
    @Override
    public void clearData() {
        if (_inputValue.length() > 0) {
            _inputValue.delete(0, _inputValue.length() - 1);
        }
        _cursorPosition = 0;
        _inputText.setText("");
    }

    /*
    * Set current input value
    */
    public void setValue(String value) {
        _inputValue.setLength(0);
        _inputValue.append(value);
        _inputText.setText(value);
        updateTextShift();
    }

    /*
    * Set color text into input field
    */
    public void setTextColor(Color color) {
        _inputText.setColor(color);
    }

    /*
    * Moves the text and the graphic text cursor accourding to the cursor's position.
    *
    */
    private void updateTextShift() {
        float cursorPos = 0f;
        _textWidthInContainer = _inputText.getTextWidth() + _padding.x + _inputText.getPosition().x;
        if (_textWidthInContainer > getPosition().x + getSize().x || getPosition().x + _inputText.getPosition().x < 0) {
            _inputText.setPosition(new Vector2f(_inputText.getPosition().x + (getPosition().x + getSize().x - _textWidthInContainer), _inputText.getPosition().y));
        }
        if (_cursorPosition != _inputValue.length()) {
            cursorPos = (_inputText.getFont().getWidth(_inputValue.toString().substring(0, _cursorPosition)) - _textCursor.getSize().x + _inputText.getPosition().x) / 2;
        } else {
            cursorPos = (_textWidthInContainer - _padding.x) / 2;
        }
        _textCursor.setPosition(new Vector2f(cursorPos, _textCursor.getPosition().y));
    }

    /*
    *Change the maximum number of characters that will be accepted as input
    */
    public void setMaxLength(int max) {
        _maxLength = max;
    }

    /*
    * Get the maximum number of characters that will be accepted as input
    */
    public int getMaxLength() {
        return _maxLength;
    }

    public void setDisabled(boolean isDisabled){
        _disabled = isDisabled;
    }

    public boolean isDisabled(){
        return _disabled;
    }

}

