// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.editor.input;

import com.google.common.collect.Lists;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.lwjgl.opengl.awt.AWTGLCanvas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.input.ButtonState;
import org.terasology.input.Input;
import org.terasology.input.InputType;
import org.terasology.input.Keyboard;
import org.terasology.input.device.CharKeyboardAction;
import org.terasology.input.device.KeyboardDevice;
import org.terasology.input.device.RawKeyboardAction;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Queue;

/**
 * AWT converting keyboard device representation.
 */
public class AwtKeyboardDevice implements KeyboardDevice {

    private static final Logger logger = LoggerFactory.getLogger(AwtKeyboardDevice.class);
    private static final TIntIntMap AWT_TO_TERA_MAPPING = new TIntIntHashMap();
    private static final TIntObjectMap<TIntIntHashMap> AWT_TO_TERA_EXTRA = new TIntObjectHashMap<>();

    static {
        //TODO: test and cleanup keys
//        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_NONE, Keyboard.KeyId.NONE);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_ESCAPE, Keyboard.KeyId.ESCAPE);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_1, Keyboard.KeyId.KEY_1);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_2, Keyboard.KeyId.KEY_2);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_3, Keyboard.KeyId.KEY_3);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_4, Keyboard.KeyId.KEY_4);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_5, Keyboard.KeyId.KEY_5);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_6, Keyboard.KeyId.KEY_6);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_7, Keyboard.KeyId.KEY_7);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_8, Keyboard.KeyId.KEY_8);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_9, Keyboard.KeyId.KEY_9);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_0, Keyboard.KeyId.KEY_0);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_MINUS, Keyboard.KeyId.MINUS);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_BACK_SPACE, Keyboard.KeyId.BACKSPACE);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_TAB, Keyboard.KeyId.TAB);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_Q, Keyboard.KeyId.Q);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_W, Keyboard.KeyId.W);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_E, Keyboard.KeyId.E);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_R, Keyboard.KeyId.R);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_T, Keyboard.KeyId.T);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_Y, Keyboard.KeyId.Y);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_U, Keyboard.KeyId.U);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_I, Keyboard.KeyId.I);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_O, Keyboard.KeyId.O);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_P, Keyboard.KeyId.P);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_OPEN_BRACKET, Keyboard.KeyId.LEFT_BRACKET);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_CLOSE_BRACKET, Keyboard.KeyId.RIGHT_BRACKET);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_A, Keyboard.KeyId.A);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_S, Keyboard.KeyId.S);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_D, Keyboard.KeyId.D);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_F, Keyboard.KeyId.F);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_G, Keyboard.KeyId.G);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_H, Keyboard.KeyId.H);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_J, Keyboard.KeyId.J);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_K, Keyboard.KeyId.K);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_L, Keyboard.KeyId.L);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_SEMICOLON, Keyboard.KeyId.SEMICOLON);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_DEAD_ACUTE, Keyboard.KeyId.APOSTROPHE);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_DEAD_GRAVE, Keyboard.KeyId.GRAVE);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_BACK_SLASH, Keyboard.KeyId.BACKSLASH);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_Z, Keyboard.KeyId.Z);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_X, Keyboard.KeyId.X);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_C, Keyboard.KeyId.C);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_V, Keyboard.KeyId.V);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_B, Keyboard.KeyId.B);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_N, Keyboard.KeyId.N);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_M, Keyboard.KeyId.M);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_COMMA, Keyboard.KeyId.COMMA);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_PERIOD, Keyboard.KeyId.PERIOD);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_SLASH, Keyboard.KeyId.SLASH);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_MULTIPLY, Keyboard.KeyId.NUMPAD_MULTIPLY);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_SPACE, Keyboard.KeyId.SPACE);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_CAPS_LOCK, Keyboard.KeyId.CAPS_LOCK);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_F1, Keyboard.KeyId.F1);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_F2, Keyboard.KeyId.F2);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_F3, Keyboard.KeyId.F3);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_F4, Keyboard.KeyId.F4);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_F5, Keyboard.KeyId.F5);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_F6, Keyboard.KeyId.F6);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_F7, Keyboard.KeyId.F7);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_F8, Keyboard.KeyId.F8);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_F9, Keyboard.KeyId.F9);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_F10, Keyboard.KeyId.F10);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_NUM_LOCK, Keyboard.KeyId.NUM_LOCK);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_SCROLL_LOCK, Keyboard.KeyId.SCROLL_LOCK);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_NUMPAD7, Keyboard.KeyId.NUMPAD_7);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_NUMPAD8, Keyboard.KeyId.NUMPAD_8);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_NUMPAD9, Keyboard.KeyId.NUMPAD_9);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_SUBTRACT, Keyboard.KeyId.NUMPAD_MINUS);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_NUMPAD4, Keyboard.KeyId.NUMPAD_4);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_NUMPAD5, Keyboard.KeyId.NUMPAD_5);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_NUMPAD6, Keyboard.KeyId.NUMPAD_6);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_ADD, Keyboard.KeyId.NUMPAD_PLUS);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_NUMPAD1, Keyboard.KeyId.NUMPAD_1);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_NUMPAD2, Keyboard.KeyId.NUMPAD_2);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_NUMPAD3, Keyboard.KeyId.NUMPAD_3);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_NUMPAD0, Keyboard.KeyId.NUMPAD_0);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_DECIMAL, Keyboard.KeyId.NUMPAD_PERIOD);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_F11, Keyboard.KeyId.F11);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_F12, Keyboard.KeyId.F12);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_F13, Keyboard.KeyId.F13);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_F14, Keyboard.KeyId.F14);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_F15, Keyboard.KeyId.F15);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_F16, Keyboard.KeyId.F16);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_F17, Keyboard.KeyId.F17);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_F18, Keyboard.KeyId.F18);
//        glfwToTeraMaps.put(KeyEvent.VK_KANA, Keyboard.KeyId.KANA);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_F19, Keyboard.KeyId.F19);
//        glfwToTeraMaps.put(KeyEvent.VK_CONVERT, Keyboard.KeyId.CONVERT);
//        glfwToTeraMaps.put(KeyEvent.VK_NOCONVERT, Keyboard.KeyId.NOCONVERT);
//        glfwToTeraMaps.put(KeyEvent.VK_YEN, Keyboard.KeyId.YEN);

//        glfwToTeraMaps.put(KeyEvent.VK_CIRCUMFLEX, Keyboard.KeyId.CIRCUMFLEX);
//        glfwToTeraMaps.put(KeyEvent.VK_AT, Keyboard.KeyId.AT);
//        glfwToTeraMaps.put(KeyEvent.VK_COLON, Keyboard.KeyId.COLON);
//        glfwToTeraMaps.put(KeyEvent.VK_UNDERLINE, Keyboard.KeyId.UNDERLINE);
//        glfwToTeraMaps.put(KeyEvent.VK_KANJI, Keyboard.KeyId.KANJI);
//        glfwToTeraMaps.put(KeyEvent.VK_STOP, Keyboard.KeyId.STOP);
//        glfwToTeraMaps.put(KeyEvent.VK_AX, Keyboard.KeyId.AX);
//        glfwToTeraMaps.put(KeyEvent.VK_UNLABELED, Keyboard.KeyId.UNLABELED);

//        glfwToTeraMaps.put(KeyEvent.VK_SECTION, Keyboard.KeyId.SECTION);
//        glfwToTeraMaps.put(KeyEvent.VK_KP_COMMA, Keyboard.KeyId.NUMPAD_COMMA);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_DIVIDE, Keyboard.KeyId.NUMPAD_DIVIDE);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_PRINTSCREEN, Keyboard.KeyId.PRINT_SCREEN);

//        glfwToTeraMaps.put(KeyEvent.VK_FUNCTION, Keyboard.KeyId.FUNCTION);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_PAUSE, Keyboard.KeyId.PAUSE);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_HOME, Keyboard.KeyId.HOME);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_UP, Keyboard.KeyId.UP);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_PAGE_UP, Keyboard.KeyId.PAGE_UP);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_LEFT, Keyboard.KeyId.LEFT);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_RIGHT, Keyboard.KeyId.RIGHT);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_END, Keyboard.KeyId.END);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_DOWN, Keyboard.KeyId.DOWN);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_PAGE_DOWN, Keyboard.KeyId.PAGE_DOWN);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_INSERT, Keyboard.KeyId.INSERT);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_DELETE, Keyboard.KeyId.DELETE);
//        glfwToTeraMaps.put(KeyEvent.VK_CLEAR, Keyboard.KeyId.CLEAR);
        AWT_TO_TERA_MAPPING.put(KeyEvent.VK_META, Keyboard.KeyId.LEFT_META);
//         AWT_TO_TERA_MAPPING.put(KeyEvent.VK_RIGHT_SUPER, Keyboard.KeyId.RIGHT_META);
//        glfwToTeraMaps.put(KeyEvent.VK_APPS, Keyboard.KeyId.APPS);
//        glfwToTeraMaps.put(KeyEvent.VK_POWER, Keyboard.KeyId.POWER);
//        glfwToTeraMaps.put(KeyEvent.VK_SLEEP, Keyboard.KeyId.SLEEP);

        TIntIntHashMap controlMap = new TIntIntHashMap();
        controlMap.put(KeyEvent.KEY_LOCATION_LEFT, Keyboard.KeyId.LEFT_CTRL);
        controlMap.put(KeyEvent.KEY_LOCATION_RIGHT, Keyboard.KeyId.RIGHT_CTRL);
        AWT_TO_TERA_EXTRA.put(KeyEvent.VK_CONTROL, controlMap);
        TIntIntHashMap shiftMap = new TIntIntHashMap();
        shiftMap.put(KeyEvent.KEY_LOCATION_LEFT, Keyboard.KeyId.LEFT_SHIFT);
        shiftMap.put(KeyEvent.KEY_LOCATION_RIGHT, Keyboard.KeyId.RIGHT_SHIFT);
        AWT_TO_TERA_EXTRA.put(KeyEvent.VK_SHIFT, shiftMap);
        TIntIntHashMap altMap = new TIntIntHashMap();
        altMap.put(KeyEvent.KEY_LOCATION_LEFT, Keyboard.KeyId.LEFT_ALT);
        altMap.put(KeyEvent.KEY_LOCATION_RIGHT, Keyboard.KeyId.RIGHT_ALT);
        AWT_TO_TERA_EXTRA.put(KeyEvent.VK_ALT, altMap);
        TIntIntHashMap metaMap = new TIntIntHashMap();
        metaMap.put(KeyEvent.KEY_LOCATION_LEFT, Keyboard.KeyId.LEFT_META);
        metaMap.put(KeyEvent.KEY_LOCATION_RIGHT, Keyboard.KeyId.RIGHT_META);
        AWT_TO_TERA_EXTRA.put(KeyEvent.VK_META, metaMap);
        TIntIntHashMap equalsMap = new TIntIntHashMap();
        equalsMap.put(KeyEvent.KEY_LOCATION_NUMPAD, Keyboard.KeyId.NUMPAD_EQUALS);
        equalsMap.put(KeyEvent.KEY_LOCATION_STANDARD, Keyboard.KeyId.EQUALS);
        equalsMap.put(KeyEvent.KEY_LOCATION_UNKNOWN, Keyboard.KeyId.EQUALS);
        AWT_TO_TERA_EXTRA.put(KeyEvent.VK_EQUALS, equalsMap);
        TIntIntHashMap enterMap = new TIntIntHashMap();
        enterMap.put(KeyEvent.KEY_LOCATION_NUMPAD, Keyboard.KeyId.NUMPAD_ENTER);
        enterMap.put(KeyEvent.KEY_LOCATION_STANDARD, Keyboard.KeyId.ENTER);
        enterMap.put(KeyEvent.KEY_LOCATION_UNKNOWN, Keyboard.KeyId.ENTER);
        AWT_TO_TERA_EXTRA.put(KeyEvent.VK_ENTER, enterMap);
    }

