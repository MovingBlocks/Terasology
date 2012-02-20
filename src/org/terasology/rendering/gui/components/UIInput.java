package org.terasology.rendering.gui.components;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.terasology.logic.manager.AudioManager;
import org.newdawn.slick.Color;
import org.terasology.rendering.gui.framework.IClickListener;
import org.terasology.rendering.gui.framework.IInputListener;
import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.framework.UIGraphicsElement;
import org.terasology.rendering.gui.components.UIText;
import org.terasology.rendering.gui.components.UITextCursor;

import javax.vecmath.Vector2f;
import java.util.ArrayList;

/**
 * A simple graphical input
 *
 * @author Anton Kireev <adeon.k87@gmail.com>
 */
public class UIInput extends UIDisplayContainer {
  private final ArrayList<IInputListener> _inputListeners = new ArrayList<IInputListener>();
  //private final ArrayList<UIClickListener> _clickListeners = new ArrayList<UIClickListener>();
  private final StringBuffer _inputValue     = new StringBuffer();
  private final UIGraphicsElement _defaultTexture;
  private final UIText _inputText;
  private final UITextCursor _textCursor;
  private final Vector2f _padding = new Vector2f(10f,10f);
  
  private int    _cursorPosition = 0;
  private String _prevInputValue = new String();
  
  private int _maxLength = 42;
  //Dirty Hack =)
    
  private int[] _cursorPosInPix;
  
  public UIInput(Vector2f size) {
      setSize(size);
      setCrop(true);

      _cursorPosInPix = new int[_maxLength];

      _defaultTexture = new UIGraphicsElement("gui_menu");
      _defaultTexture.setVisible(true);
      _defaultTexture.getTextureSize().set(new Vector2f(256f / 512f, 30f / 512f));

      _inputText = new UIText();
      _inputText.setVisible(true);
      _inputText.setColor(Color.black);
      _inputText.setPosition(new Vector2f((getPosition().x + _padding.x), (getPosition().y + _padding.y)));

      _textCursor = new UITextCursor();
      _textCursor.setVisible(false);
      _textCursor.setPosition(new Vector2f((getPosition().x + _padding.x + _inputText.getTextWidth()), (getPosition().y + _padding.y / 2)));

      addDisplayElement(_defaultTexture);
      addDisplayElement(_inputText);
      addDisplayElement(_textCursor);

  }
  
