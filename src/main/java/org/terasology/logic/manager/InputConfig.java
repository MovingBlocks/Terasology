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
package org.terasology.logic.manager;

import com.google.protobuf.TextFormat;
import org.lwjgl.input.Keyboard;
import org.terasology.protobuf.InputData;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Overdhose
 *         Date: 1/07/12
 */
public final class InputConfig {

    private Logger logger = Logger.getLogger(getClass().getName());
    private final static InputConfig _instance = new InputConfig();
    private InputData.InputSetting.Builder _inputsetting;

    public static InputConfig getInstance() {
        return _instance;
    }

    private InputConfig() {
        if (!loadLastConfig()) {
            loadDefaultConfig();
        }
    }

    private boolean loadLastConfig() {
        return loadConfig(new File(PathManager.getInstance().getWorldPath(), "lastinput.cfg"));
    }

    public void loadDefaultConfig() {
        _inputsetting = InputData.InputSetting.newBuilder();
    }

    public boolean loadConfig(File file) {

        InputData.InputSetting.Builder setting = InputData.InputSetting.newBuilder();
        if (file.exists()) {
            logger.log(Level.INFO, "Using config file: " + file);
            try {
                FileInputStream fis = new FileInputStream(file);
                InputStreamReader isr = new InputStreamReader(fis);
                TextFormat.merge(isr, setting);
                isr.close();
            } catch (Exception e) {
                logger.log(Level.WARNING, "Could not load config file " + file, e);
                return false;
            }
        }
        _inputsetting = setting;
        return true;
    }

    public void saveConfig(File file) {
        try {
            logger.log(Level.INFO, "Using config file: " + file);
            FileOutputStream fos = new FileOutputStream(file);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            TextFormat.print(_inputsetting.build(), osw);
            osw.close();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Could not write " + file, e);
        }
    }

    private int strToKey(String skey) {
        if (skey.startsWith("MOUSE")) {
            if (skey.equals("MOUSELEFT")) {
                return 256;
            } else if (skey.equals("MOUSERIGHT")) {
                return 257;
            } else if (skey.equals("MOUSEMIDDLE")) {
                return 258;
            } else if (skey.equals("MOUSEWHEELUP")) {
                return 259;
            } else if (skey.equals("MOUSEWHEELDOWN")) {
                return 260;
            } else {
                return 261;
            }
        } else {
            return Keyboard.getKeyIndex(skey);
        }
    }
    
    private String keyToStr(int key) {
    	if (key < 256) {
            return Keyboard.getKeyName(key);
        } else {
            if (key == 256) {
                return "MOUSELEFT";
            } else if (key == 257) {
                return "MOUSERIGHT";
            } else if (key == 258) {
                return "MOUSEMIDDLE";
            } else if (key == 259) {
                return "MOUSEWHEELUP";
            } else if (key == 260) {
                return "MOUSEWHEELDOWN";
            } else {
                return "MOUSEMOVE";
            }
        }
    }

    /* Get / Set methods */

    public int getKeyForward() {
        return strToKey(_inputsetting.getKeyForward());
    }
    
    public void setKeyForward(int i) {
    	_inputsetting.setKeyForward(keyToStr(i));
    }

    public int getKeyBackward() {
        return strToKey(_inputsetting.getKeyBackward());
    }
    
    public void setKeyBackward(int i) {
    	_inputsetting.setKeyBackward(keyToStr(i));
    }

    public int getKeyJumpbehaviour() {
        return strToKey(_inputsetting.getJumpbehaviour());
    }
    
    public void setKeyJumpbehaviour(int i) {
    	//TODO remove jump behavior button?
    }

    public int getKeyAttack() {
        return strToKey(_inputsetting.getKeyAttack());
    }
    
    public void setKeyAttack(int i) {
    	_inputsetting.setKeyAttack(keyToStr(i));
    }

    public int getKeyConsole() {
        return strToKey(_inputsetting.getKeyConsole());
    }
    
    public void setKeyConsole(int i) {
    	_inputsetting.setKeyConsole(keyToStr(i));
    }

    public int getKeyCrouch() {
        return strToKey(_inputsetting.getKeyCrouch());
    }
    
    public void setKeyCrouch(int i) {
    	_inputsetting.setKeyCrouch(keyToStr(i));
    }

    public int getKeyFrob() {
        return strToKey(_inputsetting.getKeyFrob());
    }
    
    public void setKeyFrob(int i) {
    	_inputsetting.setKeyFrob(keyToStr(i));
    }