    private Queue<RawKeyboardAction> rawKeyQueue = Lists.newLinkedList();
    private Queue<CharKeyboardAction> charQueue = Lists.newLinkedList();
    private TIntSet buttonStates = new TIntHashSet();

    public AwtKeyboardDevice() {
    }

    public void registerToAwtGlCanvas(AWTGLCanvas canvas) {
        canvas.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                charQueue.offer(new CharKeyboardAction(e.getKeyChar()));
            }

            @Override
            public void keyPressed(KeyEvent e) {
                awtKeyCallback(e.getExtendedKeyCode(), ButtonState.DOWN, e.getKeyLocation());
            }

            @Override
            public void keyReleased(KeyEvent e) {
                awtKeyCallback(e.getExtendedKeyCode(), ButtonState.UP, e.getKeyLocation());
            }
        });
    }

    /**
     * Callback receive key input events.
     */
    public void awtKeyCallback(int key, ButtonState state, int location) {
        int teraKey;
        TIntIntHashMap extraMap = AWT_TO_TERA_EXTRA.get(key);
        if (extraMap != null) {
            teraKey = extraMap.get(key);
        } else {
            teraKey = AWT_TO_TERA_MAPPING.get(key);
        }
        Input input = InputType.KEY.getInput(teraKey);

        if (state == ButtonState.DOWN) {
            buttonStates.add(teraKey);
        } else if (state == ButtonState.UP) {
            buttonStates.remove(teraKey);
        }

        rawKeyQueue.offer(new RawKeyboardAction(input, state));
    }

    @Override
    public boolean isKeyDown(int key) {
        return buttonStates.contains(key);
    }

    @Override
    public Queue<RawKeyboardAction> getInputQueue() {
        Queue<RawKeyboardAction> rawKeyboardActions = Lists.newLinkedList();
        RawKeyboardAction action;
        while ((action = rawKeyQueue.poll()) != null) {
            rawKeyboardActions.add(action);
        }
        return rawKeyboardActions;
    }

    @Override
    public Queue<CharKeyboardAction> getCharInputQueue() {
        Queue<CharKeyboardAction> charActions = Lists.newLinkedList();
        CharKeyboardAction action;
        while ((action = charQueue.poll()) != null) {
            charActions.add(action);
        }
        return charActions;
    }
}
