package org.terasology.rendering.gui.components;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.terasology.logic.manager.AudioManager;
import org.newdawn.slick.Color;
import org.terasology.rendering.gui.framework.IInputListener;
import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.framework.UIGraphicsElement;
import org.terasology.game.Terasology;
import javax.vecmath.Vector2f;
import java.util.ArrayList;

/**
 * A simple graphical input
 *
 * @author Anton Kireev <adeon.k87@gmail.com>
 * @version 0.1
 * @todo Add text selection and paste from clipboard
 */
public class UIInput extends UIDisplayContainer {
  private final ArrayList<IInputListener> _inputListeners = new ArrayList<IInputListener>();

  private final StringBuffer      _inputValue = new StringBuffer();
  private final UIGraphicsElement _defaultTexture;
  private final UIText            _inputText;
  private final UITextCursor      _textCursor;
  private final Vector2f          _padding    = new Vector2f(10f,10f);
  
  private int    _cursorPosition       = 0;  //The current position of the carriage
  private int    _maxLength            = 42; //The maximum number of characters that will be accepted as input
  private float  _textWidthInContainer = 0;  //The width of the text inside the INPUT field.

  private String _prevInputValue = new String();

  public UIInput(Vector2f size) {
      setSize(size);
      setCrop(true);

      _defaultTexture = new UIGraphicsElement("gui_menu");
      _defaultTexture.setVisible(true);
      _defaultTexture.getTextureSize().set(new Vector2f(256f / 512f, 30f / 512f));

      _inputText = new UIText();
      _inputText.setVisible(true);
      _inputText.setColor(Color.black);
      _inputText.setPosition(new Vector2f((getPosition().x + _padding.x), (getPosition().y + _padding.y)));

      _textCursor = new UITextCursor();
      _textCursor.setVisible(true);
      _textCursor.setPosition(new Vector2f((getPosition().x + _padding.x), (getPosition().y + _padding.y / 2)));

      addDisplayElement(_defaultTexture);
      addDisplayElement(_inputText);
      addDisplayElement(_textCursor);

  }
  
  public void update() {
    _defaultTexture.setSize(getSize());

    Vector2f mousePos = new Vector2f(Mouse.getX(), Display.getHeight() - Mouse.getY());

    if (intersects(mousePos)) {

        if (!_clickSoundPlayed) {
            AudioManager.getInstance().getAudio("PlaceBlock").playAsSoundEffect(1.0f, 0.5f, false);
            _clickSoundPlayed = true;
        }

        if (_mouseUp) {
            _mouseUp = false;
        }

        if(_mouseDown){
            clicked(mousePos);
        }

    } else {
        if(_mouseDown){
           _focused = false;
        }
        _clickSoundPlayed = false;
        _mouseUp = false;
        _mouseDown = false;
        _defaultTexture.getTextureOrigin().set(0f, 90f / 512f);
    }

    if (isFocused()) {
        if(!_textCursor.isVisible()){
            _textCursor.setVisible(true);
        }
        _defaultTexture.getTextureOrigin().set(0f, 120f / 512f);
    } else {
        if(_textCursor.isVisible()){
            _textCursor.setVisible(false);
        }
        _defaultTexture.getTextureOrigin().set(0f, 90f / 512f);
    }
    updateTextShift();
  }

  /*
   * @todo Change the current position of the carriage, when user pushed mouse button
   */
  public void clicked(Vector2f mousePos) {
      _focused = true;
  }

  public void processKeyboardInput(int key){
    if(isFocused()){
        if (key == Keyboard.KEY_BACK) {

            _cursorPosition--;

            if(_cursorPosition<0){
                _cursorPosition = 0;
            }

            if(_inputValue.length()>0){
                _prevInputValue = _inputValue.toString();
                _inputValue.deleteCharAt((_cursorPosition));
            }
        }else if(key == Keyboard.KEY_LEFT){
            _cursorPosition--;
            if(_cursorPosition<0){
                _cursorPosition = 0;
            }
        }else if(key == Keyboard.KEY_RIGHT){
            _cursorPosition++;
            if(_cursorPosition>_inputValue.length()-1){
                _cursorPosition = _inputValue.length()-1;
            }
        }else{
            if(_inputValue.length()>_maxLength){
                return;
            }
            char c = Keyboard.getEventCharacter();
            if (c >= 'a' && c < 'z' + 1 || c >= '0' && c < '9' + 1 || c >= 'A' && c < 'Z' + 1 || c == ' ' || c == '_' || c == '.' || c == ',' || c == '!' || c == '-' || c == '(' || c == ')' || c == '"' || c == '\'' || c == ';' || c == '+') {
                _prevInputValue = _inputValue.toString();
                _inputValue.insert(_cursorPosition,c);
                _cursorPosition++;
            }
        }
        _inputText.setText(_inputValue.toString());
        updateTextShift();
    }
  }
  
  public void keyPressed(){
    for (int i = 0; i < _inputListeners.size(); i++) {
      _inputListeners.get(i).keyPressed(this);
    }
  }

  /*
   * Get current input value
   */
  public String getValue(){
      return _inputValue.toString();
  }

  /*
   * Set current input value
   */
  public void setValue(String value){
      _inputValue.setLength(0);
      _inputValue.append(value);
      _inputText.setText(value);
      updateTextShift();
  }

  /*
   * Set color text into input field
   */
  public void setTextColor(Color color){
    _inputText.setColor(color);
  }

  /*
   * Moves the text and the graphic text cursor accourding to the cursor's position.
   *
   */
  private void updateTextShift(){
      float cursorPos = 0f;
      _textWidthInContainer = _inputText.getTextWidth() + _padding.x + _inputText.getPosition().x;
      if(_textWidthInContainer > getSize().x || _inputText.getPosition().x < 0){
          _inputText.setPosition(new Vector2f((_inputText.getPosition().x + (getSize().x - _textWidthInContainer)),_inputText.getPosition().y));
      }
      if(_cursorPosition!=_inputValue.length()){
          cursorPos = (_inputText.getFont().getWidth(_inputValue.toString().substring(0, _cursorPosition)) - _textCursor.getSize().x + _inputText.getPosition().x)/2;
      }else{
          cursorPos = (_textWidthInContainer - _padding.x)/2;
      }
      _textCursor.setPosition(new Vector2f(cursorPos, _textCursor.getPosition().y));
  }

  public boolean isFocused(){
      return _focused;
  }

  /*
   *Change the maximum number of characters that will be accepted as input
   */
  public void setMaxLength(int max){
      _maxLength = max;
  }

  /*
   * Get the maximum number of characters that will be accepted as input
   */
  public int getMaxLength(){
      return _maxLength;
  }

}

