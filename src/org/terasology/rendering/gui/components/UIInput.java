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

import javax.vecmath.Vector2f;
import java.util.ArrayList;

/**
 *
 * @author small-jeeper
 */
public class UIInput extends UIDisplayContainer {
  private final ArrayList<IInputListener> _inputListeners = new ArrayList<IInputListener>();
  //private final ArrayList<UIClickListener> _clickListeners = new ArrayList<UIClickListener>();
  private final StringBuffer _inputValue = new StringBuffer();
  private final UIGraphicsElement _defaultTexture;
  private final UIText _inputText;
  private final Vector2f _padding = new Vector2f(10f,10f);
  
  public UIInput(Vector2f size) {
      setSize(size);
      setCrop(true);

      _defaultTexture = new UIGraphicsElement("gui_menu");
      _defaultTexture.setVisible(true);
      _defaultTexture.getTextureSize().set(new Vector2f(256f / 512f, 30f / 512f));

      _inputText = new UIText();
      _inputText.setVisible(true);
      _inputText.setColor(Color.red);
      _inputText.setPosition(new Vector2f((getPosition().x + _padding.x), (getPosition().y + _padding.y)));
      addDisplayElement(_defaultTexture);
      addDisplayElement(_inputText);
     // update();
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
            clicked();
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
      _defaultTexture.getTextureOrigin().set(0f, 120f / 512f);
    } else {
      _defaultTexture.getTextureOrigin().set(0f, 90f / 512f);
    }

      _inputText.setText(_inputValue.toString());
     // if((_inputText.getPosition().x + getPosition().x + _inputText.getTextWidth())>(getPosition().x + getSize().x)){
     //     _inputText.setPosition(new Vector2f((_inputText.getPosition().x + getPosition().x -8f),_inputText.getPosition().y));
     // }
  }
  
  public void clicked() {
      System.out.println("x: " + Mouse.getX()+ " y: " + (Display.getHeight() - Mouse.getY()));
      System.out.println("Y: " + getPosition().y +  " My: " + (Display.getHeight() - Mouse.getY()));
      _focused = true;
  }

  public void processKeyboardInput(int key){
    if(_focused){
      //  System.out.println(_inputText.getPosition().x);
        if (key == Keyboard.KEY_BACK) {
            int length = _inputValue.length() - 1;

            if (length < 0) {
                length = 0;
            }
            _inputValue.setLength(length);
        }else{
            char c = Keyboard.getEventCharacter();
         //   if (c >= 'a' && c < 'z' + 1 || c >= '0' && c < '9' + 1 || c >= 'A' && c < 'Z' + 1 || c == ' ' || c == '_' || c == '.' || c == ',' || c == '!' || c == '-' || c == '(' || c == ')' || c == '"' || c == '\'' || c == ';' || c == '+') {
                _inputValue.append(c);
          //  }
        }
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

}