  public void update() {
    _defaultTexture.setSize(getSize());

    Vector2f mousePos = new Vector2f(Mouse.getX(), Display.getHeight() - Mouse.getY());
   //
                                //x: 508 y: 428 w: 256 h: 32
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

    if (_focused) {
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

    float textWidthWhithPos = _inputText.getTextWidth() + _inputText.getPosition().x + getPosition().x;

    if(textWidthWhithPos>(getPosition().x + getSize().x)){
         _inputText.setPosition(new Vector2f((_inputText.getPosition().x + getChangePosition(0)),_inputText.getPosition().y));
    }
  }
  
  public void clicked(Vector2f mousePos) {
      _focused = true;

      return;

      //TO DO add to UITextCursor
      /*int localCoordX = (int)(mousePos.x - getPosition().x);
      int textWidth = _inputText.getTextWidth();

      if(localCoordX>textWidth){
          _cursorPosition = _inputValue.length();
          _textCursor.setPosition(new Vector2f(_padding.x + _inputText.getTextWidth()/2, (_textCursor.getPosition().y)));
      }else{
          boolean continueSearch = true;
          int tempPos = _cursorPosition;
          int tempTextWidth = textWidth;
          while(continueSearch){
              textWidth /= 2;
              tempTextWidth /= 2;
              if(localCoordX<textWidth){

              }else if(localCoordX>textWidth){

              }
          }
      } */

  }

//TO DO  Fix it
  public void processKeyboardInput(int key){
    if(_focused){
        Vector2f cursorPosition = new Vector2f();

        if (key == Keyboard.KEY_BACK) {

            _cursorPosition--;

            if(_cursorPosition<0){
                _cursorPosition = 0;
            }

            if(_inputValue.length()>0){
                _prevInputValue = _inputValue.toString();
                _inputValue.deleteCharAt((_cursorPosition));
            }

            float textPosition  = _inputText.getPosition().x;
            int   subPosition = getChangePosition(0);

            if(textPosition<0){
                if(textPosition + subPosition>0&&textPosition + subPosition<_padding.x){
                    _inputText.setPosition(new Vector2f(_padding.x + textPosition,_inputText.getPosition().y));
                }else{
                    _inputText.setPosition(new Vector2f(textPosition + subPosition,_inputText.getPosition().y));
                }
            }

            cursorPosition = new Vector2f(_padding.x/2 + _inputText.getTextWidth()/2, (_textCursor.getPosition().y));
        }else if(key == Keyboard.KEY_LEFT){
            _cursorPosition--;
            if(_cursorPosition<0){
                _cursorPosition = 0;
            }else{      //TO DO add to UITextCursor
                cursorPosition =  new Vector2f(_padding.x + (_inputText.getTextWidth() - getChangePosition(_cursorPosition))/2, (_textCursor.getPosition().y));
            }
        }else if(key == Keyboard.KEY_RIGHT){
            _cursorPosition++;
            if(_cursorPosition>_inputValue.length()-1){
                _cursorPosition = _inputValue.length()-1;
            }else{
                cursorPosition =  new Vector2f(_padding.x + (_inputText.getTextWidth() - getChangePosition(_cursorPosition))/2, (_textCursor.getPosition().y));
            }
        }else{
            if(_inputValue.length()>_maxLength){
                return;
            }
            char c = Keyboard.getEventCharacter();
            if (c >= 'a' && c < 'z' + 1 || c >= '0' && c < '9' + 1 || c >= 'A' && c < 'Z' + 1 || c == ' ' || c == '_' || c == '.' || c == ',' || c == '!' || c == '-' || c == '(' || c == ')' || c == '"' || c == '\'' || c == ';' || c == '+') {
                _prevInputValue = _inputValue.toString();

                _inputValue.insert(_cursorPosition,c);
                //_inputValue.a
                _cursorPosition++;
           }
           cursorPosition = new Vector2f(_padding.x + _inputText.getTextWidth()/2, (_textCursor.getPosition().y));
        }
        _inputText.setText(_inputValue.toString());
        //TO DO add to UITextCursor
        //TO DO WTF?! What happened with getPositon? Fix it
        _textCursor.setPosition(new Vector2f(cursorPosition));
        //System.out.println(_textCursor.getPosition());
    }
  }
  
  public void keyPressed(){
    for (int i = 0; i < _inputListeners.size(); i++) {
      _inputListeners.get(i).keyPressed(this);
    }
  }

  public String getValue(){
      return _inputValue.toString();
  }
    
  public void setValue(String value){
      _inputValue.append(value);
  }

  public void setColor(Color color){
    _inputText.setColor(color);
  }
    //TO DO add to UITextCursor
  private int getChangePosition(int cursorPos){
      if(cursorPos==1){
          int prevWidth    = _inputText.getFont().getWidth(_prevInputValue);
          int currentWidth = _inputText.getFont().getWidth(_inputValue.toString());
          return (prevWidth - currentWidth);
      }else{
          return  _inputText.getFont().getWidth(_inputValue.toString())
                  -
                  _inputText.getFont().getWidth(_inputValue.toString().substring(0,(cursorPos>0?cursorPos-1:0)));
      }
  }

  /*private void addIndexPos(int index, int posPix){
      if(index<(_cursorPosInPix.length-1)){
        _cursorPosInPix[index] = posPix;
      }
  }

  private void delteIndexPos(int index, int posPix){
      if(index<(_cursorPosInPix.length-1)){
        _cursorPosInPix[index] = posPix;
      }
  }*/

  public void setMaxLength(int max){
      _cursorPosInPix = new int[_maxLength];
      _maxLength = max;
  }
    
  public int getMaxLength(){
      return _maxLength;
  }

}

