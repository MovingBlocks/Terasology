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

    private void loadDefaultConfig() {
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

    public int getKey(String skey) {
        if (skey.startsWith("MOUSE")) {
            if (skey == "MOUSELEFT") {
                return 256;
            } else if (skey == "MOUSERIGHT") {
                return 257;
            } else if (skey == "MOUSEMIDDLE") {
                return 258;
            } else if (skey == "MOUSEWHEELUP") {
                return 259;
            } else if (skey == "MOUSEWHEELDOWN") {
                return 260;
            } else {
                return 261;
            }
        } else {
            return Keyboard.getKeyIndex(skey);
        }
    }

    /* Get / Set methods */

    public int getKeyForward() {
        return getKey(_inputsetting.getKeyForward());
    }

    public int getKeyBackward() {
        return getKey(_inputsetting.getKeyBackward());
    }

    public int getJumpbehaviour() {
        return getKey(_inputsetting.getJumpbehaviour());
    }

    public int getKeyAttack() {
        return getKey(_inputsetting.getKeyAttack());
    }

    public int getKeyConsole() {
        return getKey(_inputsetting.getKeyConsole());
    }

    public int getKeyCrouch() {
        return getKey(_inputsetting.getKeyCrouch());
    }

    public int getKeyFrob() {
        return getKey(_inputsetting.getKeyFrob());
    }

    public int getKeyHidegui() {
        return getKey(_inputsetting.getKeyHidegui());
    }

    public int getKeyInventory() {
        return getKey(_inputsetting.getKeyInventory());
    }

    public int getKeyJump() {
        return getKey(_inputsetting.getKeyJump());
    }

    public int getKeyLeft() {
        return getKey(_inputsetting.getKeyLeft());
    }

    public int getKeyMinionmode() {
        return getKey(_inputsetting.getKeyMinionmode());
    }

    public int getKeyPauze() {
        return getKey(_inputsetting.getKeyPauze());
    }

    public int getKeyRight() {
        return getKey(_inputsetting.getKeyRight());
    }

    public int getKeyRun() {
        return getKey(_inputsetting.getKeyRun());
    }

    public int getKeyToolnext() {
        return getKey(_inputsetting.getKeyToolnext());
    }

    public int getKeyToolprev() {
        return getKey(_inputsetting.getKeyToolprev());
    }

    public int getKeyToolslot1() {
        return getKey(_inputsetting.getKeyToolslot1());
    }

    public int getKeyToolslot2() {
        return getKey(_inputsetting.getKeyToolslot2());
    }

    public int getKeyToolslot3() {
        return getKey(_inputsetting.getKeyToolslot3());
    }

    public int getKeyToolslot4() {
        return getKey(_inputsetting.getKeyToolslot4());
    }

    public int getKeyToolslot5() {
        return getKey(_inputsetting.getKeyToolslot5());
    }

    public int getKeyToolslot6() {
        return getKey(_inputsetting.getKeyToolslot6());
    }

    public int getKeyToolslot7() {
        return getKey(_inputsetting.getKeyToolslot7());
    }

    public int getKeyToolslot8() {
        return getKey(_inputsetting.getKeyToolslot8());
    }

    public int getKeyToolslot9() {
        return getKey(_inputsetting.getKeyToolslot9());
    }

    public int getKeyUsehelditem() {
        return getKey(_inputsetting.getKeyUsehelditem());
    }

}
