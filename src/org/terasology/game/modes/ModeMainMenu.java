package org.terasology.game.modes;

import org.terasology.game.Terasology;

//OpenGL
import org.lwjgl.opengl.Display;


//GUI
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.menus.UIMainMenu;

import java.util.ArrayList;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.terasology.logic.characters.Player;
import org.terasology.logic.manager.ConfigurationManager;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.utilities.FastRandom;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import static org.lwjgl.opengl.GL11.*;
/**
 *
 * @author kireev
 */
public class ModeMainMenu implements IGameMode{
  
  //GUI
  private ArrayList<UIDisplayElement> _guiScreens = new ArrayList<UIDisplayElement>();
  private UIMainMenu         _mainMenu;
  //private UIOptionsMenu      _optionsMenu;
  //private UIGenerateMapMenu  _generateMapMenu;
  
  /* CONST */
  private static final int TICKS_PER_SECOND = 60;
  private static final int SKIP_TICKS = 1000 / TICKS_PER_SECOND;

  
  private double _timeAccumulator = 0;
  
  private Terasology _gameInstance = null;
  
  public void init(){
    _gameInstance = Terasology.getInstance();
            
   _mainMenu = new UIMainMenu();


    _guiScreens.add(_mainMenu);
    _mainMenu.setVisible(true);
    Terasology.getInstance().initGroovy();
    Mouse.setGrabbed(false);
    Mouse.setCursorPosition(Display.getWidth() / 2, Display.getHeight() / 2);
  }
   public void updateTimeAccumulator(long currentTime, long startTime){
      return;
  }
  public void update(){
          updateUserInterface();
  }
  
  
  private boolean screenHasFocus() {
      for (UIDisplayElement screen : _guiScreens) {
          if (screen.isVisible() && !screen.isOverlay()) {
              return true;
          }
      }

      return false;
  }
  
  public void render(){
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    glLoadIdentity();
    
    renderUserInterface();
  }
  
  public void renderUserInterface() {
      for (UIDisplayElement screen : _guiScreens) {
          screen.render();
      }
  }
  
  private void updateUserInterface() {
      for (UIDisplayElement screen : _guiScreens) {
          screen.update();
      }
  }
  
  public WorldRenderer getActiveWorldRenderer() {
      return null;
  }
  
  /**
   * Process keyboard input - first look for "system" like events, then otherwise pass to the Player object
   */
  public void processKeyboardInput() {
      while (Keyboard.next()) {
          int key = Keyboard.getEventKey();

          if (!Keyboard.isRepeatEvent() && Keyboard.getEventKeyState()) {
              if (key == Keyboard.KEY_ESCAPE && !Keyboard.isRepeatEvent() && Keyboard.getEventKeyState()) {
                _gameInstance.exit(false);
                return;
              }
              // Pass input to focused GUI element
              for (UIDisplayElement screen : _guiScreens) {
                  if (screenCanFocus(screen)) {
                      screen.processKeyboardInput(key);
                  }
              }
          }
      }
  }
  

  /*
   * Process mouse input - nothing system-y, so just passing it to the Player class
   */
  public void processMouseInput() {
      while (Mouse.next()) {
          int button = Mouse.getEventButton();
          int wheelMoved = Mouse.getEventDWheel();

          for (UIDisplayElement screen : _guiScreens) {
              if (screenCanFocus(screen)) {
                  screen.processMouseInput(button, Mouse.getEventButtonState(), wheelMoved);
              }
          }
      }
  }
  

  private boolean screenCanFocus(UIDisplayElement s) {
      boolean result = true;

      for (UIDisplayElement screen : _guiScreens) {
          if (screen.isVisible() && !screen.isOverlay() && screen != s)
              result = false;
      }

      return result;
  }
  
  public void updatePlayerInput(){
    return;
  }
 
}