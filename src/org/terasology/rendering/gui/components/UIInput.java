package org.terasology.rendering.gui.components;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.terasology.logic.manager.AudioManager;
import org.terasology.rendering.gui.framework.UIClickListener;
import org.terasology.rendering.gui.framework.UIInputListener;
import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.framework.UIGraphicsElement;

import javax.vecmath.Vector2f;
import java.util.ArrayList;

/**
 *
 * @author small-jeeper
 */
public class UIInput extends UIDisplayContainer {
  private final ArrayList<UIInputListener> _inputListeners = new ArrayList<UIInputListener>();
  private final ArrayList<UIClickListener> _clickListeners = new ArrayList<UIClickListener>();
  
  private final UIGraphicsElement _defaultTexture;
  
  public UIInput(Vector2f size) {
      setSize(size);

      _defaultTexture = new UIGraphicsElement("gui_menu");
      _defaultTexture.setVisible(true);
      _defaultTexture.getTextureSize().set(new Vector2f(256f / 512f, 30f / 512f));
      addDisplayElement(_defaultTexture);
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
            clicked();
        }

        if (_mouseDown) {
            _defaultTexture.getTextureOrigin().set(0f, 60f / 512f);
        } else {
            _defaultTexture.getTextureOrigin().set(0f, 30f / 512f);
        }

    } else {
        _clickSoundPlayed = false;
        _mouseUp = false;
        _mouseDown = false;

        _defaultTexture.getTextureOrigin().set(0f, 0f);
    }
  }   
  
  public void clicked() {
      _focused = _focused?false:true;
  }
  
  public void keyPressed(){
    for (int i = 0; i < _inputListeners.size(); i++) {
      _inputListeners.get(i).keyPressed(this);
    }
  }

}

