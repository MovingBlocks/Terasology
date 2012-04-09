package org.terasology.rendering.gui.components;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.newdawn.slick.Color;
import org.terasology.rendering.gui.framework.IInputDataElement;
import org.terasology.logic.manager.AudioManager;
import org.terasology.rendering.gui.framework.IInputListener;
import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.framework.UIGraphicsElement;

import javax.vecmath.Vector2f;
import java.util.ArrayList;

/**
 * A simple graphical input
 *
 * @author Anton Kireev <adeon.k87@gmail.com>
 * @version 0.23
 */
public class UIInput extends UIDisplayContainer implements IInputDataElement {
//	TODO: Add text selection and paste from clipboard
  private final ArrayList<IInputListener> _inputListeners = new ArrayList<IInputListener>();

  private final StringBuffer      _inputValue = new StringBuffer();
  private final UIText            _inputText;
  private final UITextCursor      _textCursor;
  private final Vector2f          _padding    = new Vector2f(10f,10f);
  
  private int    _cursorPosition       = 0;  //The current position of the carriage
  private int    _maxLength            = 255; //The maximum number of characters that will be accepted as input
  private float  _textWidthInContainer = 0;  //The width of the text inside the INPUT field.

  private String _prevInputValue = new String();

  public UIInput(Vector2f size) {
      setSize(size);
      setCrop(true);
      setStyle("background-image","gui_menu 256/512 30/512 0 90/512");

      _inputText = new UIText();
      _inputText.setVisible(true);
      _inputText.setColor(Color.black);
      _inputText.setPosition(new Vector2f((getPosition().x + _padding.x), (getPosition().y + _padding.y)));

      _textCursor = new UITextCursor();
      _textCursor.setVisible(true);
      _textCursor.setPosition(new Vector2f((getPosition().x + _padding.x), (getPosition().y + _padding.y / 2)));

      addDisplayElement(_inputText);
      addDisplayElement(_textCursor);
  }
  
  public void update() {
    Vector2f mousePos = new Vector2f(Mouse.getX(), Display.getHeight() - Mouse.getY());

    if (intersects(mousePos)) {

        if (!_clickSoundPlayed) {
            AudioManager.play("PlaceBlock");
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
        setStyle("background-position","0 90/512");
    }

    if (isFocused()) {
        if(!_textCursor.isVisible()){
            _textCursor.setVisible(true);
        }
        setStyle("background-position","0 120/512");
    } else {
        if(_textCursor.isVisible()){
            _textCursor.setVisible(false);
        }
        setStyle("background-position","0 90/512");
    }
    updateTextShift();
    super.update();
  }

  public void clicked(Vector2f mousePos) {
      _focused = true;

      if(_inputValue.length()>0&&_inputText.getTextWidth()>0){
          Vector2f absolutePosition = _inputText.calcAbsolutePosition();
          float positionRelativeElement = (absolutePosition.x + _inputText.getTextWidth()) - mousePos.x;
          float averageSymbols =  _inputText.getTextWidth()/_inputValue.length();

          int pos = Math.abs((int)(positionRelativeElement/averageSymbols)-_inputValue.length());

          if(pos>(_inputValue.length())){
              pos = _inputValue.length();
          }else if(pos<0){
              pos = 0;
          }
          _cursorPosition = pos;
      }

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
            if(_cursorPosition>_inputValue.length()){
                _cursorPosition = _inputValue.length();
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
   * Clear _inputValue; set cursor position to "0"; set input text to "";
   */
  public void clearData() {
      if(_inputValue.length() > 0){
         _inputValue.delete(0,_inputValue.length()-1);
      }
      _cursorPosition = 0;
      _inputText.setText("");
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
      if(_textWidthInContainer > getPosition().x + getSize().x || getPosition().x + _inputText.getPosition().x < 0){
          _inputText.setPosition(new Vector2f(( _inputText.getPosition().x + (getPosition().x + getSize().x - _textWidthInContainer)),_inputText.getPosition().y));
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