    public int getKeyHidegui() {
        return strToKey(_inputsetting.getKeyHidegui());
    }
    
    public void setKeyHidegui(int i) {
    	_inputsetting.setKeyHidegui(keyToStr(i));
    }

    public int getKeyInventory() {
        return strToKey(_inputsetting.getKeyInventory());
    }
    
    public void setKeyInventory(int i) {
    	_inputsetting.setKeyInventory(keyToStr(i));
    }

    public int getKeyJump() {
        return strToKey(_inputsetting.getKeyJump());
    }
    
    public void setKeyJump(int i) {
    	_inputsetting.setKeyJump(keyToStr(i));
    }

    public int getKeyLeft() {
        return strToKey(_inputsetting.getKeyLeft());
    }
    
    public void setKeyLeft(int i) {
    	_inputsetting.setKeyLeft(keyToStr(i));
    }

    public int getKeyMinionmode() {
        return strToKey(_inputsetting.getKeyMinionmode());
    }
    
    public void setKeyMinionmode(int i) {
    	_inputsetting.setKeyMinionmode(keyToStr(i));
    }

    public int getKeyPause() {
        return strToKey(_inputsetting.getKeyPause());
    }
    
    public void setKeyPause(int i) {
    	_inputsetting.setKeyPause(keyToStr(i));
    }

    public int getKeyRight() {
        return strToKey(_inputsetting.getKeyRight());
    }
    
    public void setKeyRight(int i) {
    	_inputsetting.setKeyRight(keyToStr(i));
    }

    public int getKeyRun() {
        return strToKey(_inputsetting.getKeyRun());
    }
    
    public void setKeyRun(int i) {
    	_inputsetting.setKeyRun(keyToStr(i));
    }

    public int getKeyToolnext() {
        return strToKey(_inputsetting.getKeyToolnext());
    }
    
    public void setKeyToolnext(int i) {
    	_inputsetting.setKeyToolnext(keyToStr(i));
    }

    public int getKeyToolprev() {
        return strToKey(_inputsetting.getKeyToolprev());
    }
    
    public void setKeyToolprev(int i) {
    	_inputsetting.setKeyToolprev(keyToStr(i));
    }

    public int getKeyToolslot1() {
        return strToKey(_inputsetting.getKeyToolslot1());
    }
    
    public void setKeyToolslot1(int i) {
    	_inputsetting.setKeyToolslot1(keyToStr(i));
    }

    public int getKeyToolslot2() {
        return strToKey(_inputsetting.getKeyToolslot2());
    }
    
    public void setKeyToolslot2(int i) {
    	_inputsetting.setKeyToolslot2(keyToStr(i));
    }
    
    public int getKeyToolslot3() {
        return strToKey(_inputsetting.getKeyToolslot3());
    }
    
    public void setKeyToolslot3(int i) {
    	_inputsetting.setKeyToolslot3(keyToStr(i));
    }
    
    public int getKeyToolslot4() {
        return strToKey(_inputsetting.getKeyToolslot4());
    }  
    
    public void setKeyToolslot4(int i) {
    	_inputsetting.setKeyToolslot4(keyToStr(i));
    }

    public int getKeyToolslot5() {
        return strToKey(_inputsetting.getKeyToolslot5());
    }
    
    public void setKeyToolslot5(int i) {
    	_inputsetting.setKeyToolslot5(keyToStr(i));
    }

    public int getKeyToolslot6() {
        return strToKey(_inputsetting.getKeyToolslot6());
    }
    
    public void setKeyToolslot6(int i) {
    	_inputsetting.setKeyToolslot6(keyToStr(i));
    }

    public int getKeyToolslot7() {
        return strToKey(_inputsetting.getKeyToolslot7());
    }
    
    public void setKeyToolslot7(int i) {
    	_inputsetting.setKeyToolslot7(keyToStr(i));
    }

    public int getKeyToolslot8() {
        return strToKey(_inputsetting.getKeyToolslot8());
    }
    
    public void setKeyToolslot8(int i) {
    	_inputsetting.setKeyToolslot8(keyToStr(i));
    }

    public int getKeyToolslot9() {
        return strToKey(_inputsetting.getKeyToolslot9());
    }
    
    public void setKeyToolslot9(int i) {
    	_inputsetting.setKeyToolslot9(keyToStr(i));
    }

    public int getKeyUsehelditem() {
        return strToKey(_inputsetting.getKeyUsehelditem());
    }
    
    public void setKeyUsehelditem(int i) {
    	_inputsetting.setKeyUsehelditem(keyToStr(i));
    }
}
